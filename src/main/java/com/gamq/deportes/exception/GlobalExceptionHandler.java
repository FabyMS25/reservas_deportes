package com.gamq.deportes.exception;

import com.gamq.deportes.dto.response.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public final ResponseEntity<ApiError> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(
                "FORBIDDEN", "No tiene permiso para realizar esta operación", request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ApiError> handleAllException(Exception ex, HttpServletRequest request) {
        ApiError error = ApiError.of(
                "INTERNAL_ERROR",
                "The request could not be completed",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<ApiError> handleNotFoundException(
            NotFoundException ex,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.of(
                "RESOURCE_NOT_FOUND",
                "The requested resource was not found",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public final ResponseEntity<ApiError> handleDuplicateKeyException(
            DuplicateKeyException ex,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.of(
                "RESOURCE_ALREADY_EXISTS",
                "The resource already exists",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public final ResponseEntity<ApiError> handleOperationNotAllowedException(
            OperationNotAllowedException ex,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.of(
                "OPERATION_NOT_ALLOWED",
                "The operation is not allowed",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(FileUploadException.class)
    public final ResponseEntity<ApiError> handleFileUploadException(
            FileUploadException ex,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.of(
                "UNSUPPORTED_FILE",
                "The uploaded file is not supported",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }
}
