package com.ohpen.mo_ha.rest;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final Duration stubDelay;

    public NotificationController(@Value("${notification.rest.stub.delay:PT0S}") Duration stubDelay) {
        this.stubDelay = stubDelay;
    }

    @GetMapping("/stub/health")
    public ResponseEntity<Void> health() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stub")
    public ResponseEntity<Void> receive(@RequestBody Map<String, Object> payload) {
        log.info("Received notification: configKey={} type={} id={}",
                payload.get("configKey"),
                payload.get("type"),
                payload.get("id"));
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/stub/slow/health")
    public ResponseEntity<Void> slowHealth() throws InterruptedException {
        delay();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stub/slow")
    public ResponseEntity<Void> slowReceive(@RequestBody Map<String, Object> payload) throws InterruptedException {
        delay();
        return ResponseEntity.accepted().build();
    }

    private void delay() throws InterruptedException {
        if (!stubDelay.isZero() && !stubDelay.isNegative()) {
            Thread.sleep(stubDelay.toMillis());
        }
    }
}