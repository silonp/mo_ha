package com.ohpen.mo_ha.rest;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ohpen.mo_ha.domain.ConfigChange;
import com.ohpen.mo_ha.domain.ConfigChangeFilter;
import com.ohpen.mo_ha.domain.ConfigChangeType;
import com.ohpen.mo_ha.service.ConfigChangeService;
import com.ohpen.mo_ha.service.RecordChangeCommand;
import com.ohpen.mo_ha.rest.dto.ConfigChangeResponse;
import com.ohpen.mo_ha.rest.dto.ConfigChangeRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/config-changes")
public class ConfigChangeController {

    private final ConfigChangeService service;

    @Autowired
    public ConfigChangeController(ConfigChangeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ConfigChangeResponse> create(@Valid @RequestBody ConfigChangeRequest request) {
        RecordChangeCommand command = new RecordChangeCommand(
                request.configKey(),
                request.type(),
                request.severity(),
                request.previousValue(),
                request.newValue(),
                request.changedBy(),
                request.reason()
        );
        ConfigChange saved = service.record(command);
        return ResponseEntity
                .created(URI.create("/config-changes/" + saved.id()))
                .body(ConfigChangeResponse.from(saved));
    }

    @GetMapping
    public List<ConfigChangeResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) ConfigChangeType type) {
        return service.findAll(new ConfigChangeFilter(from, to, type)).stream()
                .map(ConfigChangeResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ConfigChangeResponse get(@PathVariable UUID id) {
        return ConfigChangeResponse.from(service.findById(id));
    }
}