package com.springcloud.demo.apigateway.routes.auth;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.springcloud.demo.apigateway.client.users.dto.UserDTO;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 9090)
public class AuthRoutesTestIT {

    @Autowired
    private WebTestClient webTestClient;

    String uri;

    @Nested
    class LoginWithEmailRoutes {
        Map<String, String> loginRequestBody;

        @BeforeEach
        void setUp() {
            uri = "/api/auth/login";
            loginRequestBody = Map.of("email", "test@test.com", "password", "test");
        }

        @Test
        void loginUserWithEmail(){

            stubFor(post(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient.post().uri(uri)
                    .bodyValue(loginRequestBody)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectHeader().value("Authorization", Matchers.notNullValue());
        }

        @Test
        void loginUserWithEmailAndWrongPassword(){
            stubFor(post(urlEqualTo(uri))
                    .willReturn(aResponse()
                            .withStatus(400)
                            .withHeader("Content-Type", "application/json")
                    )
            );

            webTestClient.post().uri(uri)
                    .bodyValue(loginRequestBody)
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectHeader().doesNotExist("Authorization");
        }
    }
}
