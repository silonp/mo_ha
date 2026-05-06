package com.ohpen.mo_ha.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConfigChangeRepository {

    ConfigChange save(ConfigChange change);

    Optional<ConfigChange> findById(UUID id);

    List<ConfigChange> findAll(ConfigChangeFilter filter);
}
