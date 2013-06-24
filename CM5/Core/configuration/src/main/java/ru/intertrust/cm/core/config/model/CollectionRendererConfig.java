package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;

import java.io.Serializable;

public class CollectionRendererConfig implements Serializable {

    @Attribute(name = "className", required = true)
    private String className;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionRendererConfig that = (CollectionRendererConfig) o;

        if (className != null ? !className.equals(that.className) : that.className != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return className != null ? className.hashCode() : 0;
    }
}
