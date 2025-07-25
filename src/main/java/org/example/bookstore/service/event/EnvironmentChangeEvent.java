package org.example.bookstore.service.event;

import org.springframework.context.ApplicationEvent;

import java.util.Set;

public class EnvironmentChangeEvent extends ApplicationEvent {
    public EnvironmentChangeEvent(Set<String> singleton) {
        super(singleton);
    }
}
