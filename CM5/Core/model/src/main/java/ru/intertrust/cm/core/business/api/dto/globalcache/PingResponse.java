package ru.intertrust.cm.core.business.api.dto.globalcache;

public class PingResponse {
    private String nodeName;
    private String nodeId;
    private long responseTime;

    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    public long getResponseTime() {
        return responseTime;
    }
    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
