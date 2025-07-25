package org.example.bookstore.service.mq.producer;

import org.example.bookstore.config.RabbitMQConfig;
import org.example.bookstore.model.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderMessageProducer {
    
    private final RabbitTemplate rabbitTemplate;
    
    public OrderMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public void sendOrderCreatedEvent(Order order) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ORDER_ROUTING_KEY,
            order
        );
    }
}