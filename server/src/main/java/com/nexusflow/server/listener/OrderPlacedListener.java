package com.nexusflow.server.listener;

import com.nexusflow.server.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderPlacedListener {

    @RabbitListener(queues = RabbitMQConfig.ORDER_PLACED_QUEUE)
    public void handleOrderPlaced(Long orderId) {
        log.info("Received Order Placed Event for Order ID: {}", orderId);
        try {
            // Simulate time-consuming task
            Thread.sleep(2000);
            log.info("Successfully processed post-order actions for Order ID: {}", orderId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error processing order event", e);
        }
    }
}
