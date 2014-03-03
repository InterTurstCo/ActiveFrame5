package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.Constraint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

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
            Date dateStart = new SimpleDateFormat(DEFAULT_FORMAT).parse(start);
            Date dateEnd = new SimpleDateFormat(DEFAULT_FORMAT).parse(end);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(Constraint.PARAM_RANGE_START, dateStart.getTime() + ""); //number of milliseconds as a string
            params.put(Constraint.PARAM_RANGE_END, dateEnd.getTime() + "");

            params.put(Constraint.PARAM_RANGE_START_FOR_MSG, start);
            params.put(Constraint.PARAM_RANGE_END_FOR_MSG, end);

            return new Constraint(getType(), params);
        } catch (ParseException e) {
            // TODO: [validation] report error in config?
            return null;
        }
    }

    @Override
    Constraint.Type getType() {
        return Constraint.Type.DATE_RANGE;
    }
}
