package ru.intertrust.cm.globalcacheclient.cluster;

import ru.intertrust.cm.core.business.api.dto.DomainObjectsModification;
import ru.intertrust.cm.globalcache.api.PersonAccessChanges;

/**
 * @author Denis Mitavskiy
 *         Date: 21.04.2016
 *         Time: 17:24
 */
public interface ClusteredCacheSynchronizer {
    void notifyCommit(DomainObjectsModification modification, PersonAccessChanges groupAccessChanges);

    void notifyClear();
}
