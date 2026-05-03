package com.techstore.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.service-role-key}")
    private String supabaseKey;

    private final ObjectMapper mapper;

    public SupabaseConfig(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Bean
    public RestTemplate supabaseRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("apikey", supabaseKey);
            request.getHeaders().set("Authorization", "Bearer " + supabaseKey);
            request.getHeaders().set("Content-Type", "application/json");
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
