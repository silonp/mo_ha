package com.ohpen.mo_ha.service;

import com.ohpen.mo_ha.domain.ConfigChangeType;
import com.ohpen.mo_ha.domain.Severity;

// This record serves a purpose to decouple Web, Service and Persistency layers.
public record RecordChangeCommand(
        String configKey,
        ConfigChangeType type,
        Severity severity,
        String previousValue,
        String newValue,
        String changedBy,
        String reason
) {
}