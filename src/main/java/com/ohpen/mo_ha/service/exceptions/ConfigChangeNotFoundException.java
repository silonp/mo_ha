package com.ohpen.mo_ha.service.exceptions;

import java.util.UUID;

public class ConfigChangeNotFoundException extends RuntimeException {
    public ConfigChangeNotFoundException(UUID id) {
        super(String.format("Configuration Change %s not found", id));
    }
}