package ru.intertrust.cm.core.gui.impl.server.widget;

import java.text.DateFormat;
import java.util.Date;

import ru.intertrust.cm.core.gui.model.DateTimeContext;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
public interface DateValueConverter<T> {
    DateTimeContext valueToContext(T value, String timeZoneId, DateFormat dateFormat);

    T contextToValue(DateTimeContext dateTimeContext);

    Date valueToDate(T value, String timeZoneId);
}
