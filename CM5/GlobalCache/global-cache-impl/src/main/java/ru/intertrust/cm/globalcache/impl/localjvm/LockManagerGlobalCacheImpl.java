package ru.intertrust.cm.globalcache.impl.localjvm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.util.DecimalCounter;
import ru.intertrust.cm.core.business.api.util.LongCounter;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.dto.CollectionTypesKey;
import ru.intertrust.cm.globalcache.api.AccessChanges;
import ru.intertrust.cm.globalcache.api.CollectionSubKey;
import ru.intertrust.cm.globalcache.api.PersonAccessChanges;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 30.07.2015
 *         Time: 13:33
 */
public abstract class LockManagerGlobalCacheImpl extends GlobalCacheImpl {
    private static final Logger logger = LoggerFactory.getLogger(LockManagerGlobalCacheImpl.class);

    protected abstract LockManager getLockManager();
    
    @Override
    public void activate() {
        final GlobalCacheLock globalLock = getLockManager().globalWriteLock();
        try {
            super.activate();
        } finally {
            getLockManager().unlock(globalLock);
        }
    }

    @Override
    public void deactivate() {
        final GlobalCacheLock globalLock = getLockManager().globalWriteLock();
        try {
            super.deactivate();
        } finally {
            getLockManager().unlock(globalLock);
        }
    }

    @Override
    public void clear() {
        final GlobalCacheLock globalLock = getLockManager().globalWriteLock();
        try {
            super.clear();
        } finally {
            getLockManager().unlock(globalLock);
        }
    }

    @Override
    public void notifyCreate(String transactionId, DomainObject obj, AccessToken accessToken) { // NOT as empty
        super.notifyCreate(transactionId, obj, accessToken);
    }

    @Override
    public void notifyUpdate(String transactionId, DomainObject obj, AccessToken accessToken) { // NOT as empty
        super.notifyUpdate(transactionId, obj, accessToken);
    }

    @Override
    public void notifyDelete(String transactionId, Id id) { // NOT as empty
        super.notifyDelete(transactionId, id);
    }

