package org.babi.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class ServerSecurityContextRepositoryImpl implements ServerSecurityContextRepository {

    private static final String TOKEN_PREFIX = "Bearer ";

    private final ReactiveAuthenticationManager authenticationManager;

    @Autowired
    public ServerSecurityContextRepositoryImpl(ReactiveAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        throw new UnsupportedOperationException("Security context saving is not supported");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("authorization");
        UsernamePasswordAuthenticationToken authenticationToken = Optional.ofNullable(authHeader)
                .filter(h -> h.startsWith(TOKEN_PREFIX))
                .map(h -> h.substring(TOKEN_PREFIX.length()))
                .map(token -> new UsernamePasswordAuthenticationToken(token, token))
                .orElse(null);
        return authenticationToken == null ? Mono.empty() : authenticationManager.authenticate(authenticationToken).map(SecurityContextImpl::new);
    }
}
