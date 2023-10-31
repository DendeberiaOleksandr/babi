package org.babi.backend.security;

import lombok.extern.slf4j.Slf4j;
import org.babi.backend.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class JwtReactiveAuthenticationManagerImpl implements ReactiveAuthenticationManager {

    private final JwtService jwtService;
    private final ReactiveUserDetailsService userDetailsService;

    @Autowired
    public JwtReactiveAuthenticationManagerImpl(JwtService jwtService,
                                                ReactiveUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .map(auth -> auth.getCredentials().toString())
                .filter(jwtService::isTokenValid)
                .map(jwtService::getSubjectFromToken)
                .flatMap(userDetailsService::findByUsername)
                .map(userDetails -> (Authentication)new UsernamePasswordAuthenticationToken(
                        userDetails.getUsername(), null,
                        userDetails.getAuthorities()
                ))
                .switchIfEmpty(Mono.empty());
    }
}
