package com.agnostik.agnostik_app.security;


import com.agnostik.agnostik_app.authentication.JwtService;
import com.agnostik.agnostik_app.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try{

            String subject = jwtService.extractSubject(token);

            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null){

                Long userId = Long.parseLong(subject);

                var user = userRepository.findById(userId).orElse(null);

                if (user != null && jwtService.isTokenValid(token, userId)) {

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user, null, Collections.emptyList());

                    authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Authenticated user '{}' (id={}) from JWT", user.getUsername(), user.getId());

                }
            }



        }catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());

        }

        filterChain.doFilter(request,response);


    }


}
