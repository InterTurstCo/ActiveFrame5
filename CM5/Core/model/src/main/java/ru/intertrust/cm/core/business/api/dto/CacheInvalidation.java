package ru.intertrust.cm.core.business.api.dto;

import java.util.Random;
import java.util.Set;

/**
 * @author Denis Mitavskiy
 *         Date: 21.04.2016
 *         Time: 17:49
 */
public class CacheInvalidation implements Dto {
    public static final long NODE_ID = new Random().nextLong();

    private long nodeId;
    private Set<Id> idsToInvalidate;
    private boolean clearFullAccessLog;
    private long receiveTime;

    public CacheInvalidation() {
    }

    public CacheInvalidation(Set<Id> idsToInvalidate, boolean clearFullAccessLog) {
        this.nodeId = NODE_ID;
        this.idsToInvalidate = idsToInvalidate;
        this.clearFullAccessLog = clearFullAccessLog;
        this.receiveTime = receiveTime;
    }

    public long getNodeId() {
        return nodeId;
    }

    public Set<Id> getIdsToInvalidate() {
        return idsToInvalidate;
    }

    public boolean isClearFullAccessLog() {
        return clearFullAccessLog;
    }

    public boolean fromThisNode() {
        return this.nodeId == NODE_ID;
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
        sb.append("nodeId=").append(nodeId);
        sb.append(", fromThisNode=").append(fromThisNode());
        sb.append(", idsToInvalidate=").append(idsToInvalidate);
        sb.append(", clearFullAccessLog=").append(clearFullAccessLog);
        sb.append('}');
        return sb.toString();
    }
}
