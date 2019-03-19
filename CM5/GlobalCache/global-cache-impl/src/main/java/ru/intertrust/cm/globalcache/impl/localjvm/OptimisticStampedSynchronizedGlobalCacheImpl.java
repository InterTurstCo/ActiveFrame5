package ru.intertrust.cm.globalcache.impl.localjvm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;

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

public class OptimisticStampedSynchronizedGlobalCacheImpl extends GlobalCacheImpl {
    StampedLock stampedlock = new StampedLock();

    @Override
    public void activate() {
        long lock = stampedlock.writeLock();
        try {
            super.activate();
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public void deactivate() {
        long lock = stampedlock.writeLock();
        try {
            super.deactivate();
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public void clear() {
        long lock = stampedlock.writeLock();
        try {
            super.clear();
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public void notifyRead(String transactionId, Id id, DomainObject obj, AccessToken accessToken) {
        long lock = stampedlock.writeLock();
        try {
            super.notifyRead(transactionId, id, obj, accessToken);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public void notifyRead(String transactionId, Collection<DomainObject> objects, AccessToken accessToken) {
        long lock = stampedlock.writeLock();
        try {
            super.notifyRead(transactionId, objects, accessToken);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public void clearAccessLog() {
        long lock = stampedlock.writeLock();
        try {
            super.clearAccessLog();
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    protected void evictObjectAndCorrespondingEntries(Id id) {
        long lock = stampedlock.writeLock();
        try {
            super.evictObjectAndCorrespondingEntries(id);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    protected void evictObjectAndCorrespondingEntries(DomainObject domainObject) {
        long lock = stampedlock.writeLock();
        try {
            super.evictObjectAndCorrespondingEntries(domainObject);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public void notifyReadByUniqueKey(String transactionId, String type, Map<String, Value> uniqueKey, DomainObject obj, long time, AccessToken accessToken) {
        long lock = stampedlock.writeLock();
        try {
            super.notifyReadByUniqueKey(transactionId, type, uniqueKey, obj, time, accessToken);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public void notifyReadPossiblyNullObjects(String transactionId, Collection<Pair<Id, DomainObject>> idsAndObjects, AccessToken accessToken) {
        long lock = stampedlock.writeLock();
        try {
            super.notifyReadPossiblyNullObjects(transactionId, idsAndObjects, accessToken);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public DomainObject getDomainObject(String transactionId, String type, Map<String, Value> uniqueKey, AccessToken accessToken) {
        long stamp = stampedlock.tryOptimisticRead();
        if (stamp != 0L) {
            DomainObject result = super.getDomainObject(transactionId, type, uniqueKey, accessToken);
            if (stampedlock.validate(stamp)) {
                return result;
            }
        }
        stamp = stampedlock.readLock();
        try {
            return super.getDomainObject(transactionId, type, uniqueKey, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
        }
    }

    @Override
    public void notifyLinkedObjectsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, List<DomainObject> linkedObjects,
            long time, AccessToken accessToken) {
        long lock = stampedlock.writeLock();
        try {
            super.notifyLinkedObjectsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjects, time, accessToken);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public void notifyLinkedObjectsIdsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, List<Id> linkedObjectsIds,
            long time, AccessToken accessToken) {
        long lock = stampedlock.writeLock();
        try {
            super.notifyLinkedObjectsIdsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjectsIds, time, accessToken);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public void invalidateUserAccess(UserSubject subject) {
        long lock = stampedlock.writeLock();
        try {
            super.invalidateUserAccess(subject);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public void notifyCommit(DomainObjectsModification modification, AccessChanges accessChanges) {
        long lock = stampedlock.writeLock();
        try {
            super.notifyCommit(modification, accessChanges);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public DomainObject getDomainObject(String transactionId, Id id, AccessToken accessToken) {
        long stamp = stampedlock.tryOptimisticRead();
        if (stamp != 0L) {
            DomainObject result = super.getDomainObject(transactionId, id, accessToken);
            if (stampedlock.validate(stamp)) {
                return result;
            }
        }
        stamp = stampedlock.readLock();
        try {
            return super.getDomainObject(transactionId, id, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
        }
    }

    @Override
    public ArrayList<DomainObject> getDomainObjects(String transactionId, Collection<Id> ids, AccessToken accessToken) {
        long stamp = stampedlock.tryOptimisticRead();
        if (stamp != 0L) {
            ArrayList<DomainObject> result = super.getDomainObjects(transactionId, ids, accessToken);
            if (stampedlock.validate(stamp)) {
                return result;
            }
        }
        stamp = stampedlock.readLock();
        try {
            return super.getDomainObjects(transactionId, ids, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
        }
    }

    @Override
    public List<DomainObject> getLinkedDomainObjects(String transactionId, Id domainObjectId, String linkedType, String linkedField, boolean exactType,
            AccessToken accessToken) {
        long stamp = stampedlock.tryOptimisticRead();
        if (stamp != 0L) {
            List<DomainObject> result = super.getLinkedDomainObjects(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
            if (stampedlock.validate(stamp)) {
                return result;
            }
        }
        stamp = stampedlock.readLock();
        try {
            return super.getLinkedDomainObjects(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
        }
    }

    @Override
    public void notifyCollectionCountRead(String transactionId, String name, Set<String> domainObjectTypes, Set<String> filterNames,
            List<? extends Filter> filterValues, int count, long time, AccessToken accessToken) {
        long lock = stampedlock.writeLock();
        try {
            super.notifyCollectionCountRead(transactionId, name, domainObjectTypes, filterNames, filterValues, count, time, accessToken);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    protected void notifyCollectionRead(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes,
            IdentifiableObjectCollection clonedCollection, int count, long time, AccessToken accessToken) {
        long lock = stampedlock.writeLock();
        try {
            super.notifyCollectionRead(key, subKey, domainObjectTypes, clonedCollection, count, time, accessToken);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    protected int getCollectionCount(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes, AccessToken accessToken) {
        long stamp = stampedlock.tryOptimisticRead();
        if (stamp != 0L) {
            int result = super.getCollectionCount(key, subKey, domainObjectTypes, accessToken);
            if (stampedlock.validate(stamp)) {
                return result;
            }
        }
        stamp = stampedlock.readLock();
        try {
            return super.getCollectionCount(key, subKey, domainObjectTypes, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
        }
    }

    @Override
    protected IdentifiableObjectCollection getCollection(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes,
            AccessToken accessToken) {
        long stamp = stampedlock.tryOptimisticRead();
        if (stamp != 0L) {
            IdentifiableObjectCollection result = super.getCollection(key, subKey, domainObjectTypes, accessToken);
            if (stampedlock.validate(stamp)) {
                return result;
            }
        }
        stamp = stampedlock.readLock();
        try {
            return super.getCollection(key, subKey, domainObjectTypes, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
        }
    }

    @Override
    public void setSizeLimitBytes(long bytes) {
        long lock = stampedlock.writeLock();
        try {
            super.setSizeLimitBytes(bytes);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public long getSizeLimitBytes() {
        long stamp = stampedlock.tryOptimisticRead();
        if (stamp != 0L) {
            long result = super.getSizeLimitBytes();
            if (stampedlock.validate(stamp)) {
                return result;
            }
        }
        stamp = stampedlock.readLock();
        try {
            return super.getSizeLimitBytes();
        } finally {
            stampedlock.unlockRead(stamp);
        }
    }

    @Override
    public void notifyReadAll(String transactionId, String type, boolean exactType, Collection<DomainObject> objects, AccessToken accessToken) {
        long lock = stampedlock.writeLock();
        try {
            super.notifyReadAll(transactionId, type, exactType, objects, accessToken);
        } finally {
            stampedlock.unlock(lock);
        }
    }

    @Override
    public List<DomainObject> getAllDomainObjects(String transactionId, String type, boolean exactType, AccessToken accessToken) {
        long stamp = stampedlock.tryOptimisticRead();
        if (stamp != 0L) {
            List<DomainObject> result = super.getAllDomainObjects(transactionId, type, exactType, accessToken);
            if (stampedlock.validate(stamp)) {
                return result;
            }
        }
        stamp = stampedlock.readLock();
        try {
            return super.getAllDomainObjects(transactionId, type, exactType, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
        }
    }

    @Override
    public List<Id> getLinkedDomainObjectsIds(String transactionId, Id domainObjectId, String linkedType, String linkedField, boolean exactType,
            AccessToken accessToken) {
        long stamp = stampedlock.tryOptimisticRead();
        if (stamp != 0L) {
            List<Id> result = super.getLinkedDomainObjectsIds(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
            if (stampedlock.validate(stamp)) {
                return result;
            }
        }
        stamp = stampedlock.readLock();
        try {
            return super.getLinkedDomainObjectsIds(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
        } finally {
            stampedlock.unlockRead(stamp);
        }
    }

    @Override
    protected void deleteEldestEntry() {
        long lock = stampedlock.writeLock();
        try {
            super.deleteEldestEntry();
        } finally {
            stampedlock.unlock(lock);
        }
    }
}
