package org.example.bookstore.service.mq.producer;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.example.bookstore.util.BookRequestIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.*;

@Service
public class RocketMQTransactionProducer implements InitializingBean {
    
    private static final Logger log = LoggerFactory.getLogger(RocketMQTransactionProducer.class);
    
    private TransactionMQProducer producer;
    
    @Value("${rocketmq.name-server}")
    private String nameServer;
    
    @Value("${rocketmq.producer.group}")
    private String producerGroup;
    @Autowired
    BookRequestIdGenerator bookRequestIdGenerator;

   @Override
    public void afterPropertiesSet() throws MQClientException {
        // 1. 创建事务消息生产者
        producer = new TransactionMQProducer(producerGroup);
        producer.setNamesrvAddr(nameServer);
        
        // 2. 配置线程池用于处理事务检查请求
        ExecutorService executorService = new ThreadPoolExecutor(
            2, 
            5, 
            100, 
            TimeUnit.SECONDS, 
            new ArrayBlockingQueue<>(2000), 
            r -> {
                Thread thread = new Thread(r);
                thread.setName("rocketmq-transaction-thread");
                return thread;
            });
        
        // 3. 设置事务监听器
        producer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                // 执行本地事务
                try {
                    log.info("Execute local transaction for msg: {}", msg);
                    // 这里实际应执行数据库操作或业务逻辑
                    boolean success = executeBusinessLogic(msg);
                    
                    return success ? 
                        LocalTransactionState.COMMIT_MESSAGE : 
                        LocalTransactionState.ROLLBACK_MESSAGE;
                } catch (Exception e) {
                    log.error("Local transaction execution failed", e);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                // 检查本地事务状态（补偿机制）
                log.info("Check local transaction status for msg: {}", msg);
                
                // 这里应该检查业务状态
                String status = checkBusinessStatus(msg.getKeys());
                
                if ("COMMITTED".equals(status)) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                } else if ("ROLLBACKED".equals(status)) {
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                } else {
                    return LocalTransactionState.UNKNOW;
                }
            }
        });
        
        producer.setExecutorService(executorService);
        producer.start();
        log.info("RocketMQ transaction producer started");
    }
    
    public void destroy() {
        if (producer != null) {
            producer.shutdown();
        }
    }

    /**
     * 发送事务消息
     */
    public TransactionSendResult sendTransactionMessage(String topic, String message) throws MQClientException {
        Message msg = new Message(topic, message.getBytes());
        // 设置业务唯一ID，用于回查时定位业务,这里传一个虚拟的userId,但仍然可以做到1000*1024/s的不重复
        msg.setKeys("ORDER_" + bookRequestIdGenerator.generate(1001L));
        return producer.sendMessageInTransaction(msg, null);
    }
    
    /**
     * 执行实际业务逻辑
     */
    private boolean executeBusinessLogic(Message message) {
        // 这里执行实际的业务逻辑
        // 示例：保存消息内容到数据库
        try {
            Thread.sleep(10000);
            log.info("Executing business logic: {}", new String(message.getBody()));
            // 实际应执行数据库操作等业务逻辑
            // 返回true表示业务成功，false表示业务失败
            Random random = new Random();
            return random.nextBoolean();
        } catch (Exception e) {
            log.error("Business logic failed", e);
            return false;
        }
    }
    
    /**
     * 检查业务状态
     */
    private String checkBusinessStatus(String businessKey) {
        // 这里应查询数据库检查业务状态
        log.info("Checking business status for key: {}", businessKey);
        return "COMMITTED"; // 实际应用中需要查询业务状态
    }
}