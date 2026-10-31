package com.banking.common.security;

import com.banking.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                Claims claims = jwtTokenProvider.extractAllClaims(token);
                String email = claims.getSubject();
                String role = claims.get("role", String.class);
                Object userIdObj = claims.get("userId");
                Long userId = null;
                if (userIdObj instanceof Number) {
                    userId = ((Number) userIdObj).longValue();
                } else if (userIdObj != null) {
                    try {
                        userId = Long.parseLong(userIdObj.toString());
                    } catch (NumberFormatException ignored) {}
                }

                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
                authToken.setDetails(userId);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage());
            sendErrorResponse(response, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        response.getWriter().write(mapper.writeValueAsString(
                ApiResponse.error(message, status.value())
        ));
    }
}
