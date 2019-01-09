package ru.intertrust.cm.globalcacheclient.ping;

public class PingNodeInfo {
    private String nodeName;
    private long time;
    
    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
}
