package org.babi.backend.user.dao;

import org.babi.backend.user.domain.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface R2DBCUserRepository extends ReactiveCrudRepository<User, Long> {

    Mono<User> findByEmail(String email);
    Mono<User> findByUsername(String username);
    Mono<User> findByUsernameOrEmail(String username, String email);
    Mono<User> findByOtp(String otp);

}
