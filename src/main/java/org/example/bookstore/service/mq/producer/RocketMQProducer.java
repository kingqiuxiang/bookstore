package org.example.bookstore.service.mq.producer;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RocketMQProducer {
    private final RocketMQTemplate rocketMQTemplate;

    public RocketMQProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    // 发送字符串消息
    public void sendMessage(String topic, String message) throws MQClientException {
        DefaultMQProducer producer = rocketMQTemplate.getProducer();
        rocketMQTemplate.convertAndSend(topic, message);
    }

    // 发送带 Tag 的消息（格式: topic:tag）
    public void sendMessageWithTag(String topicAndTag, Object payload) {
        rocketMQTemplate.convertAndSend(topicAndTag, payload);
    }
}