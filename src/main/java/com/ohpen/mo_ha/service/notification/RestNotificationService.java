package com.ohpen.mo_ha.service.notification;

import java.net.http.HttpClient;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.ohpen.mo_ha.domain.ConfigChange;

@Service
public class RestNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(RestNotificationService.class);

    private final RestClient restClient;
    private final String url;
    private final String healthUrl;

    public RestNotificationService(
            RestClient.Builder restClientBuilder,
            @Value("${notification.rest.url}") String url,
            @Value("${notification.rest.health-url}") String healthUrl,
            @Value("${notification.rest.connect-timeout}") Duration connectTimeout,
            @Value("${notification.rest.read-timeout}") Duration readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);
        this.restClient = restClientBuilder.requestFactory(factory).build();
        this.url = url;
        this.healthUrl = healthUrl;
    }

    @Override
    // No Async as we need to display something to the user.
    public boolean isAvailable() {
        try {
            restClient.get().uri(healthUrl).retrieve().toBodilessEntity();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    @Async("restNotificationExecutor")
    public void notifyCriticalChange(ConfigChange change) {
        try {
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(change)
                    .retrieve()
                    .toBodilessEntity(); // No body, just status code.
            log.info("Sent critical-change notification for id={} configKey={}", change.id(), change.configKey());
        } catch (Exception ex) {
            log.error("Failed to send critical-change notification for id={} configKey={}: {}",
                    change.id(), change.configKey(), ex.getMessage(), ex);
        }
    }
}