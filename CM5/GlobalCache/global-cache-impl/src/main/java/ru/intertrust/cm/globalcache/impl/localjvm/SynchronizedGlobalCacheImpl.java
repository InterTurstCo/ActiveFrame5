package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.globalcache.api.AccessChanges;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 30.07.2015
 *         Time: 13:33
 */
public class SynchronizedGlobalCacheImpl extends GlobalCacheImpl {
    @Override
    public synchronized void notifyCreate(String transactionId, DomainObject obj, AccessToken accessToken) {
        super.notifyCreate(transactionId, obj, accessToken);
    }

    @Override
    public synchronized void notifyUpdate(String transactionId, DomainObject obj, AccessToken accessToken) {
        super.notifyUpdate(transactionId, obj, accessToken);
    }

    @Override
    public synchronized void notifyDelete(String transactionId, Id id) {
        super.notifyDelete(transactionId, id);
    }

    @Override
    public synchronized void notifyRead(String transactionId, Id id, DomainObject obj, AccessToken accessToken) {
        super.notifyRead(transactionId, id, obj, accessToken);
    }

    @Override
    public synchronized void notifyRead(String transactionId, Collection<DomainObject> objects, AccessToken accessToken) {
        super.notifyRead(transactionId, objects, accessToken);
    }

    @Override
    public synchronized void notifyReadByUniqueKey(String transactionId, String type, Map<String, Value> uniqueKey, DomainObject obj, long time, AccessToken accessToken) {
        super.notifyReadByUniqueKey(transactionId, type, uniqueKey, obj, time, accessToken);
    }

    @Override
    public synchronized void notifyReadPossiblyNullObjects(String transactionId, Collection<Pair<Id, DomainObject>> idsAndObjects, AccessToken accessToken) {
        super.notifyReadPossiblyNullObjects(transactionId, idsAndObjects, accessToken);
    }

    @Override
    public synchronized DomainObject getDomainObject(String transactionId, String type, Map<String, Value> uniqueKey, AccessToken accessToken) {
        return super.getDomainObject(transactionId, type, uniqueKey, accessToken);
    }

    @Override
    public synchronized void notifyLinkedObjectsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, List<DomainObject> linkedObjects, long time, AccessToken accessToken) {
        super.notifyLinkedObjectsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjects, time, accessToken);
    }

    @Override
    public synchronized void notifyLinkedObjectsIdsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, Set<Id> linkedObjectsIds, long time, AccessToken accessToken) {
        super.notifyLinkedObjectsIdsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjectsIds, time, accessToken);
    }

    @Override
    public synchronized void notifyCommit(DomainObjectsModification modification, AccessChanges accessChanges) {
        super.notifyCommit(modification, accessChanges);
    }

    @Override
    public synchronized DomainObject getDomainObject(String transactionId, Id id, AccessToken accessToken) {
        return super.getDomainObject(transactionId, id, accessToken);
    }

    @Override
    public synchronized ArrayList<DomainObject> getDomainObjects(String transactionId, Collection<Id> ids, AccessToken accessToken) {
        return super.getDomainObjects(transactionId, ids, accessToken);
    }

    @Override
    public synchronized List<DomainObject> getLinkedDomainObjects(String transactionId, Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        return super.getLinkedDomainObjects(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
    }

    @Override
    public synchronized void notifyCollectionRead(String transactionId, String name, Set<String> domainObjectTypes, Set<String> filterNames, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
        super.notifyCollectionRead(transactionId, name, domainObjectTypes, filterNames, filterValues, sortOrder, offset, limit, collection, time, accessToken);
    }

    @Override
    public synchronized void notifyCollectionRead(String transactionId, String query, Set<String> domainObjectTypes, List<? extends Value> paramValues, int offset, int limit, IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
        super.notifyCollectionRead(transactionId, query, domainObjectTypes, paramValues, offset, limit, collection, time, accessToken);
    }

    @Override
    public synchronized IdentifiableObjectCollection getCollection(String transactionId, String name, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {
        return super.getCollection(transactionId, name, filterValues, sortOrder, offset, limit, accessToken);
    }

    @Override
    public synchronized IdentifiableObjectCollection getCollection(String transactionId, String query, List<? extends Value> paramValues, int offset, int limit, AccessToken accessToken) {
        return super.getCollection(transactionId, query, paramValues, offset, limit, accessToken);
    }

    @Override
    public synchronized void notifyReadAll(String transactionId, String type, boolean exactType, Collection<DomainObject> objects, AccessToken accessToken) {
        super.notifyReadAll(transactionId, type, exactType, objects, accessToken);
    }

    @Override
    public synchronized List<DomainObject> getAllDomainObjects(String transactionId, String type, boolean exactType, AccessToken accessToken) {
        return super.getAllDomainObjects(transactionId, type, exactType, accessToken);
    }
}
