package org.example.bookstore.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * todo @qiuxiang.wang ,以后集成Elasticsearch时需要配置的类
 */
@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String[] elasticUris;
    
    @Value("${spring.elasticsearch.username}")
    private String username;
    
    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean
    public RestClient restClient() {
        // 创建凭据提供者
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, 
                new UsernamePasswordCredentials(username, password));
        
        // 创建 HTTP Host 数组
        HttpHost[] hosts = Arrays.stream(elasticUris)
                .map(HttpHost::create)
                .toArray(HttpHost[]::new);
        
        // 创建 REST 客户端
        return RestClient.builder(hosts)
                .setHttpClientConfigCallback(httpClientBuilder -> 
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();
    }

    @Bean
    public ElasticsearchTransport getElasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient getElasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}