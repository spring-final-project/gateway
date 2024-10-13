package com.springcloud.demo.apigateway.routes.bookings;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 9090)
public class BookingRoutesTestIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private UserClient userClient;

    UserDTO userLogged;
    String uri;

    @Nested
    class CreateBookingRoute {
        Map<String, Object> requestBody;

        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/bookings";
            requestBody = Map.of(
                    "checkIn", LocalDateTime.now().toString(),
                    "checkOut", LocalDateTime.now().plusDays(1).toString(),
                    "roomId", UUID.randomUUID().toString()
            );
        }

        @Test
        void createBookingRouteByCustomer() {
            String token = jwtUtils.generateToken("gonza@gmail.com");

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
        void unauthorizedCreateBookingRouteByOwner() {
            String token = jwtUtils.generateToken("gonza@gmail.com");

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
        void unauthorizedCreateBookingWhenTokenIsNotValid() {
            String token = "invalid_token";

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
        void unauthorizedCreateBookingWhenTokenNotExist() {
            given(userClient.findByEmail(anyString())).willReturn(Mono.empty());
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
    class GetBookingRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/bookings";
        }

        @Test
        void getBookingRoute() {
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
            given(userClient.findByEmail(anyString())).willReturn(Mono.empty());
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
    class GetBookingByIdRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/bookings/" + UUID.randomUUID();
        }

        @Test
        void getBookingByIdRoute() {
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
            given(userClient.findByEmail(anyString())).willReturn(Mono.empty());
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
    class UpdateStatusBookingRoute {
        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/bookings/" + UUID.randomUUID();
        }

        @Test
        void updateStatusBookingByOwner() {
            String token = jwtUtils.generateToken("gonza@gmail.com");
            Map<String, Object> requestBody = Map.of("status", "DELIVERED");

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
        void unauthorizedUpdateStatusBookingByCustomer() {
            String token = jwtUtils.generateToken("gonza@gmail.com");
            Map<String, Object> requestBody = Map.of("status", "DELIVERED");

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
        void unauthorizedUpdateStatusWhenTokenIsNotValid() {
            String token = "invalid_token";
            Map<String, Object> requestBody = Map.of("status", "DELIVERED");

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
        void unauthorizedUpdateStatusWhenTokenNotExist() {
            Map<String, Object> requestBody = Map.of("status", "DELIVERED");

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
    class ReviewRoute {
        Map<String, Object> requestBody;

        @BeforeEach
        void setup() {
            userLogged = UserDTO.builder()
                    .id(UUID.randomUUID())
                    .email("gonza@gmail.com")
                    .password("Abcd1234")
                    .build();
            uri = "/api/bookings/" + UUID.randomUUID() + "/review";
            requestBody = Map.of("review", "Very clean", "rating", "8");
        }

        @Test
        void createReviewByCustomer(){
            String token = jwtUtils.generateToken("gonza@gmail.com");

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
        void unauthorizedCreateReviewByOwner(){
            String token = jwtUtils.generateToken("gonza@gmail.com");

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
        void unauthorizedCreateReviewWhenTokenIsNotValid(){
            String token = "invalid_token";

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
        void unauthorizedCreateReviewWhenTokenNotExist(){
            given(userClient.findByEmail(anyString())).willReturn(Mono.empty());
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
}
