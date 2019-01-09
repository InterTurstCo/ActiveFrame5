package ru.intertrust.cm.core.business.api.dto;

import java.util.*;

import ru.intertrust.cm.core.business.api.dto.globalcache.PingData;

/**
 * @author Denis Mitavskiy
 *         Date: 21.04.2016
 *         Time: 17:49
 */
public class CacheInvalidation implements Dto {
    public static final long NODE_ID = new Random().nextLong();
    private static final StringBuilder NULL_STR = new StringBuilder("null");

    private long senderId;
    private List<DomainObject> createdDomainObjectsToInvalidate;
    private Set<Id> createdIdsToInvalidate;
    private Set<Id> idsToInvalidate;
    private boolean clearFullAccessLog;
    private boolean clearCache;
    private long receiveTime;
    private Set<Id> usersAccessToInvalidate;
    private PingData pingData;

    public CacheInvalidation() {
    }

    public CacheInvalidation(boolean clearCache) {
        this.clearCache = clearCache;
    }

    public CacheInvalidation(Set<Id> idsToInvalidate, boolean clearFullAccessLog) {
        this.idsToInvalidate = idsToInvalidate;
        this.clearFullAccessLog = clearFullAccessLog;
    }

    public CacheInvalidation(Set<Id> idsToInvalidate, boolean clearFullAccessLog, Set<Id> personsWhosGroupsChanged) {
        this(idsToInvalidate, clearFullAccessLog);
        this.usersAccessToInvalidate = personsWhosGroupsChanged;
    }

    public CacheInvalidation(Set<Id> createdIds, HashSet<Id> idsToInvalidate, boolean clearFullAccessLog, HashSet<Id> personsWhosGroupsChanged) {
        this(idsToInvalidate, clearFullAccessLog, personsWhosGroupsChanged);
        this.createdIdsToInvalidate = createdIds;
    }

    public CacheInvalidation(List<DomainObject> createdDomainObjects, HashSet<Id> idsToInvalidate, boolean clearFullAccessLog, HashSet<Id> personsWhosGroupsChanged) {
        this(idsToInvalidate, clearFullAccessLog, personsWhosGroupsChanged);
        this.createdDomainObjectsToInvalidate = createdDomainObjects;
    }

    public long getSenderId() {
        return senderId;
    }

    public long setSenderId() {
        return senderId = NODE_ID;
    }

    public Set<Id> getUsersAccessToInvalidate() {
        return usersAccessToInvalidate == null ? Collections.<Id>emptySet() : usersAccessToInvalidate;
    }

    public void setUsersAccessToInvalidate(Set<Id> usersAccessToInvalidate) {
        this.usersAccessToInvalidate = usersAccessToInvalidate;
    }

    public List<DomainObject> getCreatedDomainObjectsToInvalidate() {
        return createdDomainObjectsToInvalidate == null ? Collections.<DomainObject>emptyList() : createdDomainObjectsToInvalidate;
    }

    public void setCreatedDomainObjectsToInvalidate(List<DomainObject> createdDomainObjectsToInvalidate) {
        this.createdDomainObjectsToInvalidate = createdDomainObjectsToInvalidate;
    }

    public Set<Id> getCreatedIdsToInvalidate() {
        return createdIdsToInvalidate == null ? Collections.<Id>emptySet() : idsToInvalidate;
    }

    public Set<Id> getIdsToInvalidate() {
        return idsToInvalidate == null ? Collections.<Id>emptySet() : idsToInvalidate;
    }

    public boolean isClearCache() {
        return clearCache;
    }

    public boolean isClearFullAccessLog() {
        return clearFullAccessLog;
    }

    public boolean fromThisNode() {
        return this.senderId == NODE_ID;
    }

    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }

    public long getReceiveTime() {
        return receiveTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CacheInvalidation{");
        sb.append("senderId=").append(senderId);
        sb.append(", fromThisNode=").append(fromThisNode());
        sb.append(", createdIdsToInvalidate=").append(asString(createdIdsToInvalidate));
        sb.append(", idsToInvalidate=").append(asString(idsToInvalidate));
        sb.append(", clearFullAccessLog=").append(clearFullAccessLog);
        sb.append(", usersAccessToInvalidate=").append(usersAccessToInvalidate);
        sb.append('}');
        return sb.toString();
    }

    private static StringBuilder asString(Collection<Id> ids) {
        if (ids == null) {
            ids = Collections.emptyList();
        }
        StringBuilder result = new StringBuilder(ids.size() * 18 + 2);
        result.append('{');
        int i = 0;
        for (Id id : ids) {
            if (i++ != 0) {
                result.append(", ");
            }
            result.append(id.toStringRepresentation());
        }
        result.append('}');
        return result;
    }

    public PingData getPingData() {
        return pingData;
    }

    public void setPingData(PingData pingData) {
        this.pingData = pingData;
    }
}
