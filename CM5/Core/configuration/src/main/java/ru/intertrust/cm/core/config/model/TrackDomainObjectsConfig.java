package ru.intertrust.cm.core.config.model;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Специальная реализация, основанная на отслеживании создания, изменения и удаления доменных объектов заданного типа и статуса.
 * @author atsvetkov
 *
 */
@Root
public class TrackDomainObjectsConfig implements Serializable {

    @Attribute(name = "type", required = true)
    private String type;

    @Attribute(name = "status")
    private String status;

    @Element(name ="bind-context", required = false)    
    private BindContextConfig bindContext;
    
    @Element(name ="get-person", required = false)    
    private GetPersonConfig getPerson;
    
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
