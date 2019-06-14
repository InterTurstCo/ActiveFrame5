package ru.intertrust.cm.core.business.api.dto.globalcache;

public class CheckResultItem {
    private String nodeName;
    private Boolean result;
        
    public CheckResultItem(String nodeName, Boolean result) {
        super();
        this.nodeName = nodeName;
        this.result = result;
    }
    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    public Boolean getResult() {
        return result;
    }
    public void setResult(Boolean result) {
        this.result = result;
    }
}
