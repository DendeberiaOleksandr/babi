package org.babi.backend.user.service.registration;

import org.babi.backend.common.exception.ResourceNotFoundException;
import org.babi.backend.mail.service.MailService;
import org.babi.backend.security.encryption.EncryptionService;
import org.babi.backend.user.api.UserRegistrationRequest;
import org.babi.backend.user.domain.User;
import org.babi.backend.user.domain.UserRole;
import org.babi.backend.user.exception.UserAlreadyExistsException;
import org.babi.backend.user.service.UserService;
import org.babi.backend.user.service.UserRegistrationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final EncryptionService encryptionService;

    public UserRegistrationServiceImpl(UserService userService,
                                       PasswordEncoder passwordEncoder,
                                       MailService mailService,
                                       EncryptionService encryptionService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.encryptionService = encryptionService;
    }

    @Transactional
    @Override
    public Mono<User> register(UserRegistrationRequest registrationRequest) {
        String email = registrationRequest.getEmail();
        return userService.findByEmail(email)
                .flatMap(user -> Mono.error(new UserAlreadyExistsException(email)))
                .onErrorReturn(ResourceNotFoundException.class, new User(
                        null, null, registrationRequest.getEmail(), false, registrationRequest.isServiceTermsAccepted(),
                        passwordEncoder.encode(registrationRequest.getPassword()),
                        null, LocalDateTime.now(), LocalDateTime.now(), UserRole.USER, null
                ))
                .flatMap(userToRegister -> userService.save((User) userToRegister));
    }
}
