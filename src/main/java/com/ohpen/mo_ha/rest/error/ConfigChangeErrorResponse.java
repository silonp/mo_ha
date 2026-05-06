package com.ohpen.mo_ha.rest.error;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

// Include only non-null fields.
// Specially 'fieldErrors' can be often null.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConfigChangeErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String traceId,
        Map<String, String> fieldErrors
) {
}