package com.banking.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtAuthGatewayFilter extends AbstractGatewayFilterFactory<JwtAuthGatewayFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public JwtAuthGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            // Public paths — skip JWT validation
            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            String token = extractToken(request);
            if (token == null) {
                return sendUnauthorized(exchange, "Missing Authorization header");
            }

            try {
                Claims claims = validateAndExtract(token);
                String role = claims.get("role", String.class);

                // Admin-only path guard
                if (path.contains("/api/v1/admin") && !"ADMIN".equals(role)) {
                    return sendForbidden(exchange, "Access denied: Admin only");
                }

                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Email", claims.getSubject())
                        .header("X-User-Id", String.valueOf(claims.get("userId", Long.class)))
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (ExpiredJwtException e) {
                return sendUnauthorized(exchange, "Token expired");
            } catch (Exception e) {
                return sendUnauthorized(exchange, "Invalid token");
            }
        };
    }

    private boolean isPublicPath(String path) {
        return path.contains("/api/v1/auth/") ||
               path.contains("/actuator") ||
               path.contains("/eureka");
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Claims validateAndExtract(String token) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private Mono<Void> sendUnauthorized(ServerWebExchange exchange, String message) {
        return sendError(exchange, HttpStatus.UNAUTHORIZED, message);
    }

    private Mono<Void> sendForbidden(ServerWebExchange exchange, String message) {
        return sendError(exchange, HttpStatus.FORBIDDEN, message);
    }

    private Mono<Void> sendError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"success\":false,\"error\":\"%s\",\"status\":%d}", message, status.value());
        var buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {}
}
