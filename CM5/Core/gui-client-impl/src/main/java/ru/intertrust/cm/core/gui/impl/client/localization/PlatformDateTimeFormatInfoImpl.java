package ru.intertrust.cm.core.gui.impl.client.localization;

import com.google.gwt.i18n.client.impl.cldr.DateTimeFormatInfoImpl;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.*;
/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.03.2015
 *         Time: 17:23
 */
public class PlatformDateTimeFormatInfoImpl extends DateTimeFormatInfoImpl {
    @Override
    public String dateFormatFull() {
        return LocalizeUtil.get(DATE_FORMAT_FULL_KEY);
    }

    @Override
    public String dateFormatLong() {
        return LocalizeUtil.get(DATE_FORMAT_LONG_KEY);
    }

    @Override
    public String dateFormatMedium() {
        return LocalizeUtil.get(DATE_FORMAT_MEDIUM_KEY);
    }

    @Override
    public String dateFormatShort() {
        return LocalizeUtil.get(DATE_FORMAT_SHORT_KEY);
    }

    @Override
    public String dateTimeFull(String timePattern, String datePattern) {
        return datePattern + LocalizeUtil.get(DATE_SEPARATOR_KEY) + " " + timePattern;
    }

    @Override
    public String dateTimeLong(String timePattern, String datePattern) {
        return datePattern + LocalizeUtil.get(DATE_SEPARATOR_KEY) + " " + timePattern;
    }

    @Override
    public String dateTimeMedium(String timePattern, String datePattern) {
        return datePattern + LocalizeUtil.get(DATE_SEPARATOR_KEY) + " " + timePattern;
    }

    @Override
    public String dateTimeShort(String timePattern, String datePattern) {
        return datePattern + LocalizeUtil.get(DATE_SEPARATOR_KEY) + " " + timePattern;
    }

    @Override
    public String[] erasFull() {
        return new String[] {
                LocalizeUtil.get(ERA_BEFORE_FULL_KEY),
                LocalizeUtil.get(ERA_NOW_FULL_KEY)
        };
    }

    @Override
    public String[] erasShort() {
        return new String[] {
                LocalizeUtil.get(ERA_BEFORE_SHORT_KEY),
                LocalizeUtil.get(ERA_NOW_SHORT_KEY)
        };
    }
    @Override
    public String formatHour24Minute() {
        return LocalizeUtil.get(FORMAT_HOUR_24_MINUTE_KEY);
    }

    @Override
    public String formatHour24MinuteSecond() {
        return LocalizeUtil.get(FORMAT_HOUR_24_MINUTE_SECOND_KEY);
    }

    @Override
    public String formatMonthAbbrevDay() {
        return LocalizeUtil.get(FORMAT_MONTH_ABBREV_DAY_KEY);
    }

    @Override
    public String formatMonthFullDay() {
        return LocalizeUtil.get(FORMAT_MONTH_FULL_DAY_KEY);
    }

    @Override
    public String formatMonthFullWeekdayDay() {
        return LocalizeUtil.get(FORMAT_MONTH_FULL_WEEKDAY_DAY_KEY);
    }

    @Override
    public String formatMonthNumDay() {
        return LocalizeUtil.get(FORMAT_MONTH_NUM_DAY_KEY);
    }

    @Override
    public String formatYearMonthAbbrev() {
        return LocalizeUtil.get(FORMAT_YEAR_MONTH_ABBREV_KEY);
    }

    @Override
    public String formatYearMonthAbbrevDay() {
        return LocalizeUtil.get(FORMAT_YEAR_MONTH_ABBREV_DAY_KEY);
    }

    @Override
    public String formatYearMonthFull() {
        return LocalizeUtil.get(FORMAT_YEAR_MONTH_FULL_KEY);
    }

    @Override
    public String formatYearMonthFullDay() {
        return LocalizeUtil.get(FORMAT_YEAR_MONTH_FULL_DAY_KEY);
    }

    @Override
    public String formatYearMonthNum() {
        return LocalizeUtil.get(FORMAT_YEAR_MONTH_NUM_KEY);
    }

    @Override
    public String formatYearMonthNumDay() {
        return LocalizeUtil.get(FORMAT_YEAR_MONTH_NUM_DAY_KEY);
    }

    @Override
    public String formatYearMonthWeekdayDay() {
        return LocalizeUtil.get(FORMAT_YEAR_MONTH_WEEKDAY_DAY_KEY);
    }

    @Override
    public String formatYearQuarterFull() {
        return LocalizeUtil.get(FORMAT_YEAR_QUARTER_FULL_KEY);
    }

    @Override
    public String formatYearQuarterShort() {
        return LocalizeUtil.get(FORMAT_YEAR_QUARTER_SHORT_KEY);
    }

    @Override
    public String[] monthsNarrow() {
        return new String[] {
                LocalizeUtil.get(JANUARY_MONTH_NARROW_KEY),
                LocalizeUtil.get(FEBRUARY_MONTH_NARROW_KEY),
                LocalizeUtil.get(MARCH_MONTH_NARROW_KEY),
                LocalizeUtil.get(APRIL_MONTH_NARROW_KEY),
                LocalizeUtil.get(MAY_MONTH_NARROW_KEY),
                LocalizeUtil.get(JUN_MONTH_NARROW_KEY),
                LocalizeUtil.get(JULY_MONTH_NARROW_KEY),
                LocalizeUtil.get(AUGUST_MONTH_NARROW_KEY),
                LocalizeUtil.get(SEPTEMBER_MONTH_NARROW_KEY),
                LocalizeUtil.get(OCTOBER_MONTH_NARROW_KEY),
                LocalizeUtil.get(NOVEMBER_MONTH_NARROW_KEY),
                LocalizeUtil.get(DECEMBER_MONTH_NARROW_KEY)
        };
    }
    @Override
    public String[] monthsShort() {
        return new String[]{
                LocalizeUtil.get(JANUARY_MONTH_SHORT_KEY),
                LocalizeUtil.get(FEBRUARY_MONTH_SHORT_KEY),
                LocalizeUtil.get(MARCH_MONTH_SHORT_KEY),
                LocalizeUtil.get(APRIL_MONTH_SHORT_KEY),
                LocalizeUtil.get(MAY_MONTH_SHORT_KEY),
                LocalizeUtil.get(JUN_MONTH_SHORT_KEY),
                LocalizeUtil.get(JULY_MONTH_SHORT_KEY),
                LocalizeUtil.get(AUGUST_MONTH_SHORT_KEY),
                LocalizeUtil.get(SEPTEMBER_MONTH_SHORT_KEY),
                LocalizeUtil.get(OCTOBER_MONTH_SHORT_KEY),
                LocalizeUtil.get(NOVEMBER_MONTH_SHORT_KEY),
                LocalizeUtil.get(DECEMBER_MONTH_SHORT_KEY)
        };
    }

    @Override
    public String[] monthsShortStandalone() {
        return new String[]{
                LocalizeUtil.get(JANUARY_MONTH_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(FEBRUARY_MONTH_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(MARCH_MONTH_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(APRIL_MONTH_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(MAY_MONTH_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(JUN_MONTH_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(JULY_MONTH_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(AUGUST_MONTH_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(SEPTEMBER_MONTH_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(OCTOBER_MONTH_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(NOVEMBER_MONTH_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(DECEMBER_MONTH_SHORT_STANDALONE_KEY)
        };
    }

    @Override
    public String[] monthsFullStandalone() {
        return new String[] {
                LocalizeUtil.get(JANUARY_MONTH_FULL_STANDALONE_KEY),
                LocalizeUtil.get(FEBRUARY_MONTH_FULL_STANDALONE_KEY),
                LocalizeUtil.get(MARCH_MONTH_FULL_STANDALONE_KEY),
                LocalizeUtil.get(APRIL_MONTH_FULL_STANDALONE_KEY),
                LocalizeUtil.get(MAY_MONTH_FULL_STANDALONE_KEY),
                LocalizeUtil.get(JUN_MONTH_FULL_STANDALONE_KEY),
                LocalizeUtil.get(JULY_MONTH_FULL_STANDALONE_KEY),
                LocalizeUtil.get(AUGUST_MONTH_FULL_STANDALONE_KEY),
                LocalizeUtil.get(SEPTEMBER_MONTH_FULL_STANDALONE_KEY),
                LocalizeUtil.get(OCTOBER_MONTH_FULL_STANDALONE_KEY),
                LocalizeUtil.get(NOVEMBER_MONTH_FULL_STANDALONE_KEY),
                LocalizeUtil.get(DECEMBER_MONTH_FULL_STANDALONE_KEY)
        };
    }
    @Override
    public String[] monthsFull() {
        return new String[] {
                LocalizeUtil.get(JANUARY_MONTH_FULL_KEY),
                LocalizeUtil.get(FEBRUARY_MONTH_FULL_KEY),
                LocalizeUtil.get(MARCH_MONTH_FULL_KEY),
                LocalizeUtil.get(APRIL_MONTH_FULL_KEY),
                LocalizeUtil.get(MAY_MONTH_FULL_KEY),
                LocalizeUtil.get(JUN_MONTH_FULL_KEY),
                LocalizeUtil.get(JULY_MONTH_FULL_KEY),
                LocalizeUtil.get(AUGUST_MONTH_FULL_KEY),
                LocalizeUtil.get(SEPTEMBER_MONTH_FULL_KEY),
                LocalizeUtil.get(OCTOBER_MONTH_FULL_KEY),
                LocalizeUtil.get(NOVEMBER_MONTH_FULL_KEY),
                LocalizeUtil.get(DECEMBER_MONTH_FULL_KEY)
        };
    }

    @Override
    public String[] weekdaysFull() {
        return new String[] {
                LocalizeUtil.get(SUNDAY_FULL_KEY),
                LocalizeUtil.get(MONDAY_FULL_KEY),
                LocalizeUtil.get(TUESDAY_FULL_KEY),
                LocalizeUtil.get(WEDNESDAY_FULL_KEY),
                LocalizeUtil.get(THURSDAY_FULL_KEY),
                LocalizeUtil.get(FRIDAY_FULL_KEY),
                LocalizeUtil.get(SATURDAY_FULL_KEY)
        };
    }

    @Override
    public String[] weekdaysFullStandalone() {
        return new String[] {
                LocalizeUtil.get(SUNDAY_FULL_STANDALONE_KEY),
                LocalizeUtil.get(MONDAY_FULL_STANDALONE_KEY),
                LocalizeUtil.get(TUESDAY_FULL_STANDALONE_KEY),
                LocalizeUtil.get(WEDNESDAY_FULL_STANDALONE_KEY),
                LocalizeUtil.get(THURSDAY_FULL_STANDALONE_KEY),
                LocalizeUtil.get(FRIDAY_FULL_STANDALONE_KEY),
                LocalizeUtil.get(SATURDAY_FULL_STANDALONE_KEY)
        };
    }

    @Override
    public String[] weekdaysNarrow() {
        return new String[] {
                LocalizeUtil.get(SUNDAY_NARROW_KEY),
                LocalizeUtil.get(MONDAY_NARROW_KEY),
                LocalizeUtil.get(TUESDAY_NARROW_KEY),
                LocalizeUtil.get(WEDNESDAY_NARROW_KEY),
                LocalizeUtil.get(THURSDAY_NARROW_KEY),
                LocalizeUtil.get(FRIDAY_NARROW_KEY),
                LocalizeUtil.get(SATURDAY_NARROW_KEY)
        };
    }

    @Override
    public String[] weekdaysShort() {
        return new String[] {
                LocalizeUtil.get(SUNDAY_SHORT_KEY),
                LocalizeUtil.get(MONDAY_SHORT_KEY),
                LocalizeUtil.get(TUESDAY_SHORT_KEY),
                LocalizeUtil.get(WEDNESDAY_SHORT_KEY),
                LocalizeUtil.get(THURSDAY_SHORT_KEY),
                LocalizeUtil.get(FRIDAY_SHORT_KEY),
                LocalizeUtil.get(SATURDAY_SHORT_KEY)
        };
    }

    @Override
    public String[] weekdaysShortStandalone() {
        return new String[] {
                LocalizeUtil.get(SUNDAY_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(MONDAY_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(TUESDAY_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(WEDNESDAY_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(THURSDAY_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(FRIDAY_SHORT_STANDALONE_KEY),
                LocalizeUtil.get(SATURDAY_SHORT_STANDALONE_KEY)
        };
    }

    @Override
    public String[] weekdaysNarrowStandalone() {
        return new String[]{
                LocalizeUtil.get(SUNDAY_FIRST_CAPITAL_KEY),
                LocalizeUtil.get(MONDAY_FIRST_CAPITAL_KEY),
                LocalizeUtil.get(TUESDAY_FIRST_CAPITAL_KEY),
                LocalizeUtil.get(WEDNESDAY_FIRST_CAPITAL_KEY),
                LocalizeUtil.get(THURSDAY_FIRST_CAPITAL_KEY),
                LocalizeUtil.get(FRIDAY_FIRST_CAPITAL_KEY),
                LocalizeUtil.get(SATURDAY_FIRST_CAPITAL_KEY)
        };
    }
    @Override
    public String[] quartersFull() {
        return new String[] {
                LocalizeUtil.get(QUARTER_FIRST_FULL_KEY),
                LocalizeUtil.get(QUARTER_SECOND_FULL_KEY),
                LocalizeUtil.get(QUARTER_THIRD_FULL_KEY),
                LocalizeUtil.get(QUARTER_FORTH_FULL_KEY)
        };
    }

    @Override
    public String[] quartersShort() {
        return new String[] {
                LocalizeUtil.get(QUARTER_FIRST_SHORT_KEY),
                LocalizeUtil.get(QUARTER_SECOND_SHORT_KEY),
                LocalizeUtil.get(QUARTER_THIRD_SHORT_KEY),
                LocalizeUtil.get(QUARTER_FORTH_SHORT_KEY)
        };
    }


}
