package org.example.bookstore.service.mq.consumer;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.zookeeper.common.StringUtils;
import org.example.bookstore.service.mq.producer.RocketMQTransactionProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    @Autowired
    RocketMQTransactionProducer rocketMQProducer;
    @KafkaListener(topics = "order-topic", groupId = "bookstore-group")
    public void receiveMessage(String message) {


//        创建建表语句并把日志存放在表里面
        if (!StringUtils.isBlank(message)) {
            // 添加到数据库中
            try {
//                rocketMQProducer.sendMessage("bookstore-topic", message);
                //发送事务消息,并通过异步的回调判断消息的rollback

                TransactionSendResult  transactionSendResult = rocketMQProducer.sendTransactionMessage("bookstore-topic", message);
                if (! transactionSendResult.getLocalTransactionState().equals(LocalTransactionState.COMMIT_MESSAGE)){
                    System.out.println("do rollback for other system ");
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("Received message: " + message);
        }
    }
}