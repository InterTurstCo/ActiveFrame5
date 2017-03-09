package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class VersionInfo implements Dto{
    private String component;
    private String version;
    
    public String getComponent() {
        return component;
    }
    public void setComponent(String component) {
        this.component = component;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
}
