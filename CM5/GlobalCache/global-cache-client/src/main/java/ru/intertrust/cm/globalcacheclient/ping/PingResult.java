package ru.intertrust.cm.globalcacheclient.ping;

import java.util.ArrayList;
import java.util.List;

public class PingResult {
    private String requestId;
    private String initiator;
    private List<PingNodeInfo> nodeInfos = new ArrayList<>();

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public List<PingNodeInfo> getNodeInfos() {
        return nodeInfos;
    }

    public void setNodeInfos(List<PingNodeInfo> nodeInfos) {
        this.nodeInfos = nodeInfos;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }
    
    

}
