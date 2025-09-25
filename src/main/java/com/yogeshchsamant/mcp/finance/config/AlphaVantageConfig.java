package com.yogeshchsamant.mcp.finance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
public class AlphaVantageConfig {

    @Value("${alphavantage.api.key}")
    private String apiKey;

    @Value("${alphavantage.api.base.url}")
    private String baseUrl;

    @Bean
    public WebClient alphaVantageWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

}