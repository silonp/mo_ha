package com.ohpen.mo_ha.rest.dto;

import com.ohpen.mo_ha.domain.ConfigChangeType;
import com.ohpen.mo_ha.domain.Severity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// TODO: Review validation rules.
public record ConfigChangeRequest(
        @NotBlank @Size(max = 200) String configKey,
        @NotNull ConfigChangeType type,
        @NotNull Severity severity,
        @Size(max = 4096) String previousValue,
        @Size(max = 4096) String newValue,
        @NotBlank @Size(max = 200) String changedBy,
        @Size(max = 1000) String reason
) {
}