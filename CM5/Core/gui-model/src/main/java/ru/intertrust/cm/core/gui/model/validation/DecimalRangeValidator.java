package ru.intertrust.cm.core.gui.model.validation;

import ru.intertrust.cm.core.business.api.dto.Constraint;

import java.math.BigDecimal;

/**
 * @author Lesia Puhova
 *         Date: 03.03.14
 *         Time: 14:56
 */
public class DecimalRangeValidator extends RangeValidator<BigDecimal> {

    public DecimalRangeValidator(Constraint constraint) {
        super(constraint);
    }

    @Override
    protected BigDecimal convert(String s) {
        if (s == null) {
            return null;
        }
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

}
