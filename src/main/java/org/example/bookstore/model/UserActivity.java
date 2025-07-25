package org.example.bookstore.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.util.Date;


@Document(collection = "user_activities")
public class UserActivity {
    @Id
    private String id;
    private Long userId;
    private String action;
    private Date timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
// getters/setters


    public UserActivity() {
    }

    public UserActivity(Long userId, String action, Date timestamp) {
        this.userId = userId;
        this.action = action;
        this.timestamp = timestamp;
    }
}

