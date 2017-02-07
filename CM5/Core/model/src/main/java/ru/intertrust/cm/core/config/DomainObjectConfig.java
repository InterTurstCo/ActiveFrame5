package ru.intertrust.cm.core.config;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * Доменный объект, составляющий контекст динамической роли.
 * @author atsvetkov
 *
 */
public class DomainObjectConfig implements Serializable {
    private static final long serialVersionUID = -4021773651212774997L;

    @Attribute(required = true)
    private String type;

    @Attribute(required = false)
    private String status;

    @Element(required = false, data=true)
    private String filter;    
    
    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DomainObjectConfig that = (DomainObjectConfig) o;

        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (status != null ? !status.equals(that.status) : that.status != null) {
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
