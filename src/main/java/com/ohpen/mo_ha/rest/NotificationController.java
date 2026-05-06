package com.ohpen.mo_ha.rest;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    // TODO: Choose a response (or timeout).
    @PostMapping("/stub")
    public ResponseEntity<Void> receive(@RequestBody Map<String, Object> payload) {
        log.info("Received notification: configKey={} type={} id={}",
                payload.get("configKey"),
                payload.get("type"),
                payload.get("id"));
        return ResponseEntity.accepted().build();
    }
}