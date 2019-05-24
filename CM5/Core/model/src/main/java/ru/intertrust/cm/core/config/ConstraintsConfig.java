package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 18:48
 */
public class ConstraintsConfig implements Serializable {

    @ElementListUnion({
            @ElementList(entry="simple-constraint", type=SimpleConstraintConfig.class, inline=true, required = false),
            @ElementList(entry="length", type=LengthConstraintConfig.class, inline=true, required = false),
            @ElementList(entry="int-range", type=IntRangeConstraintConfig.class, inline=true, required = false),
            @ElementList(entry="dec-range", type=DecimalRangeConstraintConfig.class, inline=true, required = false),
            @ElementList(entry="date-range", type=DateRangeConstraintConfig.class, inline=true, required = false),
    })
    private List<ConstraintConfig> constraintConfigs = new ArrayList<ConstraintConfig>();

    public List<ConstraintConfig> getConstraintConfigs() {
        return constraintConfigs;
    }

    public void setConstraintConfigs(List<ConstraintConfig> constraintConfigs) {
        if (constraintConfigs != null) {
            this.constraintConfigs = constraintConfigs;
        } else {
            this.constraintConfigs.clear();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConstraintsConfig that = (ConstraintsConfig) o;
        if (constraintConfigs != null ? !constraintConfigs.equals(that.constraintConfigs) :
                that.constraintConfigs != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return constraintConfigs != null ? constraintConfigs.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ConstraintsConfig [constraintConfigs=" + constraintConfigs + "]";
    }
    
    
}
