package com.entrusting.backend.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 애플리케이션 공통 설정
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate Bean
     * S2S 통신에 사용
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
