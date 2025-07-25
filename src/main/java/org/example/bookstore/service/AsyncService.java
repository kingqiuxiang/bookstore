package org.example.bookstore.service;

import io.lettuce.core.RedisException;
import org.example.bookstore.model.Order;
import org.example.bookstore.model.User;
import org.example.bookstore.model.UserActivity;
import org.example.bookstore.repository.OrderRepository;
import org.example.bookstore.repository.UserActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class AsyncService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

    private final RedisTemplate<String, User> userRedisTemplate;
    private final UserActivityRepository activityRepository;
    private final RedisTemplate<String, Order> orderRedisTemplate;
    private final RedisTemplate genericRedisTemplate;

    public AsyncService(RedisTemplate<String, User> userRedisTemplate,
                        UserActivityRepository activityRepository,RedisTemplate<String, Order> orderRedisTemplate, RedisTemplate genericRedisTemplate)  {
        this.userRedisTemplate = userRedisTemplate;
        this.activityRepository = activityRepository;
        this.orderRedisTemplate = orderRedisTemplate;
        this.genericRedisTemplate = genericRedisTemplate;
    }

    @Async("asyncTaskExecutor")
    public CompletableFuture<Void> cacheUserAsync(User user) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 带重试的缓存逻辑
                cacheUserWithRetry(user, 3);
            } catch (Exception e) {
                logger.error("Failed to cache user {} after retries", user.getUsername(), e);
                // 可添加监控/告警逻辑
            }
        });
    }

    @Async("asyncTaskExecutor")
    public CompletableFuture<Void> cacheOrderAsync(Order order) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 带重试的缓存逻辑
                cacheOrderWithRetry(order, 3);
            } catch (Exception e) {
                logger.error("Failed to cache orderId {} after retries", order.getId(), e);
                // 可添加监控/告警逻辑
            }
        });
    }


    @Async("asyncTaskExecutor")
    public CompletableFuture<Void> updateStockAsync(Order order) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 带重试的缓存逻辑
                genericRedisTemplate.opsForValue().decrement("books:count",order.getQuantity());
            } catch (Exception e) {
                logger.error("Failed to cache orderId {} after retries", order.getId(), e);
                // 可添加监控/告警逻辑
            }
        });
    }

    private void cacheOrderWithRetry(Order order, int maxRetries) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                orderRedisTemplate.opsForList().rightPush("orders:", order);
                return;
            } catch (RedisConnectionFailureException e) {
                attempt++;
                logger.warn("Redis cache failed (attempt {}/{})", attempt, maxRetries);
                try {
                    Thread.sleep(1000 * (long) Math.pow(2, attempt)); // 指数退避
                } catch (InterruptedException ignored) {
                }
            }
        }
        throw new RedisException("Failed to cache user after " + maxRetries + " attempts");
    }


    @Async("asyncTaskExecutor")
    public CompletableFuture<Void> logActivityAsync(Long userId, String action) {
        return CompletableFuture.runAsync(() -> {
            try {
                UserActivity activity = new UserActivity(userId, action, new Date());
                activityRepository.save(activity);
            } catch (Exception e) {
                logger.error("Failed to log activity for user {}", userId, e);
                // 可添加死信队列处理
            }
        });
    }

    private void cacheUserWithRetry(User user, int maxRetries) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                userRedisTemplate.opsForValue().set(
                        "user:" + user.getUsername(),
                        user,
                        30, TimeUnit.MINUTES
                );
                return;
            } catch (RedisConnectionFailureException e) {
                attempt++;
                logger.warn("Redis cache failed (attempt {}/{})", attempt, maxRetries);
                try {
                    Thread.sleep(1000 * (long) Math.pow(2, attempt)); // 指数退避
                } catch (InterruptedException ignored) {
                }
            }
        }
        throw new RedisException("Failed to cache user after " + maxRetries + " attempts");
    }
}
    

