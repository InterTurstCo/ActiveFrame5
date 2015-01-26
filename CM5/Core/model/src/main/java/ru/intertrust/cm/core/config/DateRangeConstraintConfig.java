package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.Constraint;

import java.util.HashMap;

/**
 * @author Lesia Puhova
 *         Date: 03.03.14
 *         Time: 13:43
 */
public class DateRangeConstraintConfig extends RangeConstraintConfig {

    @Override
    public Constraint getConstraint() {
        String start = getStart();
        String end = getEnd();
        if (start == null && end == null) {
            return null;
        }
        HashMap<String, String> params = new HashMap<>();

        params.put(Constraint.PARAM_RANGE_START, start);
        params.put(Constraint.PARAM_RANGE_END, end);

        return new Constraint(getType(), params);
    }

    @Override
    Constraint.Type getType() {
        return Constraint.Type.DATE_RANGE;
    }
}
