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

}
