package ru.intertrust.cm.core.config;

import java.io.Serializable;

import org.simpleframework.xml.Element;

/**
 * Представляет набор сущностей, составляющих контекст роли.

 * @author atsvetkov
 */
public class ContextConfig implements Serializable {

    @Element(name = "domain-object", required = true)
    private DomainObjectConfig domainObject;

    public DomainObjectConfig getDomainObject() {
        return domainObject;
    }

    public void setDomainObject(DomainObjectConfig domainObject) {
        this.domainObject = domainObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContextConfig that = (ContextConfig) o;

        if (domainObject != null ? !domainObject.equals(that.domainObject) : that.domainObject != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = domainObject != null ? domainObject.hashCode() : 0;
        return result;
    }

}
