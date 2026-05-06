package com.ohpen.mo_ha.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

// Entity
public record ConfigChange(
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

    public ConfigChange {
        Objects.requireNonNull(id, "'id' must not be null");
        Objects.requireNonNull(type, "'type' must not be null");
        Objects.requireNonNull(severity, "'severity' must not be null");
        Objects.requireNonNull(timestamp, "'timestamp' must not be null");

        if (configKey == null || configKey.isBlank()) {
            throw new IllegalArgumentException("'configKey' must not be blank");
        }
        if (changedBy == null || changedBy.isBlank()) {
            throw new IllegalArgumentException("'changedBy' must not be blank");
        }

        if (type == ConfigChangeType.CREATE) {
            if (previousValue != null) {
                throw new IllegalArgumentException("CREATE must not have a previousValue");
            }
            if (newValue == null) {
                throw new IllegalArgumentException("CREATE must have a newValue");
            }
        } else if (type == ConfigChangeType.UPDATE) {
            if (previousValue == null) {
                throw new IllegalArgumentException("UPDATE must have a previousValue");
            }
            if (newValue == null) {
                throw new IllegalArgumentException("UPDATE must have a newValue");
            }
        } else if (type == ConfigChangeType.DELETE) {
            if (previousValue == null) {
                throw new IllegalArgumentException("DELETE must have a previousValue");
            }
            if (newValue != null) {
                throw new IllegalArgumentException("DELETE must not have a newValue");
            }
        }
    }
}