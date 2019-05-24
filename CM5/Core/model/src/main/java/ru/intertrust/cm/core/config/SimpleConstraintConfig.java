package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Constraint;

import java.util.HashMap;

/**
 *
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

    @Override
    public Constraint getConstraint() {
        if (value == null) {
            return  null;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constraint.PARAM_PATTERN, value);
        return new Constraint(Constraint.Type.SIMPLE, params);
    }

    @Override
    public String toString() {
        return "SimpleConstraintConfig [value=" + value + "]";
    }
    
    
}
