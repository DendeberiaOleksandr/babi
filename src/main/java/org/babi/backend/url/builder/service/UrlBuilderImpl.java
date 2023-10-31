package org.babi.backend.url.builder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlBuilderImpl implements UrlBuilder {

    private String registrationVerificationPath;

    @Override
    public String buildRegistrationVerificationUrl(String token) {
        return new StringBuilder(registrationVerificationPath).append("?token=").append(token).toString();
    }

    @Value("${babi.url.registrationVerificationPath:http://localhost:3000/client/web/emailConfirmation}")
    public void setRegistrationVerificationPath(String registrationVerificationPath) {
        this.registrationVerificationPath = registrationVerificationPath;
    }
}
