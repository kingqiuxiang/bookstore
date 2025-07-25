package org.example.bookstore.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreatePartitionsResult;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Configuration
public class KafkaTopicConfig {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Bean
    public NewTopic myTopic() throws ExecutionException, InterruptedException, TimeoutException {
//       如果topic已经存在就增加分区到50,如果不存在就创建分区
        int currentPartitions = kafkaTemplate.partitionsFor("order-topic").size();
        if (currentPartitions == 1) {
            AdminClient admin = AdminClient.create(kafkaTemplate.getProducerFactory().getConfigurationProperties());
            Map<String, NewPartitions> partitionsMap = new HashMap<>();
            partitionsMap.put("order-topic", NewPartitions.increaseTo(50));
            CreatePartitionsResult result = admin.createPartitions(partitionsMap);
            result.all().get(30, TimeUnit.SECONDS);
            System.out.println("Successfully expanded partitions to " + 50 + " for topic 'order-topic'");
        }

        return TopicBuilder.name("order-topic")
                .partitions(50)  // 设置分区数为50
                .replicas(1)     // 设置副本数为1
                .config("retention.ms", "604800000") // 7天保留
                .build();
    }
}