package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * 
 * @author atsvetkov
 *
 */
public abstract class NamedTypeConfig implements Dto {

    @Attribute(name = "name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (!(this instanceof NamedTypeConfig)) {
            return false;
        }
        NamedTypeConfig other = (NamedTypeConfig) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NamedTypeConfig [name=" + name + "]";
    }
}
