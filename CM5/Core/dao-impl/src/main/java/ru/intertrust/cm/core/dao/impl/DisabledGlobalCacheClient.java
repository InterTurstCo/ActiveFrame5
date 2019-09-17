package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.AclInfo;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.globalcacheclient.GlobalCacheSettings;

import java.io.Serializable;
import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 03.07.2015
 *         Time: 15:53
 */
public class DisabledGlobalCacheClient implements GlobalCacheClient {

    @Autowired
    private GlobalCacheSettings globalCacheSettings;

    public static final DisabledGlobalCacheClient INSTANCE = new DisabledGlobalCacheClient();


    @Override
    public void activate(boolean isAtStartActivation) {
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void applySettings(Map<String, Serializable> settings) {
    }

    @Override
    public Map<String, Serializable> getSettings() {
        final HashMap<String, Serializable> settings = new HashMap<>();
        settings.put("global.cache.mode", globalCacheSettings.getMode().toString());
        settings.put("global.cache.max.size", globalCacheSettings.getSizeLimitBytes());
        settings.put("global.cache.max.item.size", globalCacheSettings.getSizeItemLimitBytes());
        settings.put("global.cache.cluster.mode", globalCacheSettings.isInCluster());
        settings.put("global.cache.wait.lock.millies", globalCacheSettings.getWaitLockMillies());
        return settings;
    }

    @Override
    public void clear() {
    }

    @Override
    public void clearCurrentNode() {
    }

    @Override
    public void notifyCreate(DomainObject obj, AccessToken accessToken) {
    }

    @Override
    public void notifyUpdate(DomainObject obj, AccessToken accessToken) {
    }

    @Override
    public void notifyDelete(Id id) {
    }

    @Override
    public void notifyRead(Id id, DomainObject obj, AccessToken accessToken) {
    }

    @Override
    public void notifyReadByUniqueKey(String type, Map<String, Value> uniqueKey, DomainObject obj, long time, AccessToken accessToken) {
    }

    @Override
    public void notifyRead(Collection<DomainObject> objects, AccessToken accessToken) {
    }

    @Override
    public void notifyReadAll(String type, boolean exactType, Collection<DomainObject> objects, AccessToken accessToken) {
    }

    @Override
    public void notifyRead(Collection<Id> ids, Collection<DomainObject> obj, AccessToken accessToken) {
    }

    @Override
    public void notifyLinkedObjectsRead(Id id, String linkedType, String linkedField, boolean exactType,
                                        List<DomainObject> linkedObjects, long time, AccessToken accessToken) {
    }

    @Override
    public void notifyLinkedObjectsIdsRead(Id id, String linkedType, String linkedField, boolean exactType, List<Id> linkedObjectsIds, long time, AccessToken accessToken) {
    }

    @Override
    public void notifyCollectionCountRead(String name, List<? extends Filter> filterValues, int count, long time, AccessToken accessToken) {
    }

    @Override
    public void notifyCollectionRead(String name, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
    }

    @Override
    public void notifyCollectionRead(String query, List<? extends Value> paramValues, int offset, int limit, IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
    }

    public void notifyCollectionRead(String name, Set<String> filterNames, Set<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit,
                                     IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
    }

    @Override
    public void invalidateCurrentNode(CacheInvalidation cacheInvalidation) {
    }

    @Override
    public void notifyCommit(DomainObjectsModification modification) {
    }

    @Override
    public void notifyRollback(String transactionId) {
    }

    @Override
    public void notifyPersonGroupChanged(Id person) {
    }

    @Override
    public void notifyGroupBranchChanged(Id groupId) {
    }

    @Override
    public void notifyAclCreated(Id contextObj, Collection<AclInfo> recordsInserted) {
    }

    @Override
    public void notifyAclDeleted(Id contextObj, Collection<AclInfo> recordsDeleted) {
    }

    @Override
    public DomainObject getDomainObject(Id id, AccessToken accessToken) {
        return null;
    }

    @Override
    public DomainObject getDomainObject(String type, Map<String, Value> uniqueKey, AccessToken accessToken) {
        return null;
    }

    @Override
    public ArrayList<DomainObject> getDomainObjects(Collection<Id> ids, AccessToken accessToken) {
        return null;
    }

    @Override
    public List<DomainObject> getLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        return null;
    }

    @Override
    public List<Id> getLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        return null;
    }

    @Override
    public List<DomainObject> getAllDomainObjects(String type, boolean exactType, AccessToken accessToken) {
        return null;
    }

    @Override
    public int getCollectionCount(String name, List<? extends Filter> filterValues, AccessToken accessToken) {
        return -1;
    }

    @Override
    public IdentifiableObjectCollection getCollection(String name, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {
        return null;
    }

    @Override
    public IdentifiableObjectCollection getCollection(String query, List<? extends Value> paramValues, int offset, int limit, AccessToken accessToken) {
        return null;
    }

    @Override
    public GlobalCacheStatistics getStatistics() {
        return null;
    }

    @Override
    public void clearStatistics(boolean hourlyOnly) {
    }

    @Override
    public void setCollectionsDao(CollectionsDao collectionsDao) {
    }
}
