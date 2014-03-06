package ru.intertrust.cm.core.gui.impl.server.validation.validators;

import ru.intertrust.cm.core.business.api.dto.Constraint;

/**
 * @author Lesia Puhova
 *         Date: 03.03.14
 *         Time: 14:56
 */
public class IntRangeValidator extends RangeValidator<Long> {

    public IntRangeValidator(Constraint constraint) {
        super(constraint);
    }

    @Override
    Long convert(String s) {
        if (s == null) {
            return null;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }
}
