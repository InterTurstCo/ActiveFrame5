package ru.intertrust.cm.core.config.search;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;

public class DomainObjectFilterConfig implements Serializable {

    @Attribute(name = "java-class", required = true)
    private String javaClass;

    public String getJavaClass() {
        return javaClass;
    }

    @Override
    public int hashCode() {
        return javaClass.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        return javaClass.equals(((DomainObjectFilterConfig) obj).javaClass);
    }
}
