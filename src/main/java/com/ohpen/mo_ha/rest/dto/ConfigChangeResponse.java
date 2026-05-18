package com.ohpen.mo_ha.rest.dto;

import java.time.Instant;
import java.util.UUID;

import com.ohpen.mo_ha.domain.ConfigChange;
import com.ohpen.mo_ha.domain.ConfigChangeType;
import com.ohpen.mo_ha.domain.Severity;

public record ConfigChangeResponse(
        UUID id,
        String configKey,
        ConfigChangeType type,
        Severity severity,
        String previousValue,
        String newValue,
        String changedBy,
        String reason,
        Instant timestamp
) {

    public static ConfigChangeResponse from(ConfigChange change) {
        return new ConfigChangeResponse(
                change.id(),
                change.configKey(),
                change.type(),
                change.severity(),
                change.previousValue(),
                change.newValue(),
                change.changedBy(),
                change.reason(),
                change.timestamp()
        );
    }
}