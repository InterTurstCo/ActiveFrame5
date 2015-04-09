package ru.intertrust.cm.core.config.gui;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class DomainObjectContextConfig implements Dto {
    private static final long serialVersionUID = -4022077140793668753L;

    @Element(name = "type", required=false)
    private String type;

    @Element(name = "status", required=false)
    private String status;

    @ElementList(entry = "attribute", inline = true, required=false)
    private List<AttrValueContextConfig> attribute;

    @ElementList(entry = "class-name", inline = true, required=false)
    private List<String> className;

    public String getDomainObjectType() {
        return type;
    }

    public void setDomainObjectType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<AttrValueContextConfig> getAttribute() {
        return attribute;
    }

    public void setAttribute(List<AttrValueContextConfig> attribute) {
        this.attribute = attribute;
    }

    public List<String> getClassName() {
        return className;
    }

    public void setClassName(List<String> className) {
        this.className = className;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DomainObjectContextConfig other = (DomainObjectContextConfig) obj;
        if (attribute == null) {
            if (other.attribute != null)
                return false;
        } else if (!attribute.equals(other.attribute))
            return false;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
