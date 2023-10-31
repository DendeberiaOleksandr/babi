package org.babi.backend.common.api.handler;

import org.babi.backend.security.excepion.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class InvalidCredentialsExceptionRule implements ExceptionRule {
    @Override
    public Class<? extends RuntimeException> getType() {
        return InvalidCredentialsException.class;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
