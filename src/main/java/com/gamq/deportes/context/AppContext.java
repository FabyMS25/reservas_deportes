package com.gamq.deportes.context;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppContext {

    @Bean
    RestTemplate restTemplate(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${ruat.client.connect-timeout}") Duration connectTimeout,
            @Value("${ruat.client.read-timeout}") Duration readTimeout) {
        RestTemplate restTemplate = restTemplateBuilder
                .additionalInterceptors(new RuatHttpLoggingInterceptor("deportes-be->ruat-be"))
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    // Preserve authentication-error bodies on POST requests with the JDK client.
                    factory.setOutputStreaming(false);
                    return factory;
                })
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .build();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        restTemplate.getMessageConverters().add(converter);
        return restTemplate;
    }

}
