package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementUnion;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class FindObjectsConfig implements Dto{
    
    @ElementUnion({
        @Element(name="class-name", type=FindObjectsClassConfig.class),
        @Element(name="doel", type=FindObjectsDoelConfig.class),
        @Element(name="query", type=FindObjectsQueryConfig.class)
     })
    private FindObjectsType findObjectType;

    public FindObjectsType getFindObjectType() {
        return findObjectType;
    }

    public void setFindObjectType(FindObjectsType findObjectType) {
        this.findObjectType = findObjectType;
    }
}
