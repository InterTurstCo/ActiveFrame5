package ru.intertrust.cm.globalcache.impl.localjvm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectsModification;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.dto.CollectionTypesKey;
import ru.intertrust.cm.globalcache.api.AccessChanges;
import ru.intertrust.cm.globalcache.api.CollectionSubKey;

public class StampedSynchronizedGlobalCacheImpl extends GlobalCacheImpl {
    private static final Logger logger = LoggerFactory.getLogger(StampedSynchronizedGlobalCacheImpl.class);
    StampedLock stampedlock = new StampedLock();

    private void checkDeadLock() {
        if (logger.isTraceEnabled()) {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            for (int i = 3; i < stack.length; i++) {
                if (stack[i].getClassName().equals(StampedSynchronizedGlobalCacheImpl.class.getName())) {
                    logger.trace("Detect deadlock candidate", new Exception("Detect deadlock candidate"));
                }
            }
        }
    }

    @Override
    public void activate() {
        logger.trace("Start activate");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.activate();
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End activate");
        }
    }

    @Override
    public void deactivate() {
        logger.trace("Start deactivate");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.deactivate();
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End deactivate");
        }
    }

    @Override
    public void clear() {
        logger.trace("Start clear");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.clear();
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End clear");
        }
    }

    @Override
    public void notifyRead(String transactionId, Id id, DomainObject obj, AccessToken accessToken) {
        logger.trace("Start notifyRead");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.notifyRead(transactionId, id, obj, accessToken);
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End notifyRead");
        }
    }

    @Override
    public void notifyRead(String transactionId, Collection<DomainObject> objects, AccessToken accessToken) {
        logger.trace("Start notifyRead");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.notifyRead(transactionId, objects, accessToken);
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End notifyRead");
        }
    }

    @Override
    public void clearAccessLog() {
        logger.trace("Start clearAccessLog");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.clearAccessLog();
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End clearAccessLog");
        }
    }

    @Override
    protected void evictObjectAndCorrespondingEntries(Id id) {
        logger.trace("Start evictObjectAndCorrespondingEntries");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.evictObjectAndCorrespondingEntries(id);
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End evictObjectAndCorrespondingEntries");
        }
    }

    @Override
    protected void evictObjectAndCorrespondingEntries(DomainObject domainObject) {
        logger.trace("Start evictObjectAndCorrespondingEntries");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.evictObjectAndCorrespondingEntries(domainObject);
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End evictObjectAndCorrespondingEntries");
        }
    }

    @Override
    public void notifyReadByUniqueKey(String transactionId, String type, Map<String, Value> uniqueKey, DomainObject obj, long time, AccessToken accessToken) {
        logger.trace("Start notifyReadByUniqueKey");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.notifyReadByUniqueKey(transactionId, type, uniqueKey, obj, time, accessToken);
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End notifyReadByUniqueKey");
        }
    }

    @Override
    public void notifyReadPossiblyNullObjects(String transactionId, Collection<Pair<Id, DomainObject>> idsAndObjects, AccessToken accessToken) {
        logger.trace("Start notifyReadPossiblyNullObjects");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.notifyReadPossiblyNullObjects(transactionId, idsAndObjects, accessToken);
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End notifyReadPossiblyNullObjects");
        }
    }

    @Override
    public DomainObject getDomainObject(String transactionId, String type, Map<String, Value> uniqueKey, AccessToken accessToken) {
        logger.trace("Start getDomainObject");
        checkDeadLock();
        long stamp = stampedlock.readLock();
        try {
            return super.getDomainObject(transactionId, type, uniqueKey, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
            logger.trace("End getDomainObject");
        }
    }

    @Override
    public void notifyLinkedObjectsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, List<DomainObject> linkedObjects,
            long time, AccessToken accessToken) {
        logger.trace("Start notifyLinkedObjectsRead");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.notifyLinkedObjectsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjects, time, accessToken);
        } finally {
            stampedlock.unlock(lock);
        }
        logger.trace("End notifyLinkedObjectsRead");
    }

    @Override
    public void notifyLinkedObjectsIdsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, List<Id> linkedObjectsIds,
            long time, AccessToken accessToken) {
        logger.trace("Start notifyLinkedObjectsIdsRead");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.notifyLinkedObjectsIdsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjectsIds, time, accessToken);
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End notifyLinkedObjectsIdsRead");
        }
    }

    @Override
    public void invalidateUserAccess(UserSubject subject) {
        logger.trace("Start invalidateUserAccess");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.invalidateUserAccess(subject);
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End invalidateUserAccess");
        }
    }

    @Override
    public void notifyCommit(DomainObjectsModification modification, AccessChanges accessChanges) {
        logger.trace("Start notifyCommit");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.notifyCommit(modification, accessChanges);
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End notifyCommit");
        }
    }

    @Override
    public DomainObject getDomainObject(String transactionId, Id id, AccessToken accessToken) {
        logger.trace("Start getDomainObject");
        checkDeadLock();
        long stamp = stampedlock.readLock();
        try {
            return super.getDomainObject(transactionId, id, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
            logger.trace("End getDomainObject");
        }
    }

    @Override
    public ArrayList<DomainObject> getDomainObjects(String transactionId, Collection<Id> ids, AccessToken accessToken) {
        logger.trace("Start getDomainObjects");
        checkDeadLock();
        long stamp = stampedlock.readLock();
        try {
            return super.getDomainObjects(transactionId, ids, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
            logger.trace("End getDomainObjects");
        }
    }

    @Override
    public List<DomainObject> getLinkedDomainObjects(String transactionId, Id domainObjectId, String linkedType, String linkedField, boolean exactType,
            AccessToken accessToken) {
        logger.trace("Start getLinkedDomainObjects");
        checkDeadLock();
        long stamp = stampedlock.readLock();
        try {
            return super.getLinkedDomainObjects(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
            logger.trace("End getLinkedDomainObjects");
        }
    }

    @Override
    public void notifyCollectionCountRead(String transactionId, String name, Set<String> domainObjectTypes, Set<String> filterNames,
            List<? extends Filter> filterValues, int count, long time, AccessToken accessToken) {
        logger.trace("Start notifyCollectionCountRead");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.notifyCollectionCountRead(transactionId, name, domainObjectTypes, filterNames, filterValues, count, time, accessToken);
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End notifyCollectionCountRead");
        }
    }

    @Override
    protected void notifyCollectionRead(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes,
            IdentifiableObjectCollection clonedCollection, int count, long time, AccessToken accessToken) {
        logger.trace("Start notifyCollectionRead");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.notifyCollectionRead(key, subKey, domainObjectTypes, clonedCollection, count, time, accessToken);
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End notifyCollectionRead");
        }
    }

    @Override
    protected int getCollectionCount(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes, AccessToken accessToken) {
        logger.trace("Start getCollectionCount");
        checkDeadLock();
        long stamp = stampedlock.readLock();
        try {
            return super.getCollectionCount(key, subKey, domainObjectTypes, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
            logger.trace("End getCollectionCount");
        }
    }

    @Override
    protected IdentifiableObjectCollection getCollection(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes,
            AccessToken accessToken) {
        logger.trace("Start getCollection");
        checkDeadLock();
        long stamp = stampedlock.readLock();
        try {
            return super.getCollection(key, subKey, domainObjectTypes, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
            logger.trace("End getCollection");
        }
    }

    @Override
    public void setSizeLimitBytes(long bytes) {
        logger.trace("Start setSizeLimitBytes");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.setSizeLimitBytes(bytes);
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End setSizeLimitBytes");
        }
    }

    @Override
    public long getSizeLimitBytes() {
        logger.trace("Start getSizeLimitBytes");
        checkDeadLock();
        long stamp = stampedlock.readLock();
        try {
            return super.getSizeLimitBytes();
        } finally {
            stampedlock.unlockRead(stamp);
            logger.trace("End getSizeLimitBytes");
        }
    }

    @Override
    public void notifyReadAll(String transactionId, String type, boolean exactType, Collection<DomainObject> objects, AccessToken accessToken) {
        logger.trace("Start notifyReadAll");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.notifyReadAll(transactionId, type, exactType, objects, accessToken);
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End notifyReadAll");
        }
    }

    @Override
    public List<DomainObject> getAllDomainObjects(String transactionId, String type, boolean exactType, AccessToken accessToken) {
        logger.trace("Start getAllDomainObjects");
        checkDeadLock();
        long stamp = stampedlock.readLock();
        try {
            return super.getAllDomainObjects(transactionId, type, exactType, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
            logger.trace("End getAllDomainObjects");
        }
    }

    @Override
    public List<Id> getLinkedDomainObjectsIds(String transactionId, Id domainObjectId, String linkedType, String linkedField, boolean exactType,
            AccessToken accessToken) {
        logger.trace("Start getLinkedDomainObjectsIds");
        checkDeadLock();
        long stamp = stampedlock.readLock();
        try {
            return super.getLinkedDomainObjectsIds(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
            logger.trace("End getLinkedDomainObjectsIds");
        }
    }

    @Override
    protected void deleteEldestEntry() {
        logger.trace("Start deleteEldestEntry");
        checkDeadLock();
        long lock = stampedlock.writeLock();
        try {
            super.deleteEldestEntry();
        } finally {
            stampedlock.unlock(lock);
            logger.trace("End deleteEldestEntry");
        }
    }
}
