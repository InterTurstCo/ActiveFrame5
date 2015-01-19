package ru.intertrust.cm.core.config;

import java.util.Date;
import java.util.HashMap;

import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.model.FatalException;

/**
 * @author Lesia Puhova
 *         Date: 03.03.14
 *         Time: 13:43
 */
public class DateRangeConstraintConfig extends RangeConstraintConfig {

    private static final String DEFAULT_FORMAT = "dd.MM.yyyy";

    @Override
    public Constraint getConstraint() {
        String start = getStart();
        String end = getEnd();
        if (start == null && end == null) {
            return null;
        }
        try {
            Date dateStart = ThreadSafeDateFormat.parse(start, DEFAULT_FORMAT);
            Date dateEnd = ThreadSafeDateFormat.parse(end, DEFAULT_FORMAT);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(Constraint.PARAM_RANGE_START_DATE_MS, dateStart.getTime() + ""); //number of milliseconds as a string
            params.put(Constraint.PARAM_RANGE_END_DATE_MS, dateEnd.getTime() + "");

            params.put(Constraint.PARAM_RANGE_START, start);
            params.put(Constraint.PARAM_RANGE_END, end);

            return new Constraint(getType(), params);
        } catch (FatalException e) {
            // TODO: [validation] report error in config?
            return null;
        }
    }

    @Override
    Constraint.Type getType() {
        return Constraint.Type.DATE_RANGE;
    }
}
