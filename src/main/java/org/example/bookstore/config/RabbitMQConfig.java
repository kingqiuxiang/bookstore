package org.example.bookstore.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 定义订单相关队列和路由
    public static final String ORDER_QUEUE = "bookstore.order.queue";
    public static final String ORDER_ROUTING_KEY = "order.created";
    // 定义库存更新队列
    public static final String STOCK_QUEUE = "bookstore.stock.queue";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("bookstore.exchange");
    }

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true);
    }

    @Bean
    public Queue stockQueue() {
        return new Queue(STOCK_QUEUE, true);
    }

    @Bean
    public Binding orderBinding(TopicExchange exchange) {
        return BindingBuilder.bind(orderQueue())
                .to(exchange)
                .with(ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding stockBinding(TopicExchange exchange) {
        return BindingBuilder.bind(stockQueue())
                .to(exchange)
                .with(ORDER_ROUTING_KEY);
    }
}