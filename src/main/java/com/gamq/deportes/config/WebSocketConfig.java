package com.gamq.deportes.config;

import com.gamq.deportes.security.DeportesPrincipal;
import com.gamq.deportes.security.DeportesTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final DeportesTokenService tokenService;

    public WebSocketConfig(DeportesTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Value("${app.cors.allowed-origin-patterns}")
    private String[] allowedOriginPatterns;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-notificaciones")
                .setAllowedOriginPatterns(allowedOriginPatterns)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) return message;

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authorization = accessor.getFirstNativeHeader("Authorization");
                    if (authorization == null || !authorization.startsWith("Bearer ")) {
                        throw new IllegalArgumentException("Missing Deportes token");
                    }
                    DeportesPrincipal principal = tokenService.verify(authorization.substring(7));
                    accessor.setUser(new UsernamePasswordAuthenticationToken(
                            principal, null, Collections.emptyList()));
                }

                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    if (!(accessor.getUser() instanceof UsernamePasswordAuthenticationToken)) {
                        throw new IllegalArgumentException("Unauthenticated notification subscription");
                    }
                    DeportesPrincipal principal =
                            (DeportesPrincipal) ((UsernamePasswordAuthenticationToken) accessor.getUser()).getPrincipal();
                    String destination = accessor.getDestination();
                    String ownTopic = "/topic/notificaciones/" + principal.getSubject();
                    if (destination == null ||
                            !(destination.equals(ownTopic) || destination.equals(ownTopic + "/contador"))) {
                        throw new IllegalArgumentException("Notification subscription does not belong to this user");
                    }
                }
                return message;
            }
        });
    }
}
