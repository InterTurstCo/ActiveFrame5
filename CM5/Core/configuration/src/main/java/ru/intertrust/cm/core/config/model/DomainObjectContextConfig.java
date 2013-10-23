package ru.intertrust.cm.core.config.model;

import java.util.List;

import org.simpleframework.xml.ElementList;

public class DomainObjectContextConfig {

    @ElementList (entry = "type", inline = true)
    private List<DomainObjTypeConfig> domainObjectType;

    @ElementList (entry = "status", inline = true)
    private List<String> status;

    public List<DomainObjTypeConfig> getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(List<DomainObjTypeConfig> domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }

}
