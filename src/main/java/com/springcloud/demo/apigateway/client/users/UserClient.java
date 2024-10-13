package com.springcloud.demo.apigateway.client.users;

import com.springcloud.demo.apigateway.client.users.dto.UserDTO;
import com.springcloud.demo.apigateway.exceptions.SimpleException;
import com.springcloud.demo.apigateway.monitoring.TracingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${spring.cloud.gateway.routes[4].uri}")
    private String usersUri;

    public Mono<UserDTO> findByEmail(String email) {

        return webClientBuilder
                .build()
                .get()
                .uri(usersUri + "/api/users/email/" + email)
                .header("X-Amzn-Trace-Id", TracingUtils.getXRayHeader())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> {
                            return response
                                    .bodyToMono(Map.class)
                                    .flatMap(body -> {
                                        return Mono.error(new SimpleException(
                                                response.statusCode().value(),
                                                (String) body.get("message"))
                                        );
                                    });
                        }

                )
                .bodyToMono(UserDTO.class);
    }
}
