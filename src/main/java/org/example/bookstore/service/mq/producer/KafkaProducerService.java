package org.example.bookstore.service.mq.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async("asyncTaskExecutor")
    public CompletableFuture<Void> sendMessage(String topic, String message) {

        //async send msg
        return CompletableFuture.runAsync(() -> {
            Random random = new Random();
            int i = random.nextInt(50);
            // 这里可以添加一些额外的处理逻辑
            kafkaTemplate.send(topic,i+"", message); // 发送消息
        });
    }
}