package com.gamq.deportes.context;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppContext {

    @Bean
    RestTemplate restTemplate(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${ruat.client.connect-timeout:10s}") Duration connectTimeout,
            @Value("${ruat.client.read-timeout:60s}") Duration readTimeout) {
        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .build();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        restTemplate.getMessageConverters().add(converter);
        return restTemplate;
    }

}
