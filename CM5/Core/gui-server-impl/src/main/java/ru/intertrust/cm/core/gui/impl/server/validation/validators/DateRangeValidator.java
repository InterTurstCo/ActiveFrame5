package ru.intertrust.cm.core.gui.impl.server.validation.validators;

import java.util.Date;

import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.model.FatalException;

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
        try {
            return ThreadSafeDateFormat.parse(s, DEFAULT_FORMAT);
        } catch (FatalException e) {
            return null;
        }
    }
}
