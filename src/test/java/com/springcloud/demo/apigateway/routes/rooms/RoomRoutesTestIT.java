package com.springcloud.demo.apigateway.routes.rooms;

import com.fasterxml.jackson.core.JsonProcessingException;
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
public class RoomRoutesTestIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserClient userClient;

    @Autowired
    private JwtUtils jwtUtils;

    UserDTO userLogged;
    String uri;

    @Nested
    class CreateRoomRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/rooms";
        }

        @Test
        void createRoomRoute() {
            String token = jwtUtils.generateToken("gonza@gmail.com");
            Map<String, Object> requestBody = Map.of("name", "Sala 1");

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
            Map<String, Object> requestBody = Map.of("name", "Sala 1");

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
            Map<String, Object> requestBody = Map.of("name", "Sala 1");

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
            Map<String, Object> requestBody = Map.of("name", "Sala 1");

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
    class GetRoomRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/rooms";
        }

        @Test
        void getRooms() {
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
    class GetRoomByIdRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/rooms/" + UUID.randomUUID();
        }

        @Test
        void getRoomById() {
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
    class UpdateRoomRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/rooms/" + UUID.randomUUID();
        }

        @Test
        void updateRoomByOwner() {
            String token = jwtUtils.generateToken("gonza@gmail.com");
            Map<String, Object> requestBody = Map.of("name", "Sala 2");

            UserRoleDTO ownerRole = UserRoleDTO.builder().role("OWNER").id(userLogged.getId()).build();
            userLogged.setRoles(List.of(ownerRole));

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
                    .bodyValue(requestBody)
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        void unauthorizedWhenUserLoggedIsCustomer() {
            String token = jwtUtils.generateToken("gonza@gmail.com");
            Map<String, Object> requestBody = Map.of("name", "Sala 2");

            UserRoleDTO customerRole = UserRoleDTO.builder().role("CUSTOMER").id(userLogged.getId()).build();
            userLogged.setRoles(List.of(customerRole));

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
                    .bodyValue(requestBody)
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        void unauthorizedWhenTokenIsNotValid() {
            String token = "invalid_token";
            Map<String, Object> requestBody = Map.of("name", "Sala 2");

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
                    .bodyValue(requestBody)
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        void unauthorizedWhenTokenNotExist() {
            Map<String, Object> requestBody = Map.of("name", "Sala 2");

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
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }
    }

    @Nested
    class DeleteRoomRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/rooms/" + UUID.randomUUID();
        }

        @Test
        void deleteRoomByOwner() {
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