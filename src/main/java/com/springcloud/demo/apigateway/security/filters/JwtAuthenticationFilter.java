package com.springcloud.demo.apigateway.security.filters;

import com.springcloud.demo.apigateway.client.users.UserClient;
import com.springcloud.demo.apigateway.exceptions.SimpleException;
import com.springcloud.demo.apigateway.security.jwt.JwtUtils;
import lombok.Getter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtUtils jwtUtils;
    private final UserClient userClient;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserClient userClient) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
        this.userClient = userClient;
    }

    @Getter
    public static class Config {
        private final List<String> allowedRoles;

        public Config(List<String> allowedRoles) {
            this.allowedRoles = Optional.ofNullable(allowedRoles).orElse(new ArrayList<>());
        }

    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String bearerToken = request.getHeaders().getFirst("Authorization");

            if (bearerToken == null) {
                return Mono.error(new SimpleException(
                        HttpStatus.UNAUTHORIZED.value(), "Token is required")
                );
            }

            String token = bearerToken.split(" ")[1];

            if (jwtUtils.isTokenValid(token)) {
                String email = jwtUtils.getEmailFromToken(token);

                return userClient.findByEmail(email)
                        .flatMap(user -> {
                            boolean isRoleValid = false;
                            if (config.getAllowedRoles().isEmpty()) {
                                isRoleValid = true;
                            } else {
                                isRoleValid = user.getRoles()
                                        .stream()
                                        .anyMatch(role -> config.getAllowedRoles().contains(role.getRole()));
                            }

                            if (!isRoleValid) {
                                return Mono.error(new SimpleException(
                                        HttpStatus.UNAUTHORIZED.value(), "Not have permission")
                                );
                            }

                            exchange.getRequest().mutate()
                                    .header("X-UserId", user.getId().toString())
                                    .build();

                            return chain.filter(exchange);
                        });
            }

            return Mono.error(new SimpleException(
                    HttpStatus.UNAUTHORIZED.value(), "Token not valid")
            );
        };
    }
}
