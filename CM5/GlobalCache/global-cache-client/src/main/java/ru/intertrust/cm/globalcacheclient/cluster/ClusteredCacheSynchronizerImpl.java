package ru.intertrust.cm.globalcacheclient.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.dto.DomainObjectsModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.globalcache.api.CacheInvalidation;
import ru.intertrust.cm.globalcache.api.GroupAccessChanges;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Denis Mitavskiy
 *         Date: 05.08.2015
 *         Time: 19:12
 */
@Stateless(name = "ClusteredCacheSynchronizer")
@TransactionManagement(TransactionManagementType.CONTAINER)
@Local(ClusteredCacheSynchronizer.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ClusteredCacheSynchronizerImpl implements ClusteredCacheSynchronizer {
    final static Logger logger = LoggerFactory.getLogger(ClusteredCacheSynchronizerImpl.class);

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void notifyCommit(DomainObjectsModification modification, GroupAccessChanges groupAccessChanges) {
        long t1 = System.nanoTime();
        final boolean clearFullAccessLog = groupAccessChanges.clearFullAccessLog();
        final HashMap<Id, HashMap<Id, Boolean>> groupAccessByObject = groupAccessChanges.getGroupAccessByObject();
        final Set<Id> changedAccessIds = groupAccessByObject == null ? null : groupAccessByObject.keySet();
        final List<Id> createdIds = modification.getCreatedIds();
        final Set<Id> savedAndChangedStatusIds = modification.getSavedAndChangedStatusIds();
        final Set<Id> deletedIds = modification.getDeletedIds();
        final HashSet<Id> ids = new HashSet<>((int) (1.5 * (createdIds.size() + changedAccessIds.size() + savedAndChangedStatusIds.size() + deletedIds.size())));
        ids.addAll(createdIds);
        ids.addAll(changedAccessIds);
        ids.addAll(savedAndChangedStatusIds);
        ids.addAll(deletedIds);

        GlobalCacheJmsSender.sendClusterNotification(new CacheInvalidation(ids, clearFullAccessLog));
        long t2 = System.nanoTime();
        logger.warn("Message sent in: " + (t2 - t1) / 1000000.0 + "ms");
    }
}
