package org.babi.backend.security.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalSecurityApiErrorHandler {

    @ExceptionHandler(value = AccessDeniedException.class)
    public Mono<ResponseEntity<?>> handleAccessDeniedException(AccessDeniedException e) {
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied"));
    }

}
