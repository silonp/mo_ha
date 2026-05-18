package com.ohpen.mo_ha.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ConfigChangeTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Instant TS = Instant.parse("2026-03-01T10:00:00Z");

    @Test
    void rejectsNullId() {
        assertThatThrownBy(() -> new ConfigChange(
                null, "k", ConfigChangeType.UPDATE, Severity.NON_CRITICAL,
                "old", "new", "peter", "r", TS))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("'id'");
    }

    @Test
    void rejectsNullType() {
        assertThatThrownBy(() -> new ConfigChange(
                ID, "k", null, Severity.NON_CRITICAL,
                "old", "new", "peter", "r", TS))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("'type'");
    }

    @Test
    void rejectsNullSeverity() {
        assertThatThrownBy(() -> new ConfigChange(
                ID, "k", ConfigChangeType.UPDATE, null,
                "old", "new", "peter", "r", TS))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("'severity'");
    }

    @Test
    void rejectsNullTimestamp() {
        assertThatThrownBy(() -> new ConfigChange(
                ID, "k", ConfigChangeType.UPDATE, Severity.NON_CRITICAL,
                "old", "new", "peter", "r", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("'timestamp'");
    }

    @Test
    void rejectsBlankConfigKey() {
        assertThatThrownBy(() -> new ConfigChange(
                ID, "  ", ConfigChangeType.UPDATE, Severity.NON_CRITICAL,
                "old", "new", "peter", "r", TS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'configKey'");
    }

    @Test
    void rejectsNullConfigKey() {
        assertThatThrownBy(() -> new ConfigChange(
                ID, null, ConfigChangeType.UPDATE, Severity.NON_CRITICAL,
                "old", "new", "peter", "r", TS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'configKey'");
    }

    @Test
    void rejectsBlankChangedBy() {
        assertThatThrownBy(() -> new ConfigChange(
                ID, "k", ConfigChangeType.UPDATE, Severity.NON_CRITICAL,
                "old", "new", "", "r", TS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'changedBy'");
    }

    @Test
    void createAcceptsNullPreviousValue() {
        assertThatCode(() -> new ConfigChange(
                ID, "k", ConfigChangeType.CREATE, Severity.NON_CRITICAL,
                null, "new", "peter", "r", TS))
                .doesNotThrowAnyException();
    }

    @Test
    void createRejectsNonNullPreviousValue() {
        assertThatThrownBy(() -> new ConfigChange(
                ID, "k", ConfigChangeType.CREATE, Severity.NON_CRITICAL,
                "old", "new", "peter", "r", TS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CREATE");
    }

    @Test
    void createRejectsNullNewValue() {
        assertThatThrownBy(() -> new ConfigChange(
                ID, "k", ConfigChangeType.CREATE, Severity.NON_CRITICAL,
                null, null, "peter", "r", TS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CREATE");
    }

    @Test
    void updateRequiresPreviousValue() {
        assertThatThrownBy(() -> new ConfigChange(
                ID, "k", ConfigChangeType.UPDATE, Severity.NON_CRITICAL,
                null, "new", "peter", "r", TS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UPDATE");
    }

    @Test
    void updateRequiresNewValue() {
        assertThatThrownBy(() -> new ConfigChange(
                ID, "k", ConfigChangeType.UPDATE, Severity.NON_CRITICAL,
                "old", null, "peter", "r", TS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UPDATE");
    }

    @Test
    void deleteAcceptsNullNewValue() {
        assertThatCode(() -> new ConfigChange(
                ID, "k", ConfigChangeType.DELETE, Severity.NON_CRITICAL,
                "old", null, "peter", "r", TS))
                .doesNotThrowAnyException();
    }

    @Test
    void deleteRejectsNullPreviousValue() {
        assertThatThrownBy(() -> new ConfigChange(
                ID, "k", ConfigChangeType.DELETE, Severity.NON_CRITICAL,
                null, null, "peter", "r", TS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DELETE");
    }

    @Test
    void deleteRejectsNonNullNewValue() {
        assertThatThrownBy(() -> new ConfigChange(
                ID, "k", ConfigChangeType.DELETE, Severity.NON_CRITICAL,
                "old", "new", "peter", "r", TS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DELETE");
    }
}