package org.babi.backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Slf4j
@Service
public class DefaultJwtService implements JwtService {

    private String secretKey;
    private long expiration;

    @Override
    public String getSubjectFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("authorities", userDetails.getAuthorities())
                .signWith(getSigningKey())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .compact();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                    .parseClaimsJws(token)
                    .getBody();
            Long expiration = claims.get("exp", Long.class);
            return new Date(expiration * 1000).after(new Date());
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return false;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    @Value("${babi.security.jwt.secretKey}")
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Value("${babi.security.jwt.expiration:1209600000}")
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
