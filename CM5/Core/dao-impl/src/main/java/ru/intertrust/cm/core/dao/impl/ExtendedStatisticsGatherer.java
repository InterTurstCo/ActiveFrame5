package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.AclInfo;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Statistics gathering should be accurate, thus no dynamic proxies used
 * @author Denis Mitavskiy
 *         Date: 22.10.2015
 *         Time: 13:18
 */
public class ExtendedStatisticsGatherer implements GlobalCacheClient {
    private final MethodStatistics NOTIFY_CREATE = new MethodStatistics("Notify Create");
    private final MethodStatistics NOTIFY_UPDATE = new MethodStatistics("Notify Update");
    private final MethodStatistics NOTIFY_DELETE = new MethodStatistics("Notify Delete");
    private final MethodStatistics NOTIFY_READ = new MethodStatistics("Notify Read");
    private final MethodStatistics NOTIFY_READ_BY_UNIQUE_KEY = new MethodStatistics("Notify Read By Unique Key");
    private final MethodStatistics NOTIFY_READ_MANY = new MethodStatistics("Notify Read Many");
    private final MethodStatistics NOTIFY_READ_ALL = new MethodStatistics("Notify Read All");
    private final MethodStatistics NOTIFY_READ_MANY_POSSIBLY_NULL = new MethodStatistics("Notify Read Many Possibly Null");
    private final MethodStatistics NOTIFY_LINKED_OBJECTS_READ = new MethodStatistics("Notify Linked Objects Read");
    private final MethodStatistics NOTIFY_LINKED_OBJECTS_IDS_READ = new MethodStatistics("Notify Linked Objects Ids Read");
    private final MethodStatistics NOTIFY_COLLECTION_READ = new MethodStatistics("Notify Collection Read");
    private final MethodStatistics NOTIFY_COLLECTION_COUNT_READ = new MethodStatistics("Notify Collection Count Read");
    private final MethodStatistics NOTIFY_COLLECTION_BY_QUERY_READ = new MethodStatistics("Notify Collection By Query Read");
    private final MethodStatistics INVALIDATE = new MethodStatistics("Invalidate");
    private final MethodStatistics NOTIFY_COMMIT = new MethodStatistics("Notify Commit");
    private final MethodStatistics NOTIFY_ROLLBACK = new MethodStatistics("Notify Rollback");
    private final MethodStatistics NOTIFY_PERSON_GROUP_CHANGED = new MethodStatistics("Notify Person Group Changed");
    private final MethodStatistics NOTIFY_GROUP_BRANCH_CHANGED = new MethodStatistics("Notify Group Branch Changed");
    private final MethodStatistics NOTIFY_ACL_CREATED = new MethodStatistics("Notify ACL Created");
    private final MethodStatistics NOTIFY_ACL_DELETED = new MethodStatistics("Notify ACL Deleted");
    private final MethodStatistics GET_DOMAIN_OBJECT = new MethodStatistics("Get Domain Object", true);
    private final MethodStatistics GET_DOMAIN_OBJECT_BY_UNIQUE_KEY = new MethodStatistics("Get Domain Object By Unique Key", true);
    private final MethodStatistics GET_DOMAIN_OBJECTS = new MethodStatistics("Get Domain Objects", true);
    private final MethodStatistics GET_LINKED_DOMAIN_OBJECTS = new MethodStatistics("Get Linked Domain Objects", true);
    private final MethodStatistics GET_LINKED_DOMAIN_OBJECTS_IDS = new MethodStatistics("Get Linked Domain Objects Ids", true);
    private final MethodStatistics GET_ALL_DOMAIN_OBJECTS = new MethodStatistics("Get All Domain Objects", true);
    private final MethodStatistics GET_COLLECTION = new MethodStatistics("Get Collection", true);
    private final MethodStatistics GET_COLLECTION_COUNT = new MethodStatistics("Get Collection Count", true);
    private final MethodStatistics GET_COLLECTION_BY_QUERY = new MethodStatistics("Get Collection By Query", true);

    private ArrayList<MethodStatistics> allMethodsStatistics;
    private ArrayList<MethodStatistics> notifiersStatistics;
    private ArrayList<MethodStatistics> gettersStatistics;

    private GlobalCacheClient delegate;

    public ExtendedStatisticsGatherer(GlobalCacheClient delegate) {
        this.delegate = delegate;
        this.allMethodsStatistics = new ArrayList<>(24);
        this.notifiersStatistics = new ArrayList<>(16);
        this.gettersStatistics = new ArrayList<>(8);
        final Field[] fields = this.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                if (!field.getType().equals(MethodStatistics.class)) {
                    continue;
                }
                final MethodStatistics stats = (MethodStatistics) field.get(this);
                allMethodsStatistics.add(stats);
                if (stats.isGetter()) {
                    gettersStatistics.add(stats);
                } else {
                    notifiersStatistics.add(stats);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public GlobalCacheClient getDelegate() {
        return delegate;
    }

    @Override
    public void activate(boolean isAtStartActivation) {
        delegate.activate(isAtStartActivation);
    }

    @Override
    public void deactivate() {
        delegate.deactivate();
    }

    @Override
    public void applySettings(Map<String, Serializable> settings) {
        delegate.applySettings(settings);
    }

    @Override
    public Map<String, Serializable> getSettings() {
        return delegate.getSettings();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public void clearCurrentNode() {
        delegate.clearCurrentNode();
    }

    @Override
    public void notifyCreate(DomainObject obj, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        delegate.notifyCreate(obj, accessToken);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_CREATE.log(executionTime, false);
    }

    @Override
    public void notifyUpdate(DomainObject obj, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        delegate.notifyUpdate(obj, accessToken);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_UPDATE.log(executionTime, false);
    }

    @Override
    public void notifyDelete(Id id) {
        final long t1 = System.nanoTime();
        delegate.notifyDelete(id);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_DELETE.log(executionTime, false);
    }

    @Override
    public void notifyRead(Id id, DomainObject obj, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        delegate.notifyRead(id, obj, accessToken);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_READ.log(executionTime, false);
    }

    @Override
    public void notifyReadByUniqueKey(String type, Map<String, Value> uniqueKey, DomainObject obj, long time, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        delegate.notifyReadByUniqueKey(type, uniqueKey, obj, time, accessToken);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_READ_BY_UNIQUE_KEY.log(executionTime, false);
    }

    @Override
    public void notifyRead(Collection<DomainObject> objects, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        delegate.notifyRead(objects, accessToken);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_READ_MANY.log(executionTime, false);
    }

    @Override
    public void notifyReadAll(String type, boolean exactType, Collection<DomainObject> objects, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        delegate.notifyReadAll(type, exactType, objects, accessToken);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_READ_ALL.log(executionTime, false);
    }

    @Override
    public void notifyRead(Collection<Id> ids, Collection<DomainObject> objects, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        delegate.notifyRead(ids, objects, accessToken);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_READ_MANY_POSSIBLY_NULL.log(executionTime, false);
    }

    @Override
    public void notifyLinkedObjectsRead(Id id, String linkedType, String linkedField, boolean exactType, List<DomainObject> linkedObjects, long time, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        delegate.notifyLinkedObjectsRead(id, linkedType, linkedField, exactType, linkedObjects, time, accessToken);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_LINKED_OBJECTS_READ.log(executionTime, false);
    }

    @Override
    public void notifyLinkedObjectsIdsRead(Id id, String linkedType, String linkedField, boolean exactType, List<Id> linkedObjectsIds, long time, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        delegate.notifyLinkedObjectsIdsRead(id, linkedType, linkedField, exactType, linkedObjectsIds, time, accessToken);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_LINKED_OBJECTS_IDS_READ.log(executionTime, false);
    }

    @Override
    public void notifyCollectionCountRead(String name, List<? extends Filter> filterValues, int count, long time, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        delegate.notifyCollectionCountRead(name, filterValues, count, time, accessToken);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_COLLECTION_COUNT_READ.log(executionTime, false);
    }

    @Override
    public void notifyCollectionRead(String name, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        delegate.notifyCollectionRead(name, filterValues, sortOrder, offset, limit, collection, time, accessToken);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_COLLECTION_READ.log(executionTime, false);
    }

    @Override
    public void notifyCollectionRead(String query, List<? extends Value> paramValues, int offset, int limit, IdentifiableObjectCollection collection, long time, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        delegate.notifyCollectionRead(query, paramValues, offset, limit, collection, time, accessToken);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_COLLECTION_BY_QUERY_READ.log(executionTime, false);
    }

    @Override
    public void invalidateCurrentNode(CacheInvalidation cacheInvalidation) {
        final long t1 = System.nanoTime();
        delegate.invalidateCurrentNode(cacheInvalidation);
        final long executionTime = System.nanoTime() - t1;
        INVALIDATE.log(executionTime, false);
    }

    @Override
    public void notifyCommit(DomainObjectsModification modification) {
        final long t1 = System.nanoTime();
        delegate.notifyCommit(modification);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_COMMIT.log(executionTime, false);
    }

    @Override
    public void notifyRollback(String transactionId) {
        final long t1 = System.nanoTime();
        delegate.notifyRollback(transactionId);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_ROLLBACK.log(executionTime, false);
    }

    @Override
    public void notifyPersonGroupChanged(Id person) {
        final long t1 = System.nanoTime();
        delegate.notifyPersonGroupChanged(person);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_PERSON_GROUP_CHANGED.log(executionTime, false);
    }

    @Override
    public void notifyGroupBranchChanged(Id groupId) {
        final long t1 = System.nanoTime();
        delegate.notifyGroupBranchChanged(groupId);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_GROUP_BRANCH_CHANGED.log(executionTime, false);
    }

    @Override
    public void notifyAclCreated(Id contextObj, Collection<AclInfo> recordsInserted) {
        final long t1 = System.nanoTime();
        delegate.notifyAclCreated(contextObj, recordsInserted);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_ACL_CREATED.log(executionTime, false);
    }

    @Override
    public void notifyAclDeleted(Id contextObj, Collection<AclInfo> recordsDeleted) {
        final long t1 = System.nanoTime();
        delegate.notifyAclDeleted(contextObj, recordsDeleted);
        final long executionTime = System.nanoTime() - t1;
        NOTIFY_ACL_DELETED.log(executionTime, false);
    }

    @Override
    public DomainObject getDomainObject(Id id, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        final DomainObject result = delegate.getDomainObject(id, accessToken);
        final long executionTime = System.nanoTime() - t1;
        GET_DOMAIN_OBJECT.log(executionTime, result != null);
        return result;
    }

    @Override
    public DomainObject getDomainObject(String type, Map<String, Value> uniqueKey, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        final DomainObject result = delegate.getDomainObject(type, uniqueKey, accessToken);
        final long executionTime = System.nanoTime() - t1;
        GET_DOMAIN_OBJECT_BY_UNIQUE_KEY.log(executionTime, result != null);
        return result;
    }

    @Override
    public ArrayList<DomainObject> getDomainObjects(Collection<Id> ids, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        final ArrayList<DomainObject> result = delegate.getDomainObjects(ids, accessToken);
        final long executionTime = System.nanoTime() - t1;
        GET_DOMAIN_OBJECTS.log(executionTime, result != null);
        return result;
    }

    @Override
    public List<DomainObject> getLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        final List<DomainObject> result = delegate.getLinkedDomainObjects(domainObjectId, linkedType, linkedField, exactType, accessToken);
        final long executionTime = System.nanoTime() - t1;
        GET_LINKED_DOMAIN_OBJECTS.log(executionTime, result != null);
        return result;
    }

    @Override
    public List<Id> getLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        final List<Id> result = delegate.getLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, exactType, accessToken);
        final long executionTime = System.nanoTime() - t1;
        GET_LINKED_DOMAIN_OBJECTS_IDS.log(executionTime, result != null);
        return result;
    }

    @Override
    public List<DomainObject> getAllDomainObjects(String type, boolean exactType, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        final List<DomainObject> result = delegate.getAllDomainObjects(type, exactType, accessToken);
        final long executionTime = System.nanoTime() - t1;
        GET_ALL_DOMAIN_OBJECTS.log(executionTime, result != null);
        return result;
    }

    @Override
    public int getCollectionCount(String name, List<? extends Filter> filterValues, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        final int result = delegate.getCollectionCount(name, filterValues, accessToken);
        final long executionTime = System.nanoTime() - t1;
        GET_COLLECTION_COUNT.log(executionTime, result != -1);
        return result;
    }

    @Override
    public IdentifiableObjectCollection getCollection(String name, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        final IdentifiableObjectCollection result = delegate.getCollection(name, filterValues, sortOrder, offset, limit, accessToken);
        final long executionTime = System.nanoTime() - t1;
        GET_COLLECTION.log(executionTime, result != null);
        return result;
    }

    @Override
    public IdentifiableObjectCollection getCollection(String query, List<? extends Value> paramValues, int offset, int limit, AccessToken accessToken) {
        final long t1 = System.nanoTime();
        final IdentifiableObjectCollection result = delegate.getCollection(query, paramValues, offset, limit, accessToken);
        final long executionTime = System.nanoTime() - t1;
        GET_COLLECTION_BY_QUERY.log(executionTime, result != null);
        return result;
    }

    @Override
    public GlobalCacheStatistics getStatistics() {
        final GlobalCacheStatistics statistics = delegate.getStatistics();
        final ArrayList<MethodStatistics> gettersStats = ObjectCloner.getInstance().cloneObject(gettersStatistics);
        final ArrayList<MethodStatistics> notifiersStats = ObjectCloner.getInstance().cloneObject(notifiersStatistics);
        final ArrayList<MethodStatistics> allMethodsStats = ObjectCloner.getInstance().cloneObject(allMethodsStatistics);
        final MethodStatistics gettersSummary = MethodStatistics.summarize(gettersStats);
        final MethodStatistics notifiersSummary = MethodStatistics.summarize(notifiersStats);
        final MethodStatistics allMethodsSummary = MethodStatistics.summarize(allMethodsStats);
        statistics.setAllMethodsSummary(new GlobalCacheStatistics.Record("Global Summary", allMethodsSummary.getHourlyCounter(), 1.0f, allMethodsSummary.getTotalCounter(), 1.0f));
        statistics.setNotifiersSummary(getSummaryRecord("Write Summary", notifiersSummary, allMethodsSummary));
        statistics.setReadersSummary(getSummaryRecord("Read Summary", gettersSummary, allMethodsSummary));
        statistics.setReadersRecords(getRecords(gettersStats, allMethodsSummary));
        statistics.setNotifiersRecords(getRecords(notifiersStats, allMethodsSummary));
        return statistics;
    }

    protected ArrayList<GlobalCacheStatistics.Record> getRecords(ArrayList<MethodStatistics> methodsStatistics, MethodStatistics globalStatistics) {
        final ArrayList<GlobalCacheStatistics.Record> records = new ArrayList<>(methodsStatistics.size());
        for (MethodStatistics methodStats : methodsStatistics) {
            final double[] frequencies = getFrequencies(methodStats, globalStatistics);
            records.add(new GlobalCacheStatistics.Record(
                    methodStats.getMethodDescription(), methodStats.getHourlyCounter(), frequencies[0], methodStats.getTotalCounter(), frequencies[1]));
        }
        return records;
    }

    private GlobalCacheStatistics.Record getSummaryRecord(String name, MethodStatistics statistics, MethodStatistics globalStatistics) {
        final double[] frequencies = getFrequencies(statistics, globalStatistics);
        return new GlobalCacheStatistics.Record(name, statistics.getHourlyCounter(), frequencies[0], statistics.getTotalCounter(), frequencies[1]);
    }

    private double[] getFrequencies(MethodStatistics statistics, MethodStatistics globalStatistics) {
        final double hourlyFrequency = divide(statistics.getHourlyCounter().getEventCount(), globalStatistics.getHourlyCounter().getEventCount());
        final double totalFrequency = divide(statistics.getTotalCounter().getEventCount(), globalStatistics.getTotalCounter().getEventCount());
        return new double[] {hourlyFrequency, totalFrequency};
    }

    private double divide(long n1, long n2) {
        return n2 == 0 ? 0.0 : new BigDecimal(n1).divide(new BigDecimal(n2), 15, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    @Override
    public void clearStatistics(boolean hourlyOnly) {
        for (MethodStatistics methodStatistics : allMethodsStatistics) {
            methodStatistics.reset(hourlyOnly);
        }
        delegate.clearStatistics(hourlyOnly);
    }

    @Override
    public void setCollectionsDao(CollectionsDao collectionsDao) {
        delegate.setCollectionsDao(collectionsDao);
    }
}
