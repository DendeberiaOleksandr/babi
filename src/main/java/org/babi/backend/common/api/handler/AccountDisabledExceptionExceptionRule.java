package org.babi.backend.common.api.handler;

import org.babi.backend.security.excepion.AccountDisabledException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AccountDisabledExceptionExceptionRule implements ExceptionRule {
    @Override
    public Class<? extends RuntimeException> getType() {
        return AccountDisabledException.class;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
