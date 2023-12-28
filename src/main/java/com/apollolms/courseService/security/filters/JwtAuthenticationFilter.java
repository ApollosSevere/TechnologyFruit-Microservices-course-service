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

        /* *IMPORTANT: This page really is just parsing the request to retrieve
        *              important information!
        *              - If the request reaches up to this point we know for sure that
        *                it is Authorized and Authenticated! --> Know all we have to do
        *                is pull the necessary information out!  */

        try {

            String jwt = parseJwt(req);
            System.out.println("ON doFilterInternal ! .....");

            boolean check = jwt != null;

            System.out.println("Is jwt present?: " + check);
            System.out.println("JWT: " + jwt);
            System.out.println("IS JWT Valid?: " + jwtUtils.validateJwtToken(jwt));


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

                /* The entire point of this is to set the SecurityContext for the whole
                * sub-application!! Find out through thoses tutorials why SecurityContextHolder
                * is imporatnat and how does it work! */
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println(SecurityContextHolder.getContext());
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
