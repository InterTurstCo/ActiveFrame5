package ru.intertrust.cm.core.gui.model.validation;

import ru.intertrust.cm.core.business.api.dto.Constraint;

/**
 * @author Lesia Puhova
 *         Date: 03.03.14
 *         Time: 14:56
 */
// actually compares number of milliseconds.
// TODO: [validation] do we have a better way to do this?
public class DateRangeValidator extends  RangeValidator<Long> {

    public DateRangeValidator(Constraint constraint) {
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
