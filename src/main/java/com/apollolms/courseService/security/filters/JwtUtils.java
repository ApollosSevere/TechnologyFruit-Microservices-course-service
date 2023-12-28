package com.apollolms.courseService.security.filters;


import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.antlr.v4.runtime.misc.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class JwtUtils extends SecurityProperties.Filter {
    private static final String PREFIX_STATUS = "role";
    private static final String PREFIX_PERMISSIONS = "authorities";

    @Value("${application.security.jwt.secret-key}")
    private String jwtSecret;
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUserNameFromJwtToken(String token) {
        if(token.startsWith("Bearer")){
            token = token.substring(7);
        }
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public Pair<Object[], List<String>> getRoleNamesFromJwtToken(String token) {
        if(token.startsWith("Bearer")){
            token = token.substring(7);
        }

        Claims roles = extractAllClaims(token);

        final Object[] role = {""};
        List<String> authorities = new ArrayList<>();
        
        roles.forEach((key, value) -> {
            if (key.startsWith(PREFIX_STATUS)) {
                log.trace("adding claim [key: {}, value: {}]", key, value);
                role[0] = value;
            } else if (key.startsWith(PREFIX_PERMISSIONS)) {
                authorities.addAll((List<String>) value);
            } else {
                log.trace("ignoring unsupported claim [key: {}]", key);
            }
        });


        return new Pair<>(role, authorities);
    }


    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            LOGGER.error("JwtUtils | validateJwtToken | Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOGGER.error("JwtUtils | validateJwtToken | JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.error("JwtUtils | validateJwtToken | JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("JwtUtils | validateJwtToken | JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
