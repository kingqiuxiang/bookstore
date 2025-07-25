package org.example.bookstore.controller;

import org.example.bookstore.model.Order;
import org.example.bookstore.service.OrderService;
import org.example.bookstore.service.mq.producer.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private KafkaProducerService kafkaProducerService;

    @PostMapping("/save-order")
    @ResponseBody
    public Order saveOrder(@RequestParam Integer quantity,  @RequestParam String bookId) {

        Order order = orderService.createOrder(bookId, quantity);
        kafkaProducerService.sendMessage("order-topic", "http request: " + order.getId() + " for book: " + bookId + " with quantity: " + quantity);
        return order;
    }

    @GetMapping("/get-order")
    @ResponseBody
    public List<Order> getAllOrder() {

        List<Order> allOrder = orderService.getAllOrder();
        kafkaProducerService.sendMessage("order-topic", "http request: get all orders, total: " + allOrder.size());

//        for (int i = 0; i < 100000; i++) {
//            kafkaProducerService.sendMessage("order-topic", "retry method send : http request: get all orders, total: " + allOrder.size());
//        }
        return allOrder;
    }


}
