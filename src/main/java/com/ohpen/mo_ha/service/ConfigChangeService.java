package com.ohpen.mo_ha.service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import com.ohpen.mo_ha.service.exceptions.ConfigChangeNotFoundException;
import org.springframework.stereotype.Service;

import com.ohpen.mo_ha.domain.ConfigChange;
import com.ohpen.mo_ha.domain.ConfigChangeFilter;
import com.ohpen.mo_ha.domain.ConfigChangeRepository;
import com.ohpen.mo_ha.domain.Severity;
import com.ohpen.mo_ha.service.notification.NotificationService;

import io.micrometer.core.instrument.MeterRegistry;

@Service
public class ConfigChangeService {

    private final ConfigChangeRepository repository;
    private final NotificationService notificationService;
    private final MeterRegistry meterRegistry;
    private final Clock clock;

    public ConfigChangeService(
            ConfigChangeRepository repository,
            NotificationService notificationService,
            MeterRegistry meterRegistry,
            Clock clock) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.meterRegistry = meterRegistry;
        this.clock = clock;
    }

    public ConfigChange record(RecordChangeCommand command) {
        ConfigChange change = new ConfigChange(
                UUID.randomUUID(),
                command.configKey(),
                command.type(),
                command.severity(),
                command.previousValue(),
                command.newValue(),
                command.changedBy(),
                command.reason(),
                clock.instant()
        );
        ConfigChange saved = repository.save(change);
        meterRegistry.counter(
                "config_changes_recorded_total",
                "severity", saved.severity().name(),
                "type", saved.type().name()
        ).increment();
        if (saved.severity() == Severity.CRITICAL) {
            notificationService.notifyCriticalChange(saved);
        }
        return saved;
    }

    public ConfigChange findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ConfigChangeNotFoundException(id));
    }

    public List<ConfigChange> findAll(ConfigChangeFilter filter) {
        return repository.findAll(filter);
    }
}