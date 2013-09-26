package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;

import java.io.Serializable;

/**
 * @author vmatsukevich
 *         Date: 5/16/13
 *         Time: 10:58 AM
 */
public class ReferenceFieldTypeConfig implements Serializable {

    @Attribute(name = "name")
    private String name;

    public ReferenceFieldTypeConfig() {
    }

    public ReferenceFieldTypeConfig(String name) {
        this.name = name;
    }

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

        ReferenceFieldTypeConfig that = (ReferenceFieldTypeConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ReferenceFieldTypeConfig{" +
                "name='" + name + '\'' +
                '}';
    }
}
