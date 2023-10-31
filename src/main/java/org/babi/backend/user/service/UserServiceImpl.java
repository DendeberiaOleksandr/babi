package org.babi.backend.user.service;

import lombok.extern.slf4j.Slf4j;
import org.babi.backend.common.exception.ResourceNotFoundException;
import org.babi.backend.user.dao.R2DBCUserRepository;
import org.babi.backend.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final R2DBCUserRepository userRepository;

    @Autowired
    public UserServiceImpl(R2DBCUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<User> findById(Long id) {
        return userRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(String.format("User does not exist by id: %s", id))));
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(String.format("Account does not exist with email: %s", email))));
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(String.format("Account does not exist with username: %s", username))));
    }

    @Override
    public Mono<User> findByOtp(String otp) {
        return userRepository.findByOtp(otp);
    }

    @Override
    public Mono<User> findByUsernameOrEmail(String username, String email) {
        return userRepository.findByUsernameOrEmail(username, email)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(String.format("Account does not exist with username: %s or email: %s", username, email))));
    }

    @Override
    public Mono<User> save(User user) {
        return userRepository.save(user);
    }
}
