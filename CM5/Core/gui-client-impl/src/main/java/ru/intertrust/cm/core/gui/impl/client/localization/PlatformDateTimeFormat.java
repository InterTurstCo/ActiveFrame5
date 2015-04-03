package ru.intertrust.cm.core.gui.impl.client.localization;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormatInfo;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.03.2015
 *         Time: 18:57
 */
public class PlatformDateTimeFormat extends DateTimeFormat {
    public PlatformDateTimeFormat(String pattern) {
        super(pattern);
    }

    public static DateTimeFormat getDateTimeFormat(String pattern, DateTimeFormatInfo dtfi) {
        return dtfi == null ? DateTimeFormat.getFormat(pattern) : DateTimeFormat.getFormat(pattern, dtfi);
    }
    public static DateTimeFormat getDateTimeFormat(DateTimeFormat.PredefinedFormat predefinedFormat, DateTimeFormatInfo dtfi) {
        return dtfi == null ? DateTimeFormat.getFormat(predefinedFormat)
                : DateTimeFormat.getFormat(DateTimeFormat.getFormat(predefinedFormat).getPattern(), dtfi);
    }
}
