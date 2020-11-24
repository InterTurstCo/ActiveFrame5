package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.FieldType;

import java.util.HashMap;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 11:09 AM
 */
public class StringFieldConfig extends FieldConfig {
    @Attribute(required = true)
    private int length;

    @Attribute(required = false)
    private Boolean encrypted;

    @Attribute(name = "default-value", required = false)
    private String defaultValue;

    public StringFieldConfig() {
    }

    public StringFieldConfig(String name, boolean notNull, boolean immutable, int length, Boolean encrypted) {
        this(name, notNull, immutable, length, encrypted, null);
    }

    public StringFieldConfig(String name, boolean notNull, boolean immutable, int length, Boolean encrypted, String defaultValue) {
        super(name, notNull, immutable);
        this.length = length;
        this.encrypted = encrypted;
        this.defaultValue = defaultValue;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Boolean getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean isEncrypted() {
        return Boolean.TRUE.equals(encrypted);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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

        if (encrypted != null ? !encrypted.equals(that.encrypted) : that.encrypted != null) {
            return false;
        }

        if (defaultValue != null ? !defaultValue.equals(that.defaultValue) : that.defaultValue != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + length;
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.STRING;
    }

    @Override
    public List<Constraint> getConstraints() {
        List<Constraint> constraints = super.getConstraints();

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constraint.PARAM_MAX_LENGTH, length + "");
        constraints.add(new Constraint(Constraint.Type.LENGTH, params));

        return constraints;
    }
}
