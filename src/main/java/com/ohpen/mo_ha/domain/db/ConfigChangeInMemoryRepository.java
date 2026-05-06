package com.ohpen.mo_ha.domain.db;

import com.ohpen.mo_ha.domain.ConfigChange;
import com.ohpen.mo_ha.domain.ConfigChangeFilter;
import com.ohpen.mo_ha.domain.ConfigChangeRepository;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class ConfigChangeInMemoryRepository implements ConfigChangeRepository {

    private final ConcurrentMap<UUID, ConfigChange> store = new ConcurrentHashMap<>();

    @Override
    public ConfigChange save(ConfigChange change) {
        store.put(change.id(), change);
        return change;
    }

    @Override
    public Optional<ConfigChange> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ConfigChange> findAll(ConfigChangeFilter filter) {
        return store.values().stream()
                .filter(change -> isWithinTimeRange(change, filter))
                .filter(change -> isMatchingType(change, filter))
                .toList();
    }

    private boolean isWithinTimeRange(ConfigChange change, @Nonnull ConfigChangeFilter filter) {
        if (filter.from() != null && change.timestamp().isBefore(filter.from())) {
            return false;
        }
        return filter.to() == null || !change.timestamp().isAfter(filter.to());
    }

    private boolean isMatchingType(ConfigChange change, @Nonnull ConfigChangeFilter filter) {
        return filter.type() == null || change.type() == filter.type();
    }
}