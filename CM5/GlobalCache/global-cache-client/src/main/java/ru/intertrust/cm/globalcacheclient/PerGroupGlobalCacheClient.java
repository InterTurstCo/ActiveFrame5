package ru.intertrust.cm.globalcacheclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.AclInfo;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.UserTransactionService;
import ru.intertrust.cm.globalcache.api.GlobalCache;
import ru.intertrust.cm.globalcache.api.GroupAccessChanges;
import ru.intertrust.cm.globalcache.api.TransactionChanges;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 03.07.2015
 *         Time: 15:54
 */
public class PerGroupGlobalCacheClient extends LocalJvmCacheClient {
    private static final Logger logger = LoggerFactory.getLogger(PerGroupGlobalCacheClient.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private volatile GlobalCacheSettings globalCacheSettings;

    @Autowired
    protected UserTransactionService userTransactionService;

    @Autowired
    private ConfigurationExplorer explorer;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    private CollectionsDao collectionsDao;

    protected volatile GlobalCache globalCache;

    private volatile long totalReads;
    private volatile long totalHits;

    private ConcurrentHashMap<String, TransactionChanges> transactionChanges;

    private volatile long reActivationTime = 0; // cache won't be used for 1 minute after reactivation

    public void activate(boolean isInitialActivation) {
        GlobalCache globalCache;
        if (globalCacheSettings.getMode().isBlocking()) {
            globalCache = (GlobalCache) context.getBean("blockingGlobalCache");
        } else {
            globalCache = (GlobalCache) context.getBean("globalCache");
        }
        globalCache.setSizeLimitBytes(globalCacheSettings.getSizeLimitBytes());
        globalCache.activate();

        this.globalCache = globalCache;
        this.transactionChanges = new ConcurrentHashMap<>();
        if (!isInitialActivation) {
            reActivationTime = System.currentTimeMillis();
        }
    }

    public void deactivate() {
        this.globalCache.deactivate();
    }

    public void applySettings(HashMap<String, Serializable> newSettings) {
        final String newModeStr = (String) newSettings.get("global.cache.mode");
        final Long maxSizeStr = (Long) newSettings.get("global.cache.max.size");
        final GlobalCacheSettings.Mode prevMode = globalCacheSettings.getMode();
        final GlobalCacheSettings.Mode newMode = GlobalCacheSettings.Mode.getMode(newModeStr);
        globalCacheSettings.setSizeLimitBytes(maxSizeStr);
        if (prevMode != newMode) {
            globalCacheSettings.setMode(newMode);
            final GlobalCache prevCache = this.globalCache;
            activate(false);
            prevCache.deactivate();
        } else {
            this.globalCache.setSizeLimitBytes(globalCacheSettings.getSizeLimitBytes());
        }
    }

    public void clear() {
        globalCache.clear();
    }

    @Override
    public void setCollectionsDao(CollectionsDao collectionsDao) {
        this.collectionsDao = collectionsDao;
    }

    @Override
    public void notifyCreate(DomainObject obj, AccessToken accessToken) {
        addObjectChanged(obj.getId(), obj.getTypeName());
    }

    @Override
    public void notifyUpdate(DomainObject obj, AccessToken accessToken) {
        addObjectChanged(obj.getId(), obj.getTypeName());
    }

    @Override
    public void notifyDelete(Id id) {
        addObjectChanged(id, domainObjectTypeIdCache.getName(id));
    }

    @Override
    public void notifyRead(Id id, DomainObject obj, AccessToken accessToken) {
        if (obj == null || !isChangedInTransaction(obj.getId())) {
            globalCache.notifyRead(null, id, obj, accessToken);
        }
    }

    @Override
    public void notifyReadByUniqueKey(String type, Map<String, Value> uniqueKey, DomainObject obj, long time, AccessToken accessToken) {
        if (obj == null && !isTypeSaved(type) || obj != null && !isChangedInTransaction(obj.getId())) {
            globalCache.notifyReadByUniqueKey(null, type, uniqueKey, obj, time, accessToken);
        }
    }

    @Override
    public void notifyRead(Collection<DomainObject> objects, AccessToken accessToken) {
        final Collection<DomainObject> unmodifiedInTransaction = getObjectsUnmodifiedInTransaction(objects);
        if (unmodifiedInTransaction.isEmpty()) {
            return;
        }
        globalCache.notifyRead(null, unmodifiedInTransaction, accessToken);
    }

    @Override
    public void notifyReadAll(String type, boolean exactType, Collection<DomainObject> objects, AccessToken accessToken) {
        final Collection<DomainObject> unmodifiedObjects = getObjectsUnmodifiedInTransaction(objects);
        if (unmodifiedObjects.isEmpty()) {
            return;
        }
        if (unmodifiedObjects.size() != objects.size()) {
            if (unmodifiedObjects.isEmpty()) {
                return;
            }
            globalCache.notifyRead(null, unmodifiedObjects, accessToken);
        } else {
            globalCache.notifyReadAll(null, type, exactType, objects, accessToken);
        }
    }

    @Override
    public void notifyRead(Collection<Id> ids, Collection<DomainObject> objects, AccessToken accessToken) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        HashMap<Id, DomainObject> objectsById;
        if (objects == null || objects.isEmpty()) {
            objectsById = new HashMap<>(0);
        } else {
            objectsById = new HashMap<>((int) (objects.size() / 0.75) + 1);
            for (DomainObject object : objects) {
                objectsById.put(object.getId(), object);
            }
        }
        ArrayList<Pair<Id, DomainObject>> trustedObjects = new ArrayList<>(ids.size());
        for (Id id : ids) {
            DomainObject obj = objectsById.get(id);
            if (obj == null) {
                trustedObjects.add(new Pair<Id, DomainObject>(id, null));
            } else if (!isChangedInTransaction(id)) {
                trustedObjects.add(new Pair<>(id, obj));
            }
        }

        globalCache.notifyReadPossiblyNullObjects(null, trustedObjects, accessToken);
    }

    @Override
    public void notifyLinkedObjectsRead(Id id, String linkedType, String linkedField, boolean exactType,
                                        List<DomainObject> linkedObjects, long time, AccessToken accessToken) {
        final Collection<DomainObject> unmodifiedObjects = getObjectsUnmodifiedInTransaction(linkedObjects);
        if (unmodifiedObjects.isEmpty()) {
            return;
        }
        if (unmodifiedObjects.size() != linkedObjects.size()) {
            globalCache.notifyRead(null, unmodifiedObjects, accessToken);
        } else {
            globalCache.notifyLinkedObjectsRead(null, id, linkedType, linkedField, exactType, linkedObjects, time, accessToken);
        }
    }

    @Override
    public void notifyLinkedObjectsIdsRead(Id id, String linkedType, String linkedField, boolean exactType,
                                        List<Id> linkedObjectsIds, long time, AccessToken accessToken) {
        final Set<Id> idsOfUnmodifiedObjects = getIdsOfObjectsUnmodifiedInTransaction(linkedObjectsIds);
        if (idsOfUnmodifiedObjects.size() != linkedObjectsIds.size() || idsOfUnmodifiedObjects.isEmpty()) {
            return;
        } else {
            globalCache.notifyLinkedObjectsIdsRead(null, id, linkedType, linkedField, exactType, idsOfUnmodifiedObjects, time, accessToken);
        }
    }

    @Override
    public void notifyCollectionRead(String name, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit,
                                     IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
        Pair<Set<String>, Set<String>> filterNamesWithTypes = collectionsDao.getDOTypes(name, filterValues);
        Set<String> filterNames = filterNamesWithTypes.getFirst();
        Set<String> doTypes = filterNamesWithTypes.getSecond();
        if (isAtLeastOneTypeSaved(doTypes)) {
            return;
        }
        globalCache.notifyCollectionRead(null, name, doTypes, filterNames, filterValues, sortOrder, offset, limit, collection, time, accessToken);
    }

    @Override
    public void notifyCollectionRead(String query, List<? extends Value> paramValues, int offset, int limit,
                                     IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
        Set<String> doTypes = collectionsDao.getQueryDOTypes(query);
        if (isAtLeastOneTypeSaved(doTypes)) {
            return;
        }
        globalCache.notifyCollectionRead(null, query, doTypes, paramValues, offset, limit, collection, time, accessToken);
    }

    @Override
    public void notifyCommit(DomainObjectsModification modification) {
        String transactionId = modification.getTransactionId();
        GroupAccessChanges accessChanges = createAccessChangesIfAbsent(transactionId);
        clearTransactionChanges(transactionId);
        globalCache.notifyCommit(modification, accessChanges);
    }