    @Override
    public void notifyRead(String transactionId, Id id, DomainObject obj, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().writeLock(id, obj, getUserSubject(accessToken));
        try {
            super.notifyRead(transactionId, id, obj, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public void notifyRead(String transactionId, Collection<DomainObject> objects, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().writeLock(objects, getUserSubject(accessToken));
        try {
            super.notifyRead(transactionId, objects, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public void clearAccessLog() {
        final GlobalCacheLock lock = getLockManager().globalAccessWriteLock();
        try {
            super.clearAccessLog();
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    protected void evictObjectAndCorrespondingEntries(Id id) {
        final GlobalCacheLock lock = getLockManager().writeLock(id, (DomainObject) null, null);
        try {
            super.evictObjectAndCorrespondingEntries(id);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    protected void evictObjectAndCorrespondingEntries(DomainObject domainObject) {
        final GlobalCacheLock lock = getLockManager().writeLock(domainObject.getId(), domainObject, null);
        try {
            super.evictObjectAndCorrespondingEntries(domainObject);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public void notifyReadByUniqueKey(String transactionId, String type, Map<String, Value> uniqueKey, DomainObject obj, long time, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().writeLock(type, getUserSubject(accessToken));
        try {
            super.notifyReadByUniqueKey(transactionId, type, uniqueKey, obj, time, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public void notifyReadPossiblyNullObjects(String transactionId, Collection<Pair<Id, DomainObject>> idsAndObjects, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().writeLockForPossiblyNullObjects(idsAndObjects, getUserSubject(accessToken));
        try {
            super.notifyReadPossiblyNullObjects(transactionId, idsAndObjects, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public DomainObject getDomainObject(String transactionId, String type, Map<String, Value> uniqueKey, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().readLock(type, getUserSubject(accessToken));
        try {
            return super.getDomainObject(transactionId, type, uniqueKey, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public void notifyLinkedObjectsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, List<DomainObject> linkedObjects, long time, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().writeLock(id, linkedType, getUserSubject(accessToken));
        try {
            super.notifyLinkedObjectsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjects, time, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public void notifyLinkedObjectsIdsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, List<Id> linkedObjectsIds, long time, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().writeLock(id, linkedType, getUserSubject(accessToken));
        try {
            super.notifyLinkedObjectsIdsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjectsIds, time, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public void invalidate(CacheInvalidation cacheInvalidation) { // do NOT sync - it's synchronized inside
        super.invalidate(cacheInvalidation);
    }

    @Override
    public void invalidateUserAccess(UserSubject subject) {
        final GlobalCacheLock lock = getLockManager().writeLock(subject);
        try {
            super.invalidateUserAccess(subject);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public void notifyCommit(DomainObjectsModification modification, AccessChanges accessChanges) {
        final GlobalCacheLock lock = getLockManager().writeLock(modification, accessChanges);
        try {
            final PersonAccessChanges personAccessChanges = (PersonAccessChanges) accessChanges;
            if (personAccessChanges.accessChangesExist()) {
                super.clearAccessLog();
                getLockManager().unlockGlobalAccessWriteLock(lock);
            }
            super.notifyCommit(modification, accessChanges);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public DomainObject getDomainObject(String transactionId, Id id, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().readLock(id, (DomainObject) null, getUserSubject(accessToken));
        try {
            return super.getDomainObject(transactionId, id, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public ArrayList<DomainObject> getDomainObjects(String transactionId, Collection<Id> ids, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().readLockIds(ids, getUserSubject(accessToken));
        try {
            return super.getDomainObjects(transactionId, ids, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public List<DomainObject> getLinkedDomainObjects(String transactionId, Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().readLock(domainObjectId, linkedType, getUserSubject(accessToken));
        try {
            return super.getLinkedDomainObjects(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public void notifyCollectionCountRead(String transactionId, String name, Set<String> domainObjectTypes, Set<String> filterNames, List<? extends Filter> filterValues, int count, long time, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().writeLockTypes(domainObjectTypes, getUserSubject(accessToken));
        try {
            super.notifyCollectionCountRead(transactionId, name, domainObjectTypes, filterNames, filterValues, count, time, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    // NOT synchronized
    @Override
    public void notifyCollectionRead(String transactionId, String name, Set<String> domainObjectTypes, Set<String> filterNames, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
        super.notifyCollectionRead(transactionId, name, domainObjectTypes, filterNames, filterValues, sortOrder, offset, limit, collection, time, accessToken);
    }

    // NOT synchronized
    @Override
    public void notifyCollectionRead(String transactionId, String query, Set<String> domainObjectTypes, List<? extends Value> paramValues, int offset, int limit, IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
        super.notifyCollectionRead(transactionId, query, domainObjectTypes, paramValues, offset, limit, collection, time, accessToken);
    }

    @Override
    protected void notifyCollectionRead(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes, IdentifiableObjectCollection clonedCollection, int count, long time, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().writeLockTypes(domainObjectTypes, getUserSubject(accessToken));
        try {
            super.notifyCollectionRead(key, subKey, domainObjectTypes, clonedCollection, count, time, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    // NOT synchronized
    @Override
    public IdentifiableObjectCollection getCollection(String transactionId, String name, List<? extends Filter> filterValues, Set<String> domainObjectTypes, SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {
        return super.getCollection(transactionId, name, filterValues, domainObjectTypes, sortOrder, offset, limit, accessToken);
    }

    // NOT synchronized
    @Override
    public IdentifiableObjectCollection getCollection(String transactionId, String query, List<? extends Value> paramValues, Set<String> domainObjectTypes, int offset, int limit, AccessToken accessToken) {
        return super.getCollection(transactionId, query, paramValues, domainObjectTypes, offset, limit, accessToken);
    }

    @Override
    protected int getCollectionCount(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().readLockTypes(domainObjectTypes, getUserSubject(accessToken));
        try {
            return super.getCollectionCount(key, subKey, domainObjectTypes, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    protected IdentifiableObjectCollection getCollection(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().readLockTypes(domainObjectTypes, getUserSubject(accessToken));
        try {
            return super.getCollection(key, subKey, domainObjectTypes, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public long getSizeBytes() {
        return super.getSizeBytes();
    }

    @Override
    public void setSizeLimitBytes(long bytes) {
        final GlobalCacheLock globalLock = getLockManager().globalWriteLock();
        try {
            super.setSizeLimitBytes(bytes);
        } finally {
            getLockManager().unlock(globalLock);
        }
    }

    @Override
    public long getSizeLimitBytes() {
        final GlobalCacheLock globalLock = getLockManager().globalReadLock();
        try {
            return super.getSizeLimitBytes();
        } finally {
            getLockManager().unlock(globalLock);
        }
    }

    @Override
    public void clearCacheCleanStatistics() { // NOT sync
        super.clearCacheCleanStatistics();
    }

    @Override
    public void notifyReadAll(String transactionId, String type, boolean exactType, Collection<DomainObject> objects, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().writeLock(type, getUserSubject(accessToken));
        try {
            super.notifyReadAll(transactionId, type, exactType, objects, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public List<DomainObject> getAllDomainObjects(String transactionId, String type, boolean exactType, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().readLock(type, getUserSubject(accessToken));
        try {
            return super.getAllDomainObjects(transactionId, type, exactType, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public List<Id> getLinkedDomainObjectsIds(String transactionId, Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        final GlobalCacheLock lock = getLockManager().writeLock(domainObjectId, linkedType, getUserSubject(accessToken));
        try {
            return super.getLinkedDomainObjectsIds(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
        } finally {
            getLockManager().unlock(lock);
        }
    }

    @Override
    public float getFreeSpacePercentage() { // not as completely safe
        return super.getFreeSpacePercentage();
    }

    @Override
    protected void assureCacheSizeLimit() { // do NOT sync
        super.assureCacheSizeLimit();
    }

    @Override
    public void cleanInvalidEntriesAndFreeSpace() { // do NOT synchronize
        super.cleanInvalidEntriesAndFreeSpace();
    }

    @Override
    protected void freeSpace(long startTime) { // do NOT synchronize
        super.freeSpace(startTime);
    }

    @Override
    protected void cleanInvalidEntries(long startTime) { // do NOT synchronize
        super.cleanInvalidEntries(startTime);
    }

    @Override
    protected void deleteEldestEntry() { // no sync, method called from background thread
        super.deleteEldestEntry();
    }

    @Override
    public LongCounter getCacheCleanTimeCounter() { // do NOT sync
        return super.getCacheCleanTimeCounter();
    }

    @Override
    public DecimalCounter getCacheCleanFreedSpaceCounter() { // do NOT sync
        return super.getCacheCleanFreedSpaceCounter();
    }

}
