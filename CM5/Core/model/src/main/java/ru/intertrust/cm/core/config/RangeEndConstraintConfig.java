package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

/**
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 18:22
 */
public class RangeEndConstraintConfig extends ConstraintConfig {

    @Attribute(required = true)
    private String value; //TODO: [validation] will we be able to use single range config for int, decimal, and dates?

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
        RangeEndConstraintConfig that = (RangeEndConstraintConfig) o;
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
