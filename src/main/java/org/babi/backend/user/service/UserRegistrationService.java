package org.babi.backend.user.service;

import org.babi.backend.user.api.UserRegistrationRequest;
import org.babi.backend.user.domain.User;
import reactor.core.publisher.Mono;

public interface UserRegistrationService {
    Mono<User> register(UserRegistrationRequest registrationRequest);
}
