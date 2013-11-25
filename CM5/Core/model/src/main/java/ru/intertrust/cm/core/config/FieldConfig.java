package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.FieldType;

import java.io.Serializable;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 10:55 AM
 */
public abstract class FieldConfig implements Serializable {
    @Attribute(name = "name")
    private String name;

    @Attribute(name = "not-null", required = false)
    private boolean notNull;

    protected FieldConfig() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public abstract FieldType getFieldType();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldConfig that = (FieldConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (notNull != that.notNull) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
