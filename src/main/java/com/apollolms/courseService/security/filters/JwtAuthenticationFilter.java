package com.apollolms.courseService.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws IOException, ServletException {

        try {

            String jwt = parseJwt(req);

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                System.out.println("ATTEMPTING PRODUCT ATHENTICATION FILTER!! .....");
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                List<SimpleGrantedAuthority> authorities=new ArrayList<>();
                Pair<Object[], List<String>> authoritiesInfo = jwtUtils.getRoleNamesFromJwtToken(jwt);

                for(String roleName : authoritiesInfo.b){
                    authorities.add(new SimpleGrantedAuthority(roleName));
                }
                authorities.add(new SimpleGrantedAuthority("ROLE_" + authoritiesInfo.a[0]));

                System.out.println("USERNAME: " + username);
                System.out.println("AUTHORITIES: " + authorities);


                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("AuthTokenFilter | doFilterInternal | Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(req, res);
    }

    private String parseJwt(HttpServletRequest request) {

        String headerAuth = request.getHeader("Authorization");
        log.info("AuthTokenFilter | parseJwt | headerAuth: {}", headerAuth);

        if (StringUtils.hasText(headerAuth)) {
            log.info("AuthTokenFilter | parseJwt | parseJwt: {}", headerAuth.substring(7));
            return headerAuth.substring(7);
        }

        return null;
    }
}
