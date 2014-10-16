package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.LabelRenderer;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Konstantin Gordeev on 14.10.2014.
 */
@ComponentName("age.renderer")
public class AgeRenderer extends LabelRenderer {

    private static final String BLANK = "";

    @Override
    public String composeString(FieldPath[] fieldPaths, WidgetContext context) {
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");

        for (FieldPath fieldPath : fieldPaths) {
            final Value value = context.getValue(fieldPath);
            if (value != null) {
                final String displayValue;
                final Object primitiveValue = value.get();

                if (value instanceof DateTimeValue) {
                    try {
                        displayValue = dateFormatter.format(primitiveValue);
                        Date birth_date = dateFormatter.parse(displayValue);
                        Date currentDate = new Date();
                        return String.valueOf(getDiffYears(birth_date, currentDate));
                    } catch (ParseException e) {
//                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
//                        e.printStackTrace();
                    }
                }
            }
        }

        return BLANK;
    }

    private int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            diff--;
        }
        return diff;
    }

    private Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }
}