package ru.intertrust.cm.globalcache.impl.localjvm;

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
import ru.intertrust.cm.globalcache.api.AccessChanges;
import ru.intertrust.cm.globalcache.api.CollectionSubKey;

public class SimpleSynchronizedGlobalCacheImpl extends GlobalCacheImpl {
    Object lock = new Object();

    @Override
    public void activate() {
        synchronized (lock) {
            super.activate();
        }
    }

    @Override
    public void deactivate() {
        synchronized (lock) {
            super.deactivate();
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            super.clear();
        }
    }

    @Override
    public void notifyRead(String transactionId, Id id, DomainObject obj, AccessToken accessToken) {
        synchronized (lock) {
            super.notifyRead(transactionId, id, obj, accessToken);
        }
    }

    @Override
    public void notifyRead(String transactionId, Collection<DomainObject> objects, AccessToken accessToken) {
        synchronized (lock) {
            super.notifyRead(transactionId, objects, accessToken);
        }
    }

    @Override
    public void clearAccessLog() {
        synchronized (lock) {
            super.clearAccessLog();
        }
    }

    @Override
    protected void evictObjectAndCorrespondingEntries(Id id) {
        synchronized (lock) {
            super.evictObjectAndCorrespondingEntries(id);
        }
    }

    @Override
    protected void evictObjectAndCorrespondingEntries(DomainObject domainObject) {
        synchronized (lock) {
            super.evictObjectAndCorrespondingEntries(domainObject);
        }
    }

    @Override
    public void notifyReadByUniqueKey(String transactionId, String type, Map<String, Value> uniqueKey, DomainObject obj, long time, AccessToken accessToken) {
        synchronized (lock) {
            super.notifyReadByUniqueKey(transactionId, type, uniqueKey, obj, time, accessToken);
        }
    }

    @Override
    public void notifyReadPossiblyNullObjects(String transactionId, Collection<Pair<Id, DomainObject>> idsAndObjects, AccessToken accessToken) {
        synchronized (lock) {
            super.notifyReadPossiblyNullObjects(transactionId, idsAndObjects, accessToken);
        }
    }

    @Override
    public DomainObject getDomainObject(String transactionId, String type, Map<String, Value> uniqueKey, AccessToken accessToken) {
        synchronized (lock) {
            return super.getDomainObject(transactionId, type, uniqueKey, accessToken);
        }
    }

    @Override
    public void notifyLinkedObjectsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, List<DomainObject> linkedObjects,
            long time, AccessToken accessToken) {
        synchronized (lock) {
            super.notifyLinkedObjectsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjects, time, accessToken);
        }
    }

    @Override
    public void notifyLinkedObjectsIdsRead(String transactionId, Id id, String linkedType, String linkedField, boolean exactType, List<Id> linkedObjectsIds,
            long time, AccessToken accessToken) {
        synchronized (lock) {
            super.notifyLinkedObjectsIdsRead(transactionId, id, linkedType, linkedField, exactType, linkedObjectsIds, time, accessToken);
        }
    }

    @Override
    public void invalidateUserAccess(UserSubject subject) {
        synchronized (lock) {
            super.invalidateUserAccess(subject);
        }
    }

    @Override
    public void notifyCommit(DomainObjectsModification modification, AccessChanges accessChanges) {
        synchronized (lock) {
            super.notifyCommit(modification, accessChanges);
        }
    }

    @Override
    public DomainObject getDomainObject(String transactionId, Id id, AccessToken accessToken) {
        synchronized (lock) {
            return super.getDomainObject(transactionId, id, accessToken);
        }
    }

    @Override
    public ArrayList<DomainObject> getDomainObjects(String transactionId, Collection<Id> ids, AccessToken accessToken) {
        synchronized (lock) {
            return super.getDomainObjects(transactionId, ids, accessToken);
        }
    }

    @Override
    public List<DomainObject> getLinkedDomainObjects(String transactionId, Id domainObjectId, String linkedType, String linkedField, boolean exactType,
            AccessToken accessToken) {
        synchronized (lock) {
            return super.getLinkedDomainObjects(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
        }
    }

    @Override
    public void notifyCollectionCountRead(String transactionId, String name, Set<String> domainObjectTypes, Set<String> filterNames,
            List<? extends Filter> filterValues, int count, long time, AccessToken accessToken) {
        synchronized (lock) {
            super.notifyCollectionCountRead(transactionId, name, domainObjectTypes, filterNames, filterValues, count, time, accessToken);
        }
    }

    @Override
    protected void notifyCollectionRead(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes,
            IdentifiableObjectCollection clonedCollection, int count, long time, AccessToken accessToken) {
        synchronized (lock) {
            super.notifyCollectionRead(key, subKey, domainObjectTypes, clonedCollection, count, time, accessToken);
        }
    }

    @Override
    protected int getCollectionCount(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes, AccessToken accessToken) {
        synchronized (lock) {
            return super.getCollectionCount(key, subKey, domainObjectTypes, accessToken);
        }
    }

    @Override
    protected IdentifiableObjectCollection getCollection(CollectionTypesKey key, CollectionSubKey subKey, Set<String> domainObjectTypes,
            AccessToken accessToken) {
        synchronized (lock) {
            return super.getCollection(key, subKey, domainObjectTypes, accessToken);
        }
    }

    @Override
    public void setSizeLimitBytes(long bytes) {
        synchronized (lock) {
            super.setSizeLimitBytes(bytes);
        }
    }

    @Override
    public long getSizeLimitBytes() {
        synchronized (lock) {
            return super.getSizeLimitBytes();
        }
    }

    @Override
    public void notifyReadAll(String transactionId, String type, boolean exactType, Collection<DomainObject> objects, AccessToken accessToken) {
        synchronized (lock) {
            super.notifyReadAll(transactionId, type, exactType, objects, accessToken);
        }
    }

    @Override
    public List<DomainObject> getAllDomainObjects(String transactionId, String type, boolean exactType, AccessToken accessToken) {
        synchronized (lock) {
            return super.getAllDomainObjects(transactionId, type, exactType, accessToken);
        }
    }

    @Override
    public List<Id> getLinkedDomainObjectsIds(String transactionId, Id domainObjectId, String linkedType, String linkedField, boolean exactType,
            AccessToken accessToken) {
        synchronized (lock) {
            return super.getLinkedDomainObjectsIds(transactionId, domainObjectId, linkedType, linkedField, exactType, accessToken);
        }
    }

    @Override
    protected void deleteEldestEntry() {
        synchronized (lock) {
            super.deleteEldestEntry();
        }
    }
}
