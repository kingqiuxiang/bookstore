package org.example.bookstore.config;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.example.bookstore.service.event.EnvironmentChangeEvent;

@Component
public class ConfigChangeListener implements ApplicationListener<EnvironmentChangeEvent> {
    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        // 这里处理配置变更事件
        System.out.println("检测到配置变更: " + event.getSource());
    }
}