    public void notifyRollback(String transactionId) {
        clearTransactionChanges(transactionId);
    }

    @Override
    public void notifyAclCreated(Id contextObj, Collection<AclInfo> recordsInserted) {
        getAccessChanges().aclCreated(contextObj, domainObjectTypeIdCache.getName(contextObj), recordsInserted);
    }

    @Override
    public void notifyAclDeleted(Id contextObj, Collection<AclInfo> recordsDeleted) {
        getAccessChanges().aclDeleted(contextObj, domainObjectTypeIdCache.getName(contextObj), recordsDeleted);
    }

    @Override
    public DomainObject getDomainObject(Id id, AccessToken accessToken) {
        ++totalReads;
        return logHit(globalCache.getDomainObject(null, id, accessToken));
    }

    @Override
    public DomainObject getDomainObject(String type, Map<String, Value> uniqueKey, AccessToken accessToken) {
        ++totalReads;
        return logHit(globalCache.getDomainObject(null, type, uniqueKey, accessToken));
    }

    @Override
    public ArrayList<DomainObject> getDomainObjects(Collection<Id> ids, AccessToken accessToken) {
        ++totalReads;
        return logHit(globalCache.getDomainObjects(null, ids, accessToken));
    }

    @Override
    public List<DomainObject> getLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        ++totalReads;
        if (canUseCacheToRetrieveType(linkedType, exactType)) {
            return logHit(globalCache.getLinkedDomainObjects(null, domainObjectId, linkedType, linkedField, exactType, accessToken));
        } else {
            return null;
        }
    }

    @Override
    public List<Id> getLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        ++totalReads;
        if (canUseCacheToRetrieveType(linkedType, exactType)) {
            return logHit(globalCache.getLinkedDomainObjectsIds(null, domainObjectId, linkedType, linkedField, exactType, accessToken));
        } else {
            return null;
        }
    }

    @Override
    public List<DomainObject> getAllDomainObjects(String type, boolean exactType, AccessToken accessToken) {
        ++totalReads;
        if (canUseCacheToRetrieveType(type, exactType)) {
            return logHit(globalCache.getAllDomainObjects(null, type, exactType, accessToken));
        } else {
            return null;
        }
    }

    @Override
    public IdentifiableObjectCollection getCollection(String name, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {
        ++totalReads;
        Set<String> doTypes = collectionsDao.getDOTypes(name, filterValues).getSecond();
        if (isAtLeastOneTypeSaved(doTypes)) {
            return null;
        }
        return logHit(globalCache.getCollection(null, name, filterValues, sortOrder, offset, limit, accessToken));
    }

    @Override
    public IdentifiableObjectCollection getCollection(String query, List<? extends Value> paramValues, int offset, int limit, AccessToken accessToken) {
        ++totalReads;
        Set<String> doTypes = collectionsDao.getQueryDOTypes(query);
        if (isAtLeastOneTypeSaved(doTypes)) {
            return null;
        }
        return logHit(globalCache.getCollection(null, query, paramValues, offset, limit, accessToken));
    }

    @Override
    public GlobalCacheStatistics getStatistics() {
        final GlobalCacheStatistics result = new GlobalCacheStatistics();
        result.setHitCount(totalReads == 0 ? 0 : totalHits / (float) totalReads);
        result.setFreeSpacePercentage(globalCache.getFreeSpacePercentage());
        result.setSize(globalCache.getSizeBytes());
        result.setCacheCleaningRecord(new GlobalCacheStatistics.CacheCleaningRecord(globalCache.getCacheCleanTimeCounter(), globalCache.getCacheCleanFreedSpaceCounter()));
        return result;
    }

    @Override
    public void clearStatistics(boolean hourlyOnly) {
        if (!hourlyOnly) {
            totalReads = 0;
            totalHits = 0;
        }
    }

    private <T> T logHit(T result) {
        if (result != null) {
            ++totalHits;
        }
        return result;
    }

    private boolean canUseCacheToRetrieveType(String type, boolean exactType) {
        if (isReactivationUndergoing()) {
            return false;
        }
        final TransactionChanges transactionChanges = getTransactionChanges();
        boolean useCache;
        if (transactionChanges == null) {
            useCache = true;
        } else if (transactionChanges.isTypeSaved(type)) {
            useCache = false;
        } else if (!exactType) {
            useCache = true;
            final Collection<DomainObjectTypeConfig> linkedTypeChildren = explorer.findChildDomainObjectTypes(type, true);
            for (DomainObjectTypeConfig linkedTypeChild : linkedTypeChildren) {
                if (transactionChanges.isTypeSaved(linkedTypeChild.getName())) {
                    useCache = false;
                    break;
                }
            }
        } else {
            useCache = true;
        }
        return useCache;
    }

    private Collection<DomainObject> getObjectsUnmodifiedInTransaction(Collection<DomainObject> objects) {
        if (objects.isEmpty()) {
            return objects;
        }
        if (isReactivationUndergoing()) {
            return Collections.emptyList();
        }
        ArrayList<DomainObject> trustedObjects = new ArrayList<>(objects.size());
        for (DomainObject object : objects) {
            if (!isChangedInTransaction(object.getId())) {
                trustedObjects.add(object);
            }
        }
        return trustedObjects;
    }

    private Set<Id> getIdsOfObjectsUnmodifiedInTransaction(Collection<Id> ids) {
        if (isReactivationUndergoing() || ids.isEmpty()) {
            return Collections.emptySet();
        }
        LinkedHashSet<Id> trustedIds = new LinkedHashSet<>(ids.size());
        for (Id id : ids) {
            if (!isChangedInTransaction(id)) {
                trustedIds.add(id);
            }
        }
        return trustedIds;
    }

    private boolean isAtLeastOneTypeSaved(Set<String> doTypes) {
        if (doTypes == null) { // this collection is definitely not in cache
            return true;
        }
        if (isReactivationUndergoing()) {
            return true;
        }
        final TransactionChanges transactionChanges = getTransactionChanges();
        if (transactionChanges == null) {
            return false;
        }
        if (transactionChanges.isAtLeastOneTypeSaved(doTypes)) {
            return true;
        }
        return false;
    }

    private boolean isTypeSaved(String type) {
        if (isReactivationUndergoing()) {
            return true;
        }
        final TransactionChanges changes = getTransactionChanges();
        return changes != null && changes.isTypeSaved(type);
    }

    private boolean isChangedInTransaction(Id id) {
        if (isReactivationUndergoing()) {
            return true;
        }
        final TransactionChanges changes = getTransactionChanges();
        return changes != null && changes.isObjectChanged(id);
    }

    private boolean isReactivationUndergoing() {
        if (reActivationTime == 0) {
            return false;
        }
        if (System.currentTimeMillis() - reActivationTime < 60000) {
            return true;
        }
        reActivationTime = 0;
        return false;
    }

    private TransactionChanges getTransactionChanges() {
        final String transactionId = userTransactionService.getTransactionId();
        return transactionId == null ? null : this.transactionChanges.get(transactionId);
    }

    protected void clearTransactionChanges(String transactionId) {
        transactionChanges.remove(transactionId);
    }

    protected GroupAccessChanges getAccessChanges() {
        final String transactionId = userTransactionService.getTransactionId();
        return transactionId == null ? new GroupAccessChanges() : createAccessChangesIfAbsent(transactionId);
    }

    protected GroupAccessChanges createAccessChangesIfAbsent(String transactionId) {
        TransactionChanges transactionChanges = createTransactionChangesIfAbsent(transactionId);
        GroupAccessChanges accessChanges = transactionChanges.getGroupAccessChanges();
        if (accessChanges == null) {
            accessChanges = new GroupAccessChanges();
            transactionChanges.setGroupAccessChanges(accessChanges);
        }
        return accessChanges;
    }

    private TransactionChanges createTransactionChangesIfAbsent(String transactionId) {
        TransactionChanges transactionChanges = this.transactionChanges.get(transactionId);
        if (transactionChanges == null) {
            transactionChanges = new TransactionChanges();
            this.transactionChanges.put(transactionId, transactionChanges);
        }
        return transactionChanges;
    }

    private void addObjectChanged(Id id, String type) {
        final String transactionId = userTransactionService.getTransactionId();
        if (transactionId == null) {
            return;
        }
        TransactionChanges transactionChanges = createTransactionChangesIfAbsent(transactionId);
        transactionChanges.addObjectChanged(id, type);
    }
}
