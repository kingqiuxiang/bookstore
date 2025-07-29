package org.example.bookstore.service.mq.producer;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {
    private final RocketMQTemplate rocketMQTemplate;

    public MessageProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    // 发送字符串消息
    public void sendMessage(String topic, String message) {
        rocketMQTemplate.convertAndSend(topic, message);
    }

    // 发送带 Tag 的消息（格式: topic:tag）
    public void sendMessageWithTag(String topicAndTag, Object payload) {
        rocketMQTemplate.convertAndSend(topicAndTag, payload);
    }
}