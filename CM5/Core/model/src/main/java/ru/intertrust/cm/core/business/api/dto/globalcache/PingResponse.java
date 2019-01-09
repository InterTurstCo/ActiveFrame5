package ru.intertrust.cm.core.business.api.dto.globalcache;

public class PingResponse {
    private String nodeName;
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
}
