package ru.intertrust.cm.core.gui.impl.server.validation.validators;

import ru.intertrust.cm.core.business.api.dto.Constraint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Lesia Puhova
 *         Date: 06.03.14
 *         Time: 15:49
 */
public class DateRangeValidator extends RangeValidator<Date> {

    private static final String DEFAULT_FORMAT = "dd.MM.yyyy";

    public DateRangeValidator(Constraint constraint) {
        super(constraint);
    }

    @Override
    Date convert(String s) {
        SimpleDateFormat format = new SimpleDateFormat(DEFAULT_FORMAT);
        try {
            return format.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }
}
