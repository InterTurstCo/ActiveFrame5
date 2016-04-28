package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.AclInfo;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 21.10.2015
 *         Time: 14:36
 */
public class SwitchableCacheClient implements GlobalCacheClient {
    private GlobalCacheClient impl;

    public SwitchableCacheClient(GlobalCacheClient impl) {
        this.impl = impl;
    }

    public synchronized GlobalCacheClient getGlobalCacheClientImpl() {
        return impl;
    }

    public synchronized void setGlobalCacheClientImpl(GlobalCacheClient impl) {
        this.impl = impl;
    }

    @Override
    public void activate(boolean isAtStartActivation) {
        this.impl.activate(isAtStartActivation);
    }

    @Override
    public void deactivate() {
        this.impl.deactivate();
    }

    @Override
    public void applySettings(Map<String, Serializable> settings) {
        impl.applySettings(settings);
    }

    @Override
    public Map<String, Serializable> getSettings() {
        return impl.getSettings();
    }

    @Override
    public void clear() {
        impl.clear();
    }

    @Override
    public void clearCurrentNode() {
        impl.clearCurrentNode();
    }

    @Override
    public void notifyCreate(DomainObject obj, AccessToken accessToken) {
        impl.notifyCreate(obj, accessToken);
    }

    @Override
    public void notifyUpdate(DomainObject obj, AccessToken accessToken) {
        impl.notifyUpdate(obj, accessToken);
    }

    @Override
    public void notifyDelete(Id id) {
        impl.notifyDelete(id);
    }

    @Override
    public void notifyRead(Id id, DomainObject obj, AccessToken accessToken) {
        impl.notifyRead(id, obj, accessToken);
    }

    @Override
    public void notifyReadByUniqueKey(String type, Map<String, Value> uniqueKey, DomainObject obj, long time, AccessToken accessToken) {
        impl.notifyReadByUniqueKey(type, uniqueKey, obj, time, accessToken);
    }

    @Override
    public void notifyRead(Collection<DomainObject> objects, AccessToken accessToken) {
        impl.notifyRead(objects, accessToken);
    }

    @Override
    public void notifyReadAll(String type, boolean exactType, Collection<DomainObject> objects, AccessToken accessToken) {
        impl.notifyReadAll(type, exactType, objects, accessToken);
    }

    @Override
    public void notifyRead(Collection<Id> ids, Collection<DomainObject> objects, AccessToken accessToken) {
        impl.notifyRead(ids, objects, accessToken);
    }

    @Override
    public void notifyLinkedObjectsRead(Id id, String linkedType, String linkedField, boolean exactType, List<DomainObject> linkedObjects, long time, AccessToken accessToken) {
        impl.notifyLinkedObjectsRead(id, linkedType, linkedField, exactType, linkedObjects, time, accessToken);
    }

    @Override
    public void notifyLinkedObjectsIdsRead(Id id, String linkedType, String linkedField, boolean exactType, List<Id> linkedObjectsIds, long time, AccessToken accessToken) {
        impl.notifyLinkedObjectsIdsRead(id, linkedType, linkedField, exactType, linkedObjectsIds, time, accessToken);
    }

    @Override
    public void notifyCollectionRead(String name, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
        impl.notifyCollectionRead(name, filterValues, sortOrder, offset, limit, collection, time, accessToken);
    }

    @Override
    public void notifyCollectionRead(String query, List<? extends Value> paramValues, int offset, int limit, IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
        impl.notifyCollectionRead(query, paramValues, offset, limit, collection, time, accessToken);
    }

    @Override
    public void invalidateCurrentNode(CacheInvalidation cacheInvalidation) {
        impl.invalidateCurrentNode(cacheInvalidation);
    }

    @Override
    public void notifyCommit(DomainObjectsModification modification) {
        impl.notifyCommit(modification);
    }

    @Override
    public void notifyRollback(String transactionId) {
        impl.notifyRollback(transactionId);
    }

    @Override
    public void notifyAclCreated(Id contextObj, Collection<AclInfo> recordsInserted) {
        impl.notifyAclCreated(contextObj, recordsInserted);
    }

    @Override
    public void notifyAclDeleted(Id contextObj, Collection<AclInfo> recordsDeleted) {
        impl.notifyAclDeleted(contextObj, recordsDeleted);
    }

    @Override
    public DomainObject getDomainObject(Id id, AccessToken accessToken) {
        return impl.getDomainObject(id, accessToken);
    }

    @Override
    public DomainObject getDomainObject(String type, Map<String, Value> uniqueKey, AccessToken accessToken) {
        return impl.getDomainObject(type, uniqueKey, accessToken);
    }

    @Override
    public ArrayList<DomainObject> getDomainObjects(Collection<Id> ids, AccessToken accessToken) {
        return impl.getDomainObjects(ids, accessToken);
    }

    @Override
    public List<DomainObject> getLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        return impl.getLinkedDomainObjects(domainObjectId, linkedType, linkedField, exactType, accessToken);
    }

    @Override
    public List<Id> getLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        return impl.getLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, exactType, accessToken);
    }

    @Override
    public List<DomainObject> getAllDomainObjects(String type, boolean exactType, AccessToken accessToken) {
        return impl.getAllDomainObjects(type, exactType, accessToken);
    }

    @Override
    public IdentifiableObjectCollection getCollection(String name, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {
        return impl.getCollection(name, filterValues, sortOrder, offset, limit, accessToken);
    }

    @Override
    public IdentifiableObjectCollection getCollection(String query, List<? extends Value> paramValues, int offset, int limit, AccessToken accessToken) {
        return impl.getCollection(query, paramValues, offset, limit, accessToken);
    }

    @Override
    public GlobalCacheStatistics getStatistics() {
        return impl.getStatistics();
    }

    @Override
    public void clearStatistics(boolean hourlyOnly) {
        impl.clearStatistics(hourlyOnly);
    }

    @Override
    public void setCollectionsDao(CollectionsDao collectionsDao) {
        impl.setCollectionsDao(collectionsDao);
    }
}
