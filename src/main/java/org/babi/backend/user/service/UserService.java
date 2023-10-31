package org.babi.backend.user.service;

import org.babi.backend.user.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> findById(Long id);
    Mono<User> findByEmail(String email);
    Mono<User> findByUsername(String username);
    Mono<User> findByOtp(String otp);
    Mono<User> findByUsernameOrEmail(String username, String email);
    Mono<User> save(User user);

}
