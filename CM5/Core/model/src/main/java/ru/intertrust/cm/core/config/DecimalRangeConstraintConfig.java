package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.Constraint;

/**
 * @author Lesia Puhova
 *         Date: 03.03.14
 *         Time: 13:43
 */
public class DecimalRangeConstraintConfig extends RangeConstraintConfig {

    @Override
    Constraint.Type getType() {
        return Constraint.Type.DECIMAL_RANGE;
    }
}
