package ru.intertrust.cm.globalcacheclient;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObjectsModification;
import ru.intertrust.cm.globalcache.api.GroupAccessChanges;
import ru.intertrust.cm.globalcache.api.PersonAccessChanges;

/**
 * @author Denis Mitavskiy
 *         Date: 22.07.2015
 *         Time: 14:17
 */
public class PerPersonGlobalCacheClient extends PerGroupGlobalCacheClient {
    @Autowired
    private PersonAccessHelper personAccessHelper;

    @Autowired
    private GlobalCacheSettings settings;

    @Override
    public void notifyCommit(DomainObjectsModification modification) {
        String transactionId = modification.getTransactionId();
        GroupAccessChanges groupAccessChanges = createAccessChangesIfAbsent(transactionId);
        clearTransactionChanges(transactionId);

        if (modification.isEmpty() && groupAccessChanges.getObjectsQty() == 0) {
            return;
        }

        globalCache.notifyCommit(modification, getPersonAccessChanges(groupAccessChanges));
        if (settings.isInCluster()) {
            clusterSynchronizer.notifyCommit(modification, groupAccessChanges);
        }
    }

    private PersonAccessChanges getPersonAccessChanges(GroupAccessChanges groupAccessChanges) {
        return personAccessHelper.getPersonAccessChanges(groupAccessChanges);
    }
}
