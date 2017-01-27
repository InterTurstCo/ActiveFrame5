package ru.intertrust.cm.core.rest.api;

import java.util.Map;

public class GenerateReportParam {
    private String name;
    private Map params;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Map getParams() {
        return params;
    }
    public void setParams(Map params) {
        this.params = params;
    }
}
