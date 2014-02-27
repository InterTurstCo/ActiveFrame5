package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Constraint;

import java.util.HashMap;

/**
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 18:19
 */
public class LengthConstraintConfig extends ConstraintConfig {

    @Attribute(required=false)
    private Integer value;

    @Attribute(name="min-value", required=false)
    private Integer minValue;

    @Attribute(name="max-value", required=false)
    private Integer maxValue;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LengthConstraintConfig that = (LengthConstraintConfig) o;
        if (maxValue != null ? !maxValue.equals(that.maxValue) : that.maxValue != null) {
            return false;
        }
        if (minValue != null ? !minValue.equals(that.minValue) : that.minValue != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (minValue != null ? minValue.hashCode() : 0);
        result = 31 * result + (maxValue != null ? maxValue.hashCode() : 0);
        return result;
    }

    @Override
    public Constraint getConstraint() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(Constraint.PARAM_LENGTH, value);
        params.put(Constraint.PARAM_MIN_LENGTH, minValue);
        params.put(Constraint.PARAM_MAX_LENGTH, maxValue);
        return new Constraint(Constraint.CONSTRAINT_LENGTH, params);
    }
}
