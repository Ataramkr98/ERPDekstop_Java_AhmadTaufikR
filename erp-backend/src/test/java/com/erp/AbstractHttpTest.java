package com.erp;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;

import org.junit.jupiter.api.BeforeEach;

/**
 * Gives every HTTP test a realistic socket timeout.
 *
 * <p>On a cold, slow disk the embedded Quarkus instance can take minutes to augment,
 * and the first request arrives while Vert.x is still loading classes. RestAssured's
 * default socket timeout is therefore not a property of the application under test -
 * it is a property of the machine - so it is set generously here rather than left to
 * fail a legitimate request.
 */
abstract class AbstractHttpTest {

    protected static final int SOCKET_TIMEOUT_SECONDS = 300;

    @BeforeEach
    void configureHttpTimeouts() {
        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.socket.timeout", SOCKET_TIMEOUT_SECONDS * 1000)
                        .setParam("http.connection.timeout", 60_000));
    }
}