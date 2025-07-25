package org.example.bookstore.controller;

import org.example.bookstore.model.User;
import org.example.bookstore.model.UserActivity;
import org.example.bookstore.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(
            @RequestParam String username,
            @RequestParam String email) {
        
        User user = userService.registerUser(username, email);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/{userId}/activities")
    public ResponseEntity<List<UserActivity>> getUserActivities(@PathVariable Long userId) {
        List<UserActivity> activities = userService.getUserActivities(userId);
        return ResponseEntity.ok(activities);
    }
}