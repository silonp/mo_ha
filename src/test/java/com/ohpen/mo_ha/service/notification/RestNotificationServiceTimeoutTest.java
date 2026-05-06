package com.ohpen.mo_ha.service.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RestNotificationServiceTimeoutTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Value("${notification.rest.connect-timeout}")
    private Duration connectTimeout;

    @Value("${notification.rest.read-timeout}")
    private Duration readTimeout;

    @Test
    void isAvailableReturnsFalseWhenStubTimesOut() {
        String base = "http://localhost:" + port + "/internal/notifications/stub/slow";
        RestNotificationService service = new RestNotificationService(
                restClientBuilder, base, base + "/health", connectTimeout, readTimeout);

        long start = System.nanoTime();
        boolean available = service.isAvailable();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(available).isFalse();
        // notification.rest.read-timeout=PT0.2S
        // notification.rest.stub.delay=PT2S
        // Must fire well before stub responds.
        assertThat(elapsedMs).isLessThan(1500);
    }
}