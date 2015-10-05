package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.AclInfo;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 03.07.2015
 *         Time: 15:53
 */
public class DisabledGlobalCacheClient implements GlobalCacheClient {
    public static final DisabledGlobalCacheClient INSTANCE = new DisabledGlobalCacheClient();

    @Override
    public boolean debugEnabled() {
        return false;
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
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
    public void notifyCollectionRead(String name, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
    }

    @Override
    public void notifyCollectionRead(String query, List<? extends Value> paramValues, int offset, int limit, IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
    }

    public void notifyCollectionRead(String name, Set<String> filterNames, Set<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit,
                                     IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
    }

    @Override
    public void notifyCommit(DomainObjectsModification modification) {
    }

    @Override
    public void notifyRollback(String transactionId) {
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
    public IdentifiableObjectCollection getCollection(String name, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {
        return null;
    }

    @Override
    public IdentifiableObjectCollection getCollection(String query, List<? extends Value> paramValues, int offset, int limit, AccessToken accessToken) {
        return null;
    }

    @Override
    public void setCollectionsDao(CollectionsDao collectionsDao) {
    }
}
