package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Класс конфигурации коллектора контекстной роли
 * @author atsvetkov
 *
 */
@Root
public class TrackDomainObjectsConfig implements Dto{

    @Attribute(name = "type", required = false)
    private String type;

    @Attribute(name = "status", required = false)
    private String status;

    @Element(name ="bind-context", required = false)    
    private BindContextConfig bindContext;
    
    @Element(name = "get-group", required = false)
    private GetGroupConfig getGroup;

    public GetGroupConfig getGetGroup() {
        return getGroup;
    }

    public void setGetGroup(GetGroupConfig getGroup) {
        this.getGroup = getGroup;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
        
    public BindContextConfig getBindContext() {
        return bindContext;
    }

    public void setBindContext(BindContextConfig bindContext) {
        this.bindContext = bindContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrackDomainObjectsConfig that = (TrackDomainObjectsConfig) o;

        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (status != null ? !status.equals(that.status) : that.status != null) {
            return false;
        }
        if (getGroup != null ? !getGroup.equals(that.getGroup) : that.getGroup != null) {
            return false;
        }
        if (bindContext != null ? !bindContext.equals(that.bindContext) : that.bindContext != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }
}
