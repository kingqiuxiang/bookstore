package org.example.bookstore.service.mq.consumer;

import org.apache.zookeeper.common.StringUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    @KafkaListener(topics = "order-topic", groupId = "bookstore-group")
    public void receiveMessage(String message) {

//        创建建表语句并把日志存放在表里面
        if (!StringUtils.isBlank(message)) {
            // 添加到数据库中
            System.out.println("Received message: " + message);
        }
    }
}