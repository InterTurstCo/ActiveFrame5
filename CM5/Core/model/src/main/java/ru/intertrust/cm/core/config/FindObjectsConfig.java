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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((findObjectType == null) ? 0 : findObjectType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FindObjectsConfig other = (FindObjectsConfig) obj;
        if (findObjectType == null) {
            if (other.findObjectType != null) {
                return false;
            }
        } else if (!findObjectType.equals(other.findObjectType)) {
            return false;
        }
        return true;
    }        
}
