package org.example.bookstore.service.mq.consumer;

import org.example.bookstore.config.RabbitMQConfig;
import org.example.bookstore.model.Order;
import org.example.bookstore.repository.OrderRepository;
import org.example.bookstore.service.AsyncService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceConsumer {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AsyncService asyncService;


    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrderCreated(Order order) {
        // 库存扣减逻辑
        order = orderRepository.save(order);
        asyncService.cacheOrderAsync(order);
        System.out.println("Processing order for: " + order.getBookId());

    }

    @RabbitListener(queues = RabbitMQConfig.STOCK_QUEUE)
    public void handleStockUpdate(Order order) {
        // 处理库存更新
        asyncService.updateStockAsync(order);
        System.out.println("Updating stock for: " + order.getBookId());
    }
}