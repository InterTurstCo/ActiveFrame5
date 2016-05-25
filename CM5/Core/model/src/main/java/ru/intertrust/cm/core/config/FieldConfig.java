package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.FieldType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 10:55 AM
 */
public abstract class FieldConfig implements Serializable {

    @Element(name="constraints", required=false)
    private ConstraintsConfig constraintsConfig;

    public ConstraintsConfig getConstraintsConfig() {
        return constraintsConfig;
    }

    public void setConstraintsConfig(ConstraintsConfig constraintsConfig) {
        this.constraintsConfig = constraintsConfig;
    }

    @Attribute(name = "name")
    private String name;

    @Attribute(name = "not-null", required = false)
    private boolean notNull;

    @Attribute(name = "immutable", required = false)
    private boolean immutable = false;

    protected FieldConfig() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public boolean isImmutable() {
        return immutable;
    }

    public void setImmutable(boolean immutable) {
        this.immutable = immutable;
    }

    public abstract FieldType getFieldType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldConfig that = (FieldConfig) o;

        if (notNull != that.notNull) return false;
        if (immutable != that.immutable) return false;
        if (constraintsConfig != null ? !constraintsConfig.equals(that.constraintsConfig) : that.constraintsConfig != null)
            return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public List<Constraint> getConstraints() {
        List<Constraint> constraints = new ArrayList<Constraint>();
        addNotNullConstraint(constraints);
        addConstraintsFromConfig(constraints);
        return constraints;
    }

    private void addNotNullConstraint(List<Constraint> constraints) {
        if (isNotNull()) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(Constraint.PARAM_PATTERN, Constraint.KEYWORD_NOT_EMPTY);
            constraints.add(new Constraint(Constraint.Type.SIMPLE, params));
        }
    }

    private void addConstraintsFromConfig(List<Constraint> constraints) {
        ConstraintsConfig constraintsConfig = getConstraintsConfig();
        if (constraintsConfig != null) {
            for (ConstraintConfig cnstrConfig : constraintsConfig.getConstraintConfigs()) {
                if (cnstrConfig.getConstraint() != null) {//TODO: [validation] we need some kind of policy to deal with duplicate and conflicting constraints
                    constraints.add(cnstrConfig.getConstraint());
                }
            }
        }
    }
}
