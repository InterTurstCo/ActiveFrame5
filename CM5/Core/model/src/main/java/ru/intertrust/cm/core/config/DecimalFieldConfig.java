package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.FieldType;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 11:12 AM
 */
public class DecimalFieldConfig extends FieldConfig {
    @Attribute(required = false)
    private Integer precision; // total number of digits, for instance 12.34567 - precision is 7, scale is 5

    @Attribute(required = false)
    private Integer scale;

    public DecimalFieldConfig() {
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
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

        DecimalFieldConfig that = (DecimalFieldConfig) o;

        if (precision != null ? !precision.equals(that.precision) : that.precision != null) {
            return false;
        }
        if (scale != null ? !scale.equals(that.scale) : that.scale != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (precision != null ? precision.hashCode() : 0);
        result = 31 * result + (scale != null ? scale.hashCode() : 0);
        return result;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.DECIMAL;
    }
}
