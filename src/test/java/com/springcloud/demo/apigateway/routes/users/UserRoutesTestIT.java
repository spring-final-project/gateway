package com.springcloud.demo.apigateway.routes.users;

import com.fasterxml.jackson.core.JsonProcessingException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.springcloud.demo.apigateway.client.users.UserClient;
import com.springcloud.demo.apigateway.client.users.dto.UserDTO;
import com.springcloud.demo.apigateway.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 9090)
public class UserRoutesTestIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserClient userClient;

    @Autowired
    private JwtUtils jwtUtils;

    UserDTO userLogged;
    String uri;

    @Nested
    class CreateUserRoute {
        @BeforeEach
        void setup() {
            uri = "/api/users";
        }

        @Test
        void createUserRoute() throws JsonProcessingException {

            Map<String, Object> requestBody = Map.of("email", "gonza@gmail.com", "password", "Abcd1234");

            stubFor(post(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(201)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .post()
                    .uri(uri)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus()
                    .isCreated();
        }
    }

    @Nested
    class GetUsersRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder().email("gonza@gmail.com").password("Abcd1234").id(UUID.randomUUID()).build();
            uri = "/api/users";
        }

        @Test
        void getUsersRoute() throws JsonProcessingException {
            String token = jwtUtils.generateToken("gonza@gmail.com");

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
            stubFor(get(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        void unauthorizedWhenTokenNotExist() throws JsonProcessingException {
            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
            stubFor(get(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .get()
                    .uri(uri)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        void unauthorizedWhenTokenIsNotValid() throws JsonProcessingException {
            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));

            stubFor(get(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + "invalid_token")
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }
    }

    @Nested
    class GetUserByIdRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder().email("gonza@gmail.com").password("Abcd1234").id(UUID.randomUUID()).build();
            uri = "/api/users/" + userLogged.getId();
        }

        @Test
        void getUserByIdRoute() throws JsonProcessingException {
            String token = jwtUtils.generateToken("gonza@gmail.com");

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
            stubFor(get(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        void unauthorizedWhenTokenIsNotValid() throws JsonProcessingException {
            String token = "invalid_token";

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
            stubFor(get(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        void unauthorizedWhenTokenNotExist() throws JsonProcessingException {
            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
            stubFor(get(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .get()
                    .uri(uri)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }
    }

    @Nested
    class FindByEmailRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder().email("gonza@gmail.com").password("Abcd1234").id(UUID.randomUUID()).build();
            uri = "/api/users/email/" + userLogged.getEmail();
        }

        @Test
        void shouldCannotFindByEmail() throws JsonProcessingException {
            String token = jwtUtils.generateToken("gonza@gmail.com");

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
            stubFor(get(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isNotFound();
        }
    }

    @Nested
    class UpdateUserRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder().email("gonza@gmail.com").password("Abcd1234").id(UUID.randomUUID()).build();
            uri = "/api/users/" + userLogged.getId();
        }

        @Test
        void shouldUpdateUser() throws JsonProcessingException {
            String token = jwtUtils.generateToken("gonza@gmail.com");

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
            stubFor(patch(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .patch()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(userLogged)
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        void unauthorizedWhenTokenIsNotValid() throws JsonProcessingException {
            String token = "invalid_token";

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
            stubFor(patch(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .patch()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(userLogged)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        void unauthorizedWhenTokenNotExist() throws JsonProcessingException {
            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
            stubFor(patch(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .patch()
                    .uri(uri)
                    .bodyValue(userLogged)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }
    }

    @Nested
    class DeleteUserRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder().email("gonza@gmail.com").password("Abcd1234").id(UUID.randomUUID()).build();
            uri = "/api/users/" + userLogged.getId();
        }

        @Test
        void shouldDeleteUser() throws JsonProcessingException {
            String token = jwtUtils.generateToken("gonza@gmail.com");

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
            stubFor(delete(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .delete()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        void unauthorizedWhenTokenIsNotValid() throws JsonProcessingException {
            String token = "invalid_token";

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
            stubFor(delete(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .delete()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        void unauthorizedWhenTokenNotExist() throws JsonProcessingException {
            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
            stubFor(delete(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient
                    .delete()
                    .uri(uri)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }
    }
}
