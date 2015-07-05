package ru.intertrust.cm.core.gui.impl.client.validation;

import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.gui.impl.client.localization.PlatformDateTimeFormat;
import ru.intertrust.cm.core.gui.model.validation.RangeValidator;

import java.util.Date;

/**
 * @author Lesia Puhova
 *         Date: 03.03.14
 *         Time: 14:56
 */
public class DateRangeValidator extends RangeValidator<Date> {

    private final String pattern;

    public DateRangeValidator(Constraint constraint) {
        super(constraint, Constraint.PARAM_RANGE_START, Constraint.PARAM_RANGE_END);
        pattern = constraint.param(Constraint.PARAM_DATE_PATTERN);
    }

    @Override
    protected Date convert(String s) {
        if (s == null || pattern == null) {
            return null;
        }
        try {
            return PlatformDateTimeFormat.getFormat(pattern).parse(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
