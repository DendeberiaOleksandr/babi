package org.babi.backend.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class PublicRoutesResolver {

    @Bean
    public Map<HttpMethod, String[]> publicRoutes(
            @Value("${babi.security.publicRoutes.post:}") String[] postRoutes,
            @Value("${babi.security.publicRoutes.get:}") String[] getRoutes,
            @Value("${babi.security.publicRoutes.put:}") String[] putRoutes,
            @Value("${babi.security.publicRoutes.patch:}") String[] patchRoutes,
            @Value("${babi.security.publicRoutes.delete:}") String[] deleteRoutes
    ) {
        Map<HttpMethod, String[]> routes = new HashMap<>();
        Optional.of(postRoutes).filter(r -> r.length > 0).ifPresent(r -> routes.put(HttpMethod.POST, postRoutes));
        Optional.of(getRoutes).filter(r -> r.length > 0).ifPresent(r -> routes.put(HttpMethod.GET, r));
        Optional.of(putRoutes).filter(r -> r.length > 0).ifPresent(r -> routes.put(HttpMethod.PUT, r));
        Optional.of(patchRoutes).filter(r -> r.length > 0).ifPresent(r -> routes.put(HttpMethod.PATCH, r));
        Optional.of(deleteRoutes).filter(r -> r.length > 0).ifPresent(r -> routes.put(HttpMethod.DELETE, r));
        return routes;
    }
}
