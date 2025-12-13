package com.nexusflow.server.service;

import com.nexusflow.server.config.RabbitMQConfig;
import com.nexusflow.server.dto.OrderDto;
import com.nexusflow.server.entity.*;
import com.nexusflow.server.exception.InsufficientStockException;
import com.nexusflow.server.exception.ResourceNotFoundException;
import com.nexusflow.server.repository.OrderRepository;
import com.nexusflow.server.repository.ProductRepository;
import com.nexusflow.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Order createOrder(OrderDto.OrderRequest request, String username) {
        log.info("Creating order for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(new ArrayList<>());
        order.setTotalAmount(BigDecimal.ZERO);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderDto.OrderItemRequest itemRequest : request.getItems()) {
            Long productId = itemRequest.getProductId();
            if (productId == null) {
                throw new IllegalArgumentException("Product ID cannot be null");
            }
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName() + 
                    ". Available: " + product.getQuantity() + ", Requested: " + itemRequest.getQuantity()
                );
            }

            // Optimistic locking handles the decrement safely
            int currentQty = product.getQuantity() != null ? product.getQuantity() : 0;
            product.setQuantity(currentQty - itemRequest.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build();

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CONFIRMED);

        Order savedOrder = orderRepository.save(order);
        
        log.info("Order created successfully with ID: {} for user: {}", savedOrder.getId(), username);

        // Async notification
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_PLACED_QUEUE, savedOrder.getId());

        return savedOrder;
    }

    public List<Order> getUserOrders(String username) {
        log.debug("Fetching orders for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return orderRepository.findByUserId(user.getId());
    }
}
