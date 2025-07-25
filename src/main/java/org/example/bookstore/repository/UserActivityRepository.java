package org.example.bookstore.repository;

import org.example.bookstore.model.UserActivity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserActivityRepository extends MongoRepository<UserActivity, String> {
    List<UserActivity> findByUserId(Long userId);
    List<UserActivity> findAll();
}
