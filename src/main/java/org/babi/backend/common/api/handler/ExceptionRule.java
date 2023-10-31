package org.babi.backend.common.api.handler;

import org.springframework.http.HttpStatus;

public interface ExceptionRule {
    Class<? extends RuntimeException> getType();
    HttpStatus getHttpStatus();
}
