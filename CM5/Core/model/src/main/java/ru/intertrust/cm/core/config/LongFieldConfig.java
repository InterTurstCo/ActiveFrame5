package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.FieldType;

import java.util.HashMap;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 11:12 AM
 */
public class LongFieldConfig extends FieldConfig {

    @Override
    public FieldType getFieldType() {
        return FieldType.LONG;
    }

    @Override
    public List<Constraint> getConstraints() {
        List<Constraint> constraints = super.getConstraints();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constraint.PARAM_PATTERN, Constraint.KEYWORD_INTEGER);
        constraints.add(new Constraint(Constraint.Type.SIMPLE, params));

        return constraints;
    }

    @Override
    void addConstraintsFromConfig(List<Constraint> constraints) {
        ConstraintsConfig constraintsConfig = getConstraintsConfig();
        if (constraintsConfig != null) {
            for (ConstraintConfig cnstrConfig : constraintsConfig.getConstraintConfigs()) {
                Constraint constraint = cnstrConfig.getConstraint();
                if (constraint != null) {
                    if (Constraint.Type.RANGE == constraint.getType()) {
                        constraint.addParam(Constraint.PARAM_FIELD_TYPE, Constraint.TYPE_LONG);
                    }
                    constraints.add(constraint);
                }
            }
        }
    }
}
