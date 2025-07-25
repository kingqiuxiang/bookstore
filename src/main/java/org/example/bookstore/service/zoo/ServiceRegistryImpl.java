package org.example.bookstore.service.zoo;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceRegistryImpl implements ServiceRegistry {

    private static final String REGISTRY_PATH = "/services/bookstore";

    @Autowired
    private CuratorFramework curatorFramework;

    @Override
    public void register(String serviceName, String serviceAddress) {
        try {
            String servicePath = REGISTRY_PATH + "/" + serviceName;
            
            // 创建持久节点
            if (curatorFramework.checkExists().forPath(servicePath) == null) {
                curatorFramework.create().creatingParentsIfNeeded().forPath(servicePath);
            }
            
            // 创建临时节点（服务实例）
            String addressPath = servicePath + "/" + serviceAddress;
            curatorFramework.create()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(addressPath);
            
        } catch (Exception e) {
            throw new RuntimeException("Service registration failed", e);
        }
    }

    @Override
    public List<String> discover(String serviceName) {
        try {
            String servicePath = REGISTRY_PATH + "/" + serviceName;
            return curatorFramework.getChildren().forPath(servicePath);
        } catch (Exception e) {
            throw new RuntimeException("Service discovery failed", e);
        }
    }
}