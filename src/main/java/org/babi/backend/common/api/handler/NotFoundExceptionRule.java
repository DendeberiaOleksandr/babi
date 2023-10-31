package org.babi.backend.common.api.handler;

import org.babi.backend.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class NotFoundExceptionRule implements ExceptionRule {

    @Override
    public Class<? extends RuntimeException> getType() {
        return ResourceNotFoundException.class;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
