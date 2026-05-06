package com.ohpen.mo_ha.service.notification;

import com.ohpen.mo_ha.domain.ConfigChange;

public interface NotificationService {
    void notifyCriticalChange(ConfigChange change);
}