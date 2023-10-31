package org.babi.backend.security.api;

import lombok.extern.slf4j.Slf4j;
import org.babi.backend.security.UserDetailsImpl;
import org.babi.backend.security.excepion.AccountDisabledException;
import org.babi.backend.security.excepion.InvalidCredentialsException;
import org.babi.backend.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ReactiveAuthenticationApiController {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ReactiveUserDetailsService userDetailsService;

    @Autowired
    public ReactiveAuthenticationApiController(JwtService jwtService,
                                               PasswordEncoder passwordEncoder,
                                               ReactiveUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/auth")
    public Mono<ResponseEntity<AuthenticationResponse>> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        return userDetailsService.findByUsername(authenticationRequest.getUsername())
                .flatMap(userDetails -> userDetails.isEnabled() ?
                        Mono.just(userDetails) : Mono.error(new AccountDisabledException("Please confirm an email to be able to sign in")))
                .filter(userDetails -> passwordEncoder.matches(authenticationRequest.getPassword(), userDetails.getPassword()))
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid password provided!")))
                .map(userDetails -> {
                    UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
                    return ResponseEntity.ok(new AuthenticationResponse(userDetailsImpl.getEmail(),
                            userDetailsImpl.getUsername(),
                            userDetailsImpl.getAuthorities().stream().findFirst().get().getAuthority(),
                            jwtService.generateToken(userDetails)));
                });
    }

}
