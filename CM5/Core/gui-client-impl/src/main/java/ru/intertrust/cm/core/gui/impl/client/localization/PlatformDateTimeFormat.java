package ru.intertrust.cm.core.gui.impl.client.localization;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.impl.cldr.DateTimeFormatInfoImpl;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.03.2015
 *         Time: 18:57
 */
public class PlatformDateTimeFormat extends DateTimeFormat {
    private static DateTimeFormatInfoImpl dateTimeFormatInfo = new PlatformDateTimeFormatInfoImpl();
    public PlatformDateTimeFormat(String pattern) {
        super(pattern);
    }

    public static DateTimeFormat getDateTimeFormat(DateTimeFormat.PredefinedFormat predefinedFormat) {
        return DateTimeFormat.getFormat(DateTimeFormat.getFormat(predefinedFormat).getPattern(), dateTimeFormatInfo);

    }
    public static DateTimeFormat getFormat(String pattern){
        return DateTimeFormat.getFormat(pattern, dateTimeFormatInfo);
    }

    public static DateTimeFormat getDateTimeFormat(String pattern){
        return getFormat(pattern);
    }
}
