package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.FieldType;

import java.io.Serializable;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 10:55 AM
 */
public abstract class FieldConfig implements Serializable {

    @Element(name="constraints", required=false)
    private ConstraintsConfig constraintsConfigConfig;

    public ConstraintsConfig getConstraintsConfigConfig() {
        return constraintsConfigConfig;
    }

    public void setConstraintsConfigConfig(ConstraintsConfig constraintsConfigConfig) {
        this.constraintsConfigConfig = constraintsConfigConfig;
    }

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

        if (notNull != that.notNull) {
            return false;
        }
        if (constraintsConfigConfig != null ? !constraintsConfigConfig.equals(that.constraintsConfigConfig) : that
                .constraintsConfigConfig != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = constraintsConfigConfig != null ? constraintsConfigConfig.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (notNull ? 1 : 0);
        return result;
    }
}
