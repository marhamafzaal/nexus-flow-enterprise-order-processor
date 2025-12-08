package com.nexusflow.server.service;

import com.nexusflow.server.config.RabbitMQConfig;
import com.nexusflow.server.dto.OrderDto;
import com.nexusflow.server.entity.*;
import com.nexusflow.server.repository.OrderRepository;
import com.nexusflow.server.repository.ProductRepository;
import com.nexusflow.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        product = Product.builder().id(1L).name("Test Product").price(BigDecimal.TEN).quantity(100).build();
    }

    @Test
    void createOrder_Success() {
        OrderDto.OrderItemRequest itemRequest = new OrderDto.OrderItemRequest(1L, 5);
        OrderDto.OrderRequest request = new OrderDto.OrderRequest(Collections.singletonList(itemRequest));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(123L);
            return o;
        });

        Order result = orderService.createOrder(request, "testuser");

        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertEquals(new BigDecimal("50"), result.getTotalAmount());
        assertEquals(95, product.getQuantity()); // Inventory check

        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitMQConfig.ORDER_PLACED_QUEUE), eq(123L));
    }

    @Test
    void createOrder_InsufficientStock() {
        OrderDto.OrderItemRequest itemRequest = new OrderDto.OrderItemRequest(1L, 101); // More than 100
        OrderDto.OrderRequest request = new OrderDto.OrderRequest(Collections.singletonList(itemRequest));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(RuntimeException.class, () -> orderService.createOrder(request, "testuser"));

        assertEquals(100, product.getQuantity()); // Unchanged
        verify(orderRepository, never()).save(any());
    }
}
