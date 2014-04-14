package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiServerHelper;
import ru.intertrust.cm.core.gui.api.server.widget.LabelRenderer;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.01.14
 *         Time: 11:40
 */
@ComponentName("custom.renderer")
public class CustomRenderer extends LabelRenderer {
    private static final char BLANK = ' ';

    @Override
    public String composeString(FieldPath[] fieldPaths, WidgetContext context) {
        final StringBuilder sb = new StringBuilder("custom render ");
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
        for (FieldPath fieldPath : fieldPaths) {
            final Value value = context.getValue(fieldPath);
            if (value != null) {
                final String displayValue;
                final Object primitiveValue = value.get();
                if (primitiveValue == null) {
                    if (value instanceof LongValue || value instanceof DecimalValue) {
                        displayValue = "0";
                    } else {
                        displayValue = "";
                    }
                } else {
                    if (value instanceof DateTimeValue) {
                        displayValue = dateFormatter.format(primitiveValue);
                    } else if (value instanceof TimelessDateValue) {
                        final TimelessDate timelessDate = ((TimelessDateValue) value).get();
                        final TimeZone timeZone = TimeZone.getTimeZone(GuiContext.get().getUserInfo().getTimeZoneId());
                        final Calendar calendar = GuiServerHelper.timelessDateToCalendar(timelessDate, timeZone);
                        displayValue = dateFormatter.format(calendar.getTime());
                    } else if (value instanceof DateTimeWithTimeZoneValue) {
                        final DateTimeWithTimeZone withTimeZone = ((DateTimeWithTimeZoneValue) value).get();
                        final Calendar calendar = GuiServerHelper.dateTimeWithTimezoneToCalendar(withTimeZone);
                        displayValue = dateFormatter.format(calendar.getTime());
                    } else {
                        displayValue = primitiveValue.toString();
                    }
                }
                sb.append(displayValue).append(BLANK);
            }
        }
        return sb.toString();
    }
}

