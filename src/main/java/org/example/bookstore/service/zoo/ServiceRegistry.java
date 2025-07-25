package org.example.bookstore.service.zoo;

import java.util.List;

public interface ServiceRegistry {
    void register(String serviceName, String serviceAddress);

    List<String> discover(String serviceName);
}