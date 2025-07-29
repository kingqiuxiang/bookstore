package org.example.bookstore.service.mq.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

@Service
// 监听主题 bookstore-topic，消费者组 bookstore-consumer-group
@RocketMQMessageListener(
    topic = "bookstore-topic",
    consumerGroup = "bookstore-consumer-group",
    selectorExpression = "*"  // 消费所有 Tag（可指定如 "tagA || tagB"）
)
public class RocketMQConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        // 这里处理业务逻辑（如订单处理、库存更新等）
    }
}