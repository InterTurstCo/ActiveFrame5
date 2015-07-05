package ru.intertrust.cm.core.gui.impl.client.localization;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarModel;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.03.2015
 *         Time: 17:52
 */
public class PlatformCalendarModel extends CalendarModel {


    public PlatformCalendarModel() {

    }
    /**
     * Gets the date of month formatter.
     *
     * @return the day of month formatter
     */
    protected DateTimeFormat getDayOfMonthFormatter() {
        return PlatformDateTimeFormat.getDateTimeFormat("d");
    }

    /**
     * Gets the day of week formatter.
     *
     * @return the day of week formatter
     */
    protected DateTimeFormat getDayOfWeekFormatter() {
        return PlatformDateTimeFormat.getDateTimeFormat("ccccc");
    }

    /**
     * Gets the month and year formatter.
     *
     * @return the month and year formatter
     */
    protected DateTimeFormat getMonthAndYearFormatter() {
        return PlatformDateTimeFormat.getDateTimeFormat(DateTimeFormat.PredefinedFormat.YEAR_MONTH_ABBR);
    }

    /**
     * Gets the month formatter.
     *
     * @return the month formatter
     */
    protected DateTimeFormat getMonthFormatter() {
        return PlatformDateTimeFormat.getDateTimeFormat(DateTimeFormat.PredefinedFormat.MONTH_ABBR);
    }

    /**
     * Gets the year formatter.
     *
     * @return the year formatter
     */
    protected DateTimeFormat getYearFormatter() {
        return PlatformDateTimeFormat.getDateTimeFormat(DateTimeFormat.PredefinedFormat.YEAR);
    }
}
