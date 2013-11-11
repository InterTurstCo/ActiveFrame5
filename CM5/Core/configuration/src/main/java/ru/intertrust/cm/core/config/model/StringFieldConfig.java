package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.FieldType;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 11:09 AM
 */
public class StringFieldConfig extends FieldConfig {
    @Attribute(required = true)
    private int length;

    @Attribute(required = false)
    private String encrypted;

    public StringFieldConfig() {
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        StringFieldConfig that = (StringFieldConfig) o;

        if (length != that.length) {
            return false;
        }

        if (encrypted != that.encrypted) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + length;
        return result;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.STRING;
    }
}
