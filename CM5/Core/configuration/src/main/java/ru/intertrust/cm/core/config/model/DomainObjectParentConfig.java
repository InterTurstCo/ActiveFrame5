package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;

import java.io.Serializable;

/**
 * @author vmatsukevich
 *         Date: 7/2/13
 *         Time: 6:41 PM
 */
public class DomainObjectParentConfig implements Serializable {

    @Attribute(name = "name", required = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DomainObjectParentConfig that = (DomainObjectParentConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
