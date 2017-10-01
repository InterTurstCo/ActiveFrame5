package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.util.DecimalCounter;
import ru.intertrust.cm.core.business.api.util.LongCounter;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.dto.CollectionTypesKey;
import ru.intertrust.cm.globalcache.api.AccessChanges;
import ru.intertrust.cm.globalcache.api.CollectionSubKey;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Denis Mitavskiy
 *         Date: 30.07.2015
 *         Time: 13:33
 */
public class StrictlySynchronizedGlobalCacheImpl extends GlobalCacheImpl {
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    @Override
    public void activate() {
        writeLock.lock();
        try {
            super.activate();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void deactivate() {
        writeLock.lock();
        try {
            super.deactivate();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            super.clear();
        } finally {
            writeLock.unlock();
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
        writeLock.lock();
        try {
            super.notifyRead(transactionId, id, obj, accessToken);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void notifyRead(String transactionId, Collection<DomainObject> objects, AccessToken accessToken) {
        writeLock.lock();
        try {
            super.notifyRead(transactionId, objects, accessToken);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clearAccessLog() {
        writeLock.lock();
        try {
            super.clearAccessLog();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    protected void evictObjectAndCorrespondingEntries(Id id) {
        writeLock.lock();
        try {
            super.evictObjectAndCorrespondingEntries(id);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    protected void evictObjectAndCorrespondingEntries(DomainObject domainObject) {
        writeLock.lock();
        try {
            super.evictObjectAndCorrespondingEntries(domainObject);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void notifyReadByUniqueKey(String transactionId, String type, Map<String, Value> uniqueKey, DomainObject obj, long time, AccessToken accessToken) {
        writeLock.lock();
        try {
            super.notifyReadByUniqueKey(transactionId, type, uniqueKey, obj, time, accessToken);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void notifyReadPossiblyNullObjects(String transactionId, Collection<Pair<Id, DomainObject>> idsAndObjects, AccessToken accessToken) {
        writeLock.lock();
        try {
            super.notifyReadPossiblyNullObjects(transactionId, idsAndObjects, accessToken);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public DomainObject getDomainObject(String transactionId, String type, Map<String, Value> uniqueKey, AccessToken accessToken) {
        readLock.lock();
        try {
            return super.getDomainObject(transactionId, type, uniqueKey, accessToken);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void notifyLinkedObjectsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, List<DomainObject> linkedObjects, long time, AccessToken accessToken) {
        writeLock.lock();
        try {
            super.notifyLinkedObjectsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjects, time, accessToken);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void notifyLinkedObjectsIdsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, List<Id> linkedObjectsIds, long time, AccessToken accessToken) {
        writeLock.lock();
        try {
            super.notifyLinkedObjectsIdsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjectsIds, time, accessToken);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void invalidate(CacheInvalidation cacheInvalidation) { // do NOT sync - it's synchronized inside
        super.invalidate(cacheInvalidation);
    }

    @Override
    public void invalidateUserAccess(UserSubject subject) {
        writeLock.lock();
        try {
            super.invalidateUserAccess(subject);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void notifyCommit(DomainObjectsModification modification, AccessChanges accessChanges) {
        writeLock.lock();
        try {
            super.notifyCommit(modification, accessChanges);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public DomainObject getDomainObject(String transactionId, Id id, AccessToken accessToken) {
        readLock.lock();
        try {
            return super.getDomainObject(transactionId, id, accessToken);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public ArrayList<DomainObject> getDomainObjects(String transactionId, Collection<Id> ids, AccessToken accessToken) {
        readLock.lock();
        try {
            return super.getDomainObjects(transactionId, ids, accessToken);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<DomainObject> getLinkedDomainObjects(String transactionId, Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        readLock.lock();
        try {
            return super.getLinkedDomainObjects(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void notifyCollectionCountRead(String transactionId, String name, Set<String> domainObjectTypes, Set<String> filterNames, List<? extends Filter> filterValues, int count, long time, AccessToken accessToken) {
        writeLock.lock();
        try {
            super.notifyCollectionCountRead(transactionId, name, domainObjectTypes, filterNames, filterValues, count, time, accessToken);
        } finally {
            writeLock.unlock();
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
        writeLock.lock();
        try {
            super.notifyCollectionRead(key, subKey, domainObjectTypes, clonedCollection, count, time, accessToken);
        } finally {
            writeLock.unlock();
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
        readLock.lock();
        try {
            return super.getCollectionCount(key, subKey, domainObjectTypes, accessToken);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    protected IdentifiableObjectCollection getCollection(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes, AccessToken accessToken) {
        readLock.lock();
        try {
            return super.getCollection(key, subKey, domainObjectTypes, accessToken);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public long getSizeBytes() {
        return super.getSizeBytes();
    }

    @Override
    public void setSizeLimitBytes(long bytes) {
        writeLock.lock();
        try {
            super.setSizeLimitBytes(bytes);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public long getSizeLimitBytes() {
        readLock.lock();
        try {
            return super.getSizeLimitBytes();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clearCacheCleanStatistics() { // NOT sync
        super.clearCacheCleanStatistics();
    }

    @Override
    public void notifyReadAll(String transactionId, String type, boolean exactType, Collection<DomainObject> objects, AccessToken accessToken) {
        writeLock.lock();
        try {
            super.notifyReadAll(transactionId, type, exactType, objects, accessToken);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<DomainObject> getAllDomainObjects(String transactionId, String type, boolean exactType, AccessToken accessToken) {
        readLock.lock();
        try {
            return super.getAllDomainObjects(transactionId, type, exactType, accessToken);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Id> getLinkedDomainObjectsIds(String transactionId, Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        readLock.lock();
        try {
            return super.getLinkedDomainObjectsIds(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
        } finally {
            readLock.unlock();
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
    protected void deleteEldestEntry() {
        writeLock.lock();
        try {
            super.deleteEldestEntry();
        } finally {
            writeLock.unlock();
        }
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
