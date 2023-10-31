package org.babi.backend.common.api.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    private final List<ExceptionRule> exceptionRules;

    @Autowired
    public GlobalErrorAttributes(List<ExceptionRule> exceptionRules) {
        this.exceptionRules = exceptionRules;
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = getError(request);
        Optional<ExceptionRule> optionalExceptionRule = exceptionRules.stream()
                .map(exceptionRule -> exceptionRule.getType().isInstance(error) ? exceptionRule : null)
                .filter(Objects::nonNull)
                .findFirst();
        return optionalExceptionRule.<Map<String, Object>>map(exceptionRule ->
                Map.of(
                        GlobalErrorAttributesKey.HTTP_STATUS.getValue(), exceptionRule.getHttpStatus(),
                        GlobalErrorAttributesKey.MESSAGE.getValue(), error.getMessage()
                ))
                .orElse(Map.of(GlobalErrorAttributesKey.HTTP_STATUS.getValue(), HttpStatus.INTERNAL_SERVER_ERROR,
                        GlobalErrorAttributesKey.MESSAGE.getValue(), "Internal server error"));
    }
}
