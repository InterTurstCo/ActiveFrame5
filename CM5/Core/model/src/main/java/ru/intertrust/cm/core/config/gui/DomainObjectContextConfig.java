package ru.intertrust.cm.core.config.gui;

import java.util.List;

import org.simpleframework.xml.ElementList;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class DomainObjectContextConfig implements Dto{

    @ElementList (entry = "type", inline = true)
    private List<String> type;

    @ElementList (entry = "status", inline = true)
    private List<String> status;

    public List<String> getDomainObjectType() {
        return type;
    }

    public void setDomainObjectType(List<String> type) {
        this.type = type;
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }

}
