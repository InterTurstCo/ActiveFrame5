package ru.intertrust.cm.globalcache.impl.localjvm.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.globalcache.api.AccessChanges;
import ru.intertrust.cm.globalcache.api.CollectionSubKey;
import ru.intertrust.cm.globalcache.impl.localjvm.StampedSynchronizedGlobalCacheImpl;

public class StampedSynchronizedGlobalCacheTestImpl extends StampedSynchronizedGlobalCacheImpl {

    private void checkDeadLock() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 3; i < stack.length; i++) {
            if (stack[i].getClassName().equals(this.getClass().getName())) {
                throw new FatalException("DeadLock candidate detected");
            }
        }
    }

    @Override
    public void activate() {
        checkDeadLock();
        super.activate();
    }

    @Override
    public void deactivate() {
        checkDeadLock();
        super.deactivate();
    }

    @Override
    public void clear() {
        checkDeadLock();
        super.clear();
    }

    @Override
    public void notifyRead(String transactionId, Id id, DomainObject obj, AccessToken accessToken) {
        checkDeadLock();
        super.notifyRead(transactionId, id, obj, accessToken);
    }

    @Override
    public void notifyRead(String transactionId, Collection<DomainObject> objects, AccessToken accessToken) {
        checkDeadLock();
        super.notifyRead(transactionId, objects, accessToken);
    }

    @Override
    public void clearAccessLog() {
        checkDeadLock();
        super.clearAccessLog();
    }

    @Override
    protected void evictObjectAndCorrespondingEntries(Id id) {
        checkDeadLock();
        super.evictObjectAndCorrespondingEntries(id);
    }

    @Override
    protected void evictObjectAndCorrespondingEntries(DomainObject domainObject) {
        checkDeadLock();
        super.evictObjectAndCorrespondingEntries(domainObject);
    }

    @Override
    public void notifyReadByUniqueKey(String transactionId, String type, Map<String, Value> uniqueKey, DomainObject obj, long time, AccessToken accessToken) {
        checkDeadLock();
        super.notifyReadByUniqueKey(transactionId, type, uniqueKey, obj, time, accessToken);
    }

    @Override
    public void notifyReadPossiblyNullObjects(String transactionId, Collection<Pair<Id, DomainObject>> idsAndObjects, AccessToken accessToken) {
        checkDeadLock();
        super.notifyReadPossiblyNullObjects(transactionId, idsAndObjects, accessToken);
    }

    @Override
    public DomainObject getDomainObject(String transactionId, String type, Map<String, Value> uniqueKey, AccessToken accessToken) {
        checkDeadLock();
        return super.getDomainObject(transactionId, type, uniqueKey, accessToken);
    }

    @Override
    public void notifyLinkedObjectsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, List<DomainObject> linkedObjects,
            long time, AccessToken accessToken) {
        checkDeadLock();
        super.notifyLinkedObjectsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjects, time, accessToken);
    }

    @Override
    public void notifyLinkedObjectsIdsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, List<Id> linkedObjectsIds,
            long time, AccessToken accessToken) {
        checkDeadLock();
        super.notifyLinkedObjectsIdsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjectsIds, time, accessToken);
    }

    @Override
    public void invalidateUserAccess(UserSubject subject) {
        checkDeadLock();
        super.invalidateUserAccess(subject);
    }

    @Override
    public void notifyCommit(DomainObjectsModification modification, AccessChanges accessChanges) {
        checkDeadLock();
        super.notifyCommit(modification, accessChanges);
    }

    @Override
    public DomainObject getDomainObject(String transactionId, Id id, AccessToken accessToken) {
        checkDeadLock();
        return super.getDomainObject(transactionId, id, accessToken);
    }

    @Override
    public ArrayList<DomainObject> getDomainObjects(String transactionId, Collection<Id> ids, AccessToken accessToken) {
        checkDeadLock();
        return super.getDomainObjects(transactionId, ids, accessToken);
    }

    @Override
    public List<DomainObject> getLinkedDomainObjects(String transactionId, Id domainObjectId, String linkedType, String linkedField, boolean exactType,
            AccessToken accessToken) {
        checkDeadLock();
        return super.getLinkedDomainObjects(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
    }

    @Override
    public void notifyCollectionCountRead(String transactionId, String name, Set<String> domainObjectTypes, Set<String> filterNames,
            List<? extends Filter> filterValues, int count, long time, AccessToken accessToken) {
        checkDeadLock();
        super.notifyCollectionCountRead(transactionId, name, domainObjectTypes, filterNames, filterValues, count, time, accessToken);
    }

    @Override
    protected void notifyCollectionRead(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes,
            IdentifiableObjectCollection clonedCollection, int count, long time, AccessToken accessToken) {
        checkDeadLock();
        super.notifyCollectionRead(key, subKey, domainObjectTypes, clonedCollection, count, time, accessToken);
    }

    @Override
    protected int getCollectionCount(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes, AccessToken accessToken) {
        checkDeadLock();
        return super.getCollectionCount(key, subKey, domainObjectTypes, accessToken);
    }

    @Override
    protected IdentifiableObjectCollection getCollection(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes,
            AccessToken accessToken) {
        checkDeadLock();
        return super.getCollection(key, subKey, domainObjectTypes, accessToken);
    }

    @Override
    public void setSizeLimitBytes(long bytes) {
        checkDeadLock();
        super.setSizeLimitBytes(bytes);
    }

    @Override
    public long getSizeLimitBytes() {
        checkDeadLock();
        return super.getSizeLimitBytes();
    }

    @Override
    public void notifyReadAll(String transactionId, String type, boolean exactType, Collection<DomainObject> objects, AccessToken accessToken) {
        checkDeadLock();
        super.notifyReadAll(transactionId, type, exactType, objects, accessToken);
    }

    @Override
    public List<DomainObject> getAllDomainObjects(String transactionId, String type, boolean exactType, AccessToken accessToken) {
        checkDeadLock();
        return super.getAllDomainObjects(transactionId, type, exactType, accessToken);
    }

    @Override
    public List<Id> getLinkedDomainObjectsIds(String transactionId, Id domainObjectId, String linkedType, String linkedField, boolean exactType,
            AccessToken accessToken) {
        checkDeadLock();
        return super.getLinkedDomainObjectsIds(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
    }

    @Override
    protected void deleteEldestEntry() {
        checkDeadLock();
        super.deleteEldestEntry();
    }
}
