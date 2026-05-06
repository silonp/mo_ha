package com.ohpen.mo_ha.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.ohpen.mo_ha.domain.ConfigChange;

@Service
public class RestNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(RestNotificationService.class);

    private final RestClient restClient;
    private final String url;

    public RestNotificationService(
            RestClient.Builder restClientBuilder,
            @Value("${notification.rest.url}") String url) {
        this.restClient = restClientBuilder.build();
        this.url = url;
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