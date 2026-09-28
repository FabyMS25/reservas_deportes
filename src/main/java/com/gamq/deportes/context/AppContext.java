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
            @Value("${external.client.connect-timeout}") Duration connectTimeout,
            @Value("${external.client.read-timeout}") Duration readTimeout,
            @Value("${usuarios.base-url}") String usuariosUrl,
            @Value("${ruat.gateway.base-url}") String ruatUrl) {
        // Separate phase limits, shared by all outgoing HTTP calls, including list reviews.
        if (connectTimeout.toMillis() <= 0 || readTimeout.toMillis() <= 0
                || connectTimeout.toMillis() > Integer.MAX_VALUE || readTimeout.toMillis() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("external.client.connect-timeout y external.client.read-timeout deben ser positivos y expresables en milisegundos de 32 bits");
        }
        RestTemplate restTemplate = restTemplateBuilder
                .additionalInterceptors(new ExternalHttpLoggingInterceptor(usuariosUrl, ruatUrl))
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
