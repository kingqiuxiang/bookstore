package org.example.bookstore.service;

import org.example.bookstore.model.Order;
import org.example.bookstore.service.mq.producer.OrderMessageProducer;
import org.example.bookstore.util.BookRequestIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderMessageProducer messageProducer;

    @Autowired
    private BookRequestIdGenerator bookRequestIdGenerator;

    @Autowired
    private RedisTemplate<String, Order> orderRedisTemplate;


    public Order createOrder(String bookId, Integer quantity) {

        Long requestId = bookRequestIdGenerator.generate(Long.parseLong(bookId));
        Order order = new Order(bookId, quantity, requestId);
        // 保存订单到数据库...

        // 发送消息,实现异步存储数据库
        sendMsg(order);

        return order;
    }

    @Async("asyncTaskExecutor")
    public void sendMsg(Order order) {
        messageProducer.sendOrderCreatedEvent(order);
    }

    public List<Order> getAllOrder() {

        return orderRedisTemplate.opsForList().range("orders:", 0, -1);


    }
}