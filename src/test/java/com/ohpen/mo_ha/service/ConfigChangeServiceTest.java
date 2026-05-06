package com.ohpen.mo_ha.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ohpen.mo_ha.domain.ConfigChange;
import com.ohpen.mo_ha.domain.ConfigChangeFilter;
import com.ohpen.mo_ha.domain.ConfigChangeType;
import com.ohpen.mo_ha.domain.Severity;
import com.ohpen.mo_ha.domain.db.ConfigChangeInMemoryRepository;
import com.ohpen.mo_ha.service.notification.NotificationService;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class ConfigChangeServiceTest {

    private Clock clock;
    private ConfigChangeService service;

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        service = new ConfigChangeService(
                new ConfigChangeInMemoryRepository(),
                mock(NotificationService.class),
                new SimpleMeterRegistry(),
                clock
        );
    }

    @Test
    void findAllFiltersByFromAndTo() {
        ConfigChange before = record("before", Instant.parse("2026-01-01T00:00:00Z"));
        ConfigChange inside = record("inside", Instant.parse("2026-03-01T00:00:00Z"));
        ConfigChange after = record("after", Instant.parse("2026-06-01T00:00:00Z"));

        ConfigChangeFilter filter = new ConfigChangeFilter(
                Instant.parse("2026-02-01T00:00:00Z"),
                Instant.parse("2026-04-01T00:00:00Z"),
                null
        );

        assertThat(service.findAll(filter))
                .containsExactly(inside)
                .doesNotContain(before, after);
    }

    @Test
    void findAllFiltersByFromAndToWithHourMinute() {
        ConfigChange tooEarly = record("tooEarly", Instant.parse("2026-03-01T10:14:59Z"));
        ConfigChange justIn = record("justIn", Instant.parse("2026-03-01T10:15:00Z"));
        ConfigChange middle = record("middle", Instant.parse("2026-03-01T10:42:30Z"));
        ConfigChange justOut = record("justOut", Instant.parse("2026-03-01T11:30:01Z"));

        ConfigChangeFilter filter = new ConfigChangeFilter(
                Instant.parse("2026-03-01T10:15:00Z"),
                Instant.parse("2026-03-01T11:30:00Z"),
                null
        );

        assertThat(service.findAll(filter))
                .containsExactlyInAnyOrder(justIn, middle)
                .doesNotContain(tooEarly, justOut);
    }

    @Test
    void findAllWithEmptyFilterReturnsAll() {
        ConfigChange a = record("a", Instant.parse("2026-01-01T00:00:00Z"));
        ConfigChange b = record("b", Instant.parse("2026-03-01T12:30:00Z"));
        ConfigChange c = record("c", Instant.parse("2026-06-01T23:59:59Z"));

        assertThat(service.findAll(ConfigChangeFilter.empty()))
                .containsExactlyInAnyOrder(a, b, c);
    }

    private ConfigChange record(String configKey, Instant timestamp) {
        when(clock.instant()).thenReturn(timestamp);
        return service.record(new RecordChangeCommand(
                configKey,
                ConfigChangeType.UPDATE,
                Severity.NON_CRITICAL,
                "old",
                "new",
                "peter",
                "test"
        ));
    }
}