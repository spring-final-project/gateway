package com.springcloud.demo.apigateway.routes.asks;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.springcloud.demo.apigateway.client.users.UserClient;
import com.springcloud.demo.apigateway.client.users.dto.UserDTO;
import com.springcloud.demo.apigateway.client.users.dto.UserRoleDTO;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 9090)
public class AskRoutesTestIT {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private UserClient userClient;

    UserDTO userLogged;
    String uri;

    @Nested
    class CreateAskRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/asks";
        }

        @Test
        void createAskRouteByCustomer() {
            String token = jwtUtils.generateToken("gonza@gmail.com");
            Map<String, Object> requestBody = Map.of("question", "First question");

            UserRoleDTO customerRole = UserRoleDTO.builder().role("CUSTOMER").id(userLogged.getId()).build();
            userLogged.setRoles(List.of(customerRole));

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
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
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isCreated();
        }

        @Test
        void unauthorizedWhenUserLoggedIsOwner() {
            String token = jwtUtils.generateToken("gonza@gmail.com");
            Map<String, Object> requestBody = Map.of("question", "First question");

            UserRoleDTO ownerRole = UserRoleDTO.builder().role("OWNER").id(userLogged.getId()).build();
            userLogged.setRoles(List.of(ownerRole));

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
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
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        void unauthorizedWhenTokenIsNotValid() {
            String token = "invalid_token";
            Map<String, Object> requestBody = Map.of("question", "First question");

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
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
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        void unauthorizedWhenTokenNotExist() {
            Map<String, Object> requestBody = Map.of("question", "First question");

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
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
                    .isUnauthorized();
        }
    }

    @Nested
    class GetAskRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/asks";
        }

        @Test
        void getAsks() {
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
        void unauthorizedWhenTokenIsNotValid() {
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
        void unauthorizedWhenTokenNotExist() {

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
    class GetAskByIdRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/asks/" + UUID.randomUUID();
        }

        @Test
        void getAskById() {
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
        void unauthorizedWhenTokenIsNotValid() {
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
        void unauthorizedWhenTokenNotExist() {

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
    class AnswerAskRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/asks/" + UUID.randomUUID() + "/answer";
        }

        @Test
        void answerAsk() {
            String token = jwtUtils.generateToken("gonza@gmail.com");
            Map<String, Object> requestBody = Map.of("answer", "First answer");

            UserRoleDTO ownerRole = UserRoleDTO.builder().role("OWNER").id(userLogged.getId()).build();
            userLogged.setRoles(List.of(ownerRole));

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
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
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isCreated();
        }

        @Test
        void unauthorizedWhenUserLoggedIsCustomer() {
            String token = jwtUtils.generateToken("gonza@gmail.com");
            Map<String, Object> requestBody = Map.of("answer", "First answer");

            UserRoleDTO customerRole = UserRoleDTO.builder().role("CUSTOMER").id(userLogged.getId()).build();
            userLogged.setRoles(List.of(customerRole));

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
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
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        void unauthorizedWhenTokenIsNotValid() {
            String token = "invalid_token";
            Map<String, Object> requestBody = Map.of("answer", "First answer");

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
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
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        void unauthorizedWhenTokenNotExist() {
            Map<String, Object> requestBody = Map.of("answer", "First answer");

            given(userClient.findByEmail(anyString())).willReturn(Mono.just(userLogged));
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
                    .isUnauthorized();
        }
    }

    @Nested
    class DeleteAskRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/asks/" + UUID.randomUUID();
        }

        @Test
        void deleteAsk() {
            String token = jwtUtils.generateToken("gonza@gmail.com");

            UserRoleDTO ownerRole = UserRoleDTO.builder().role("OWNER").id(userLogged.getId()).build();
            userLogged.setRoles(List.of(ownerRole));

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
        void unauthorizedWhenUserLoggedIsCustomer() {
            String token = jwtUtils.generateToken("gonza@gmail.com");

            UserRoleDTO customerRole = UserRoleDTO.builder().role("CUSTOMER").id(userLogged.getId()).build();
            userLogged.setRoles(List.of(customerRole));

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
        void unauthorizedWhenTokenIsNotValid() {
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
        void unauthorizedWhenTokenNotExist() {

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

    @Nested
    class DeleteAnswerRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/asks/" + UUID.randomUUID() + "/answer";
        }

        @Test
        void deleteAnswer() {
            String token = jwtUtils.generateToken("gonza@gmail.com");

            UserRoleDTO ownerRole = UserRoleDTO.builder().role("OWNER").id(userLogged.getId()).build();
            userLogged.setRoles(List.of(ownerRole));

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
        void unauthorizedWhenUserLoggedIsCustomer() {
            String token = jwtUtils.generateToken("gonza@gmail.com");

            UserRoleDTO customerRole = UserRoleDTO.builder().role("CUSTOMER").id(userLogged.getId()).build();
            userLogged.setRoles(List.of(customerRole));

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
        void unauthorizedWhenTokenIsNotValid() {
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
        void unauthorizedWhenTokenNotExist() {

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
