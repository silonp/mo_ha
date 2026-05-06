package com.ohpen.mo_ha.domain;

import java.time.Instant;

public record ConfigChangeFilter(Instant from, Instant to, ConfigChangeType type) {
    // Get all!
    public static ConfigChangeFilter empty() {
        return new ConfigChangeFilter(null, null, null);
    }
}