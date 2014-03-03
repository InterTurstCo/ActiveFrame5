package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.Constraint;

/**
 * @author Lesia Puhova
 *         Date: 03.03.14
 *         Time: 13:43
 */
public class IntRangeConstraintConfig extends RangeConstraintConfig {

    @Override
    Constraint.Type getType() {
        return Constraint.Type.INT_RANGE;
    }
}
