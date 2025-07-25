package org.example.bookstore.service;

import org.example.bookstore.model.User;
import org.example.bookstore.model.UserActivity;
import org.example.bookstore.repository.UserActivityRepository;
import org.example.bookstore.repository.UserRepository;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserActivityRepository activityRepository;
    private final RedisTemplate<String, User> userRedisTemplate;
    private final AsyncService asyncService;
    private final RedissonClient redisson;
    public UserService(UserRepository userRepository,
                       UserActivityRepository activityRepository,
                       RedisTemplate<String, User> userRedisTemplate, AsyncService asyncService,RedissonClient redisson) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.userRedisTemplate = userRedisTemplate;
        this.asyncService = asyncService;
        this.redisson=redisson;
    }

    @Transactional
    public User registerUser(String username, String email) {
        // 1. 创建用户 (MySQL)
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        User savedUser = userRepository.save(user);

        // 2. 缓存用户信息 (Redis)
        // cacheUser(savedUser);
        asyncService.cacheUserAsync(savedUser);
        // 3. 记录注册活动 (MongoDB)
        // logUserActivity(savedUser.getId(), );
        asyncService.logActivityAsync(savedUser.getId(), "REGISTER");
        return savedUser;
    }

    public User getUserByUsername(String username) {
        // 1. 先查Redis缓存
        User cachedUser = userRedisTemplate.opsForValue().get("user:" + username);
        if (cachedUser != null) {
            return cachedUser;
        }

        // 2. Redis未命中则查询MySQL
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 3. 将结果缓存到Redis
            cacheUser(user);
            return user;
        }
        return null;
    }

    public List<UserActivity> getUserActivities(Long userId) {
        return activityRepository.findByUserId(userId);
    }

    private void cacheUser(User user) {
        redisson.getLock("user:" + user.getUsername()).lock();
        userRedisTemplate.opsForValue().set("user:" + user.getUsername(), user, 30, TimeUnit.MINUTES);
        redisson.getLock("user:" + user.getUsername()).unlock();
    }

    public void logUserActivity(Long userId, String action) {
        UserActivity activity = new UserActivity();
        activity.setUserId(userId);
        activity.setAction(action);
        activity.setTimestamp(new Date());
        activityRepository.save(activity);
    }
}