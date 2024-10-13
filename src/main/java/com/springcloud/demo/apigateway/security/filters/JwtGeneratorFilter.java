package com.springcloud.demo.apigateway.security.filters;

import com.springcloud.demo.apigateway.security.jwt.JwtUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class JwtGeneratorFilter extends AbstractGatewayFilterFactory<JwtGeneratorFilter.Config> {

    private final JwtUtils jwtUtils;

    public JwtGeneratorFilter(JwtUtils jwtUtils) {
        super(JwtGeneratorFilter.Config.class);
        this.jwtUtils = jwtUtils;
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();

                if (Objects.equals(response.getStatusCode(), HttpStatus.OK)) {
                    String token = jwtUtils.generateToken(response.getHeaders().getFirst("email"));
                    response.getHeaders().setBearerAuth(token);

                } else {
                    response.getHeaders().remove("Authorization");
                }

                response.getHeaders().remove("email");
            }));

        };
    }
}
