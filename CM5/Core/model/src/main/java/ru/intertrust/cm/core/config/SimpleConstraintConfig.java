package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

/**
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 18:17
 */
public class SimpleConstraintConfig extends ConstraintConfig {

    @Attribute(required = true)
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleConstraintConfig that = (SimpleConstraintConfig) o;
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
