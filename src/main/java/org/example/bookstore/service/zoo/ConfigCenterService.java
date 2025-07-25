package org.example.bookstore.service.zoo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.example.bookstore.service.event.EnvironmentChangeEvent;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//need add destory method
@Service
public class ConfigCenterService implements DisposableBean {

    private static final String CONFIG_PATH = "/config/bookstore";

    @Autowired
    private CuratorFramework curatorFramework;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private Map<String, String> configCache = new ConcurrentHashMap<>();
    private PathChildrenCache cache;

    @PostConstruct
    public void init() throws Exception {
        loadConfig();
        watchConfigChanges();
        System.out.println(configCache.get("spring.data.redis.url"));
    }
//    在项目启动时修改配置等操作的实现,同时可以用来监听实现服务发现注册销毁等操作
    private void loadConfig() throws Exception {
        if (curatorFramework.checkExists().forPath(CONFIG_PATH) != null) {
            List<String> keys = curatorFramework.getChildren().forPath(CONFIG_PATH);
            for (String key : keys) {
                byte[] data = curatorFramework.getData().forPath(CONFIG_PATH + "/" + key);
                configCache.put(key, new String(data, StandardCharsets.UTF_8));
            }
        }
    }

    private void watchConfigChanges() throws Exception {
         cache = new PathChildrenCache(curatorFramework, CONFIG_PATH, true);
        cache.getListenable().addListener((client, event) -> {
            if (event.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
                String key = event.getData().getPath().replace(CONFIG_PATH + "/", "");
                byte[] data = event.getData().getData();
                System.out.println("-----------------------start--------------------------");
                configCache.put(key, new String(data, StandardCharsets.UTF_8));
                eventPublisher.publishEvent(new EnvironmentChangeEvent(Collections.singleton(key)));
                System.out.println("-----------------------completed--------------------------");

            }
        });
        cache.start();
    }

    public String getConfig(String key) {
        return configCache.get(key);
    }


    @Override
    public void destroy() throws Exception {
        try {
            System.out.println("run destroy method");
            if (cache != null) {
                cache.close();
                curatorFramework.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to close PathChildrenCache", e);
        }
    }
}