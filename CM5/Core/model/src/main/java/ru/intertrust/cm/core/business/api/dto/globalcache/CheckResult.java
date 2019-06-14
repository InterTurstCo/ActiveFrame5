package ru.intertrust.cm.core.business.api.dto.globalcache;

import java.util.HashMap;
import java.util.Map;

public class CheckResult {
    private String initiator;
    private Map<String, CheckResultItem> checkData = new HashMap<String, CheckResultItem>();

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public Map<String, CheckResultItem> getCheckData() {
        return checkData;
    }

    public void setCheckData(Map<String, CheckResultItem> checkData) {
        this.checkData = checkData;
    }
    
    
}
