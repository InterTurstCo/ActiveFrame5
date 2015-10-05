package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.AclInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 03.07.2015
 *         Time: 15:10
 */
public interface GlobalCacheClient {
    boolean debugEnabled();

    void setDebugEnabled(boolean enabled);

    void notifyCreate(DomainObject obj, AccessToken accessToken);

    void notifyUpdate(DomainObject obj, AccessToken accessToken);

    void notifyDelete(Id id);

    void notifyRead(Id id, DomainObject obj, AccessToken accessToken);

    void notifyReadByUniqueKey(String type, Map<String, Value> uniqueKey, DomainObject obj, long time, AccessToken accessToken);

    void notifyRead(Collection<DomainObject> objects, AccessToken accessToken);

    void notifyReadAll(String type, boolean exactType, Collection<DomainObject> objects, AccessToken accessToken);

    void notifyRead(Collection<Id> ids, Collection<DomainObject> objects, AccessToken accessToken);

    void notifyLinkedObjectsRead(Id id, String linkedType, String linkedField, boolean exactType, List<DomainObject> linkedObjects,
                                 long time, AccessToken accessToken);

    void notifyLinkedObjectsIdsRead(Id id, String linkedType, String linkedField, boolean exactType, List<Id> linkedObjectsIds,
                                 long time, AccessToken accessToken);

    void notifyCollectionRead(String name, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit,
                              IdentifiableObjectCollection collection, long time, AccessToken accessToken);

    void notifyCollectionRead(String query, List<? extends Value> paramValues, int offset, int limit,
                                     IdentifiableObjectCollection collection, long time, AccessToken accessToken);

    void notifyCommit(DomainObjectsModification modification);

    void notifyRollback(String transactionId);

    void notifyAclCreated(Id contextObj, Collection<AclInfo> recordsInserted);

    void notifyAclDeleted(Id contextObj, Collection<AclInfo> recordsDeleted);

    DomainObject getDomainObject(Id id, AccessToken accessToken);

    DomainObject getDomainObject(String type, Map<String, Value> uniqueKey, AccessToken accessToken);

    ArrayList<DomainObject> getDomainObjects(Collection<Id> ids, AccessToken accessToken);

    List<DomainObject> getLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken);

    List<Id> getLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken);

    List<DomainObject> getAllDomainObjects(String type, boolean exactType, AccessToken accessToken);

    IdentifiableObjectCollection getCollection(String name, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, AccessToken accessToken);

    IdentifiableObjectCollection getCollection(String query, List<? extends Value> paramValues, int offset, int limit, AccessToken accessToken);

    void setCollectionsDao(CollectionsDao collectionsDao);
}
