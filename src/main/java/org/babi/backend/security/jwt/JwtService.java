package org.babi.backend.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String getSubjectFromToken(String token);

    String generateToken(UserDetails userDetails);

    boolean isTokenValid(String token);

}
