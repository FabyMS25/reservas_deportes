package com.gamq.deportes.config;

import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.dto.response.Status;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.EnumMap;
import java.util.Map;

@ControllerAdvice
public class ResponseStatusAdvice implements ResponseBodyAdvice<Object> {
    private static final Map<Status, HttpStatus> HTTP_STATUSES = new EnumMap<>(Status.class);

    static {
        HTTP_STATUSES.put(Status.OK, HttpStatus.OK);
        HTTP_STATUSES.put(Status.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        HTTP_STATUSES.put(Status.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        HTTP_STATUSES.put(Status.VALIDATION_EXCEPTION, HttpStatus.BAD_REQUEST);
        HTTP_STATUSES.put(Status.EXCEPTION, HttpStatus.INTERNAL_SERVER_ERROR);
        HTTP_STATUSES.put(Status.WRONG_CREDENTIALS, HttpStatus.UNAUTHORIZED);
        HTTP_STATUSES.put(Status.ACCESS_DENIED, HttpStatus.FORBIDDEN);
        HTTP_STATUSES.put(Status.NOT_FOUND, HttpStatus.NOT_FOUND);
        // Existing controllers use NOT_CONTENT from catch blocks, so it represents
        // a server failure rather than a successful empty 204 response.
        HTTP_STATUSES.put(Status.NOT_CONTENT, HttpStatus.INTERNAL_SERVER_ERROR);
        HTTP_STATUSES.put(Status.DUPLICATE_ENTITY, HttpStatus.CONFLICT);
        HTTP_STATUSES.put(Status.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY);
        HTTP_STATUSES.put(Status.UNSUPPORTED_MEDIA_TYPE, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (body instanceof Response) {
            Status status = ((Response<?>) body).getStatus();
            response.setStatusCode(HTTP_STATUSES.getOrDefault(status, HttpStatus.INTERNAL_SERVER_ERROR));
        }
        return body;
    }
}
