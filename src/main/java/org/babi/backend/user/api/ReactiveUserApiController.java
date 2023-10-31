package org.babi.backend.user.api;

import lombok.extern.slf4j.Slf4j;
import org.babi.backend.user.service.UserRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class ReactiveUserApiController {

    private final UserRegistrationService registrationService;

    @Autowired
    public ReactiveUserApiController(UserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PreAuthorize("permitAll()")
    @PostMapping
    public Mono<ResponseEntity<?>> register(@RequestBody UserRegistrationRequest registrationRequest) {
        return registrationService.register(registrationRequest)
                .map(user -> ResponseEntity.ok(user.getId()));
    }

}
