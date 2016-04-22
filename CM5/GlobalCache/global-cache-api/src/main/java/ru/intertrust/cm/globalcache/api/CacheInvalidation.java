package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.HashSet;
import java.util.Random;

/**
 * @author Denis Mitavskiy
 *         Date: 21.04.2016
 *         Time: 17:49
 */
public class CacheInvalidation implements Dto {
    public static final long NODE_ID = new Random().nextLong();

    private long nodeId;
    private HashSet<Id> idsToInvalidate;
    private boolean clearFullAccessLog;

    public CacheInvalidation() {
    }

    public CacheInvalidation(HashSet<Id> idsToInvalidate, boolean clearFullAccessLog) {
        this.nodeId = NODE_ID;
        this.idsToInvalidate = idsToInvalidate;
        this.clearFullAccessLog = clearFullAccessLog;
    }

    public long getNodeId() {
        return nodeId;
    }

    public HashSet<Id> getIdsToInvalidate() {
        return idsToInvalidate;
    }

    public boolean isClearFullAccessLog() {
        return clearFullAccessLog;
    }

    public boolean fromThisNode() {
        return this.nodeId == NODE_ID;
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
