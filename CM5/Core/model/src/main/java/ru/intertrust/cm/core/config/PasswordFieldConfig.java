package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.FieldType;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 11:09 AM
 */
public class PasswordFieldConfig extends FieldConfig {
    @Attribute(required = true)
    private int length;

    public PasswordFieldConfig() {
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public boolean equals(Object o, boolean ignoreNonDataStructureFields) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o, ignoreNonDataStructureFields)) {
            return false;
        }

        PasswordFieldConfig that = (PasswordFieldConfig) o;

        if (length != that.length) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        return equals(o, false);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + length;
        return result;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.PASSWORD;
    }
}
