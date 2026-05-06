package com.ohpen.mo_ha.service.notification;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class NotificationServiceHealthIndicator implements HealthIndicator {

    private final NotificationService notificationService;

    public NotificationServiceHealthIndicator(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public Health health() {
        return notificationService.isAvailable() ? Health.up().build() : Health.down().build();
    }
}