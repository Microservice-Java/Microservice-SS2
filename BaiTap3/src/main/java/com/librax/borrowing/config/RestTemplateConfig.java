package com.librax.borrowing.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    /**
     * Khai báo RestTemplate Bean gắn Annotation @LoadBalanced.
     * Cho phép RestTemplate tự động giải mã tên dịch vụ logic (ví dụ: 'http://book-service')
     * thông qua Service Registry (Eureka/Consul/Kubernetes DNS) và thực hiện client-side load balancing.
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
