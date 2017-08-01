package ru.intertrust.cm.core.business.api.dto;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author Denis Mitavskiy
 *         Date: 21.04.2016
 *         Time: 17:49
 */
public class CacheInvalidation implements Dto {
    public static final long NODE_ID = new Random().nextLong();

    private long senderId;
    private Set<Id> idsToInvalidate;
    private boolean clearFullAccessLog;
    private boolean clearCache;
    private long receiveTime;
    private Set<Id> usersAccessToInvalidate;

    public CacheInvalidation() {
    }

    public CacheInvalidation(boolean clearCache) {
        this.clearCache = clearCache;
    }

    public CacheInvalidation(Set<Id> idsToInvalidate, boolean clearFullAccessLog) {
        this.idsToInvalidate = idsToInvalidate;
        this.clearFullAccessLog = clearFullAccessLog;
    }

    public CacheInvalidation(HashSet<Id> idsToInvalidate, boolean clearFullAccessLog, HashSet<Id> personsWhosGroupsChanged) {
        this(idsToInvalidate, clearFullAccessLog);
        this.usersAccessToInvalidate = personsWhosGroupsChanged;
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
        sb.append(", idsToInvalidate=").append(idsToInvalidate);
        sb.append(", clearFullAccessLog=").append(clearFullAccessLog);
        sb.append(", usersAccessToInvalidate=").append(usersAccessToInvalidate);
        sb.append('}');
        return sb.toString();
    }
}
