package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Constraint;

import java.util.HashMap;

/**
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 18:22
 */
public class RangeConstraintConfig extends ConstraintConfig {

    @Attribute(name="start", required = false)
    private String start;
    
    @Attribute(name="end", required = false)
    private String end;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RangeConstraintConfig that = (RangeConstraintConfig) o;

        if (end != null ? !end.equals(that.end) : that.end != null) {
            return false;
        }
        if (start != null ? !start.equals(that.start) : that.start != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }

    @Override
    public Constraint getConstraint() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(Constraint.PARAM_RANGE_START, start);
        params.put(Constraint.PARAM_RANGE_END, end);
        return new Constraint(Constraint.CONSTRAINT_RANGE_END, params);
    }
}
