package org.example.bookstore.util;

import org.example.bookstore.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final RedisTemplate genericRedisTemplate;


    public DataInitializer(UserService userService, RedisTemplate genericRedisTemplate) {
        this.userService = userService;
        this.genericRedisTemplate = genericRedisTemplate;
    }

    @Override
    public void run(String... args) {
        // 创建初始用户
        userService.registerUser("alice", "alice@example.com");
        userService.registerUser("bob", "bob@example.com");
        Object res = genericRedisTemplate.opsForValue().get("books:count");
        if (res == null) {
            genericRedisTemplate.opsForValue().set("books:count", 1000);
        }

        // 为bob添加额外活动
        userService.logUserActivity(2L, "LOGIN");
        userService.logUserActivity(2L, "UPDATE_PROFILE");
    }
}