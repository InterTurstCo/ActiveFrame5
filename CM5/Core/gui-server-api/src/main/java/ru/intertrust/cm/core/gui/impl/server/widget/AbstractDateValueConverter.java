package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.gui.api.server.GuiContext;

import java.util.TimeZone;

/**
 * @author Sergey.Okolot
 *         Created on 28.10.2014 14:54.
 */
public abstract class AbstractDateValueConverter<T> implements DateValueConverter<T> {

    protected static String getTimeZoneId(final String timeZoneId) {
        switch (timeZoneId) {
            case ModelUtil.DEFAULT_TIME_ZONE_ID:
            case ModelUtil.LOCAL_TIME_ZONE_ID:
            case ModelUtil.ORIGINAL_TIME_ZONE_ID:
            case "По умолчанию":
            case "Локальная":
            case "Оригинальная":
                return GuiContext.get().getUserInfo().getTimeZoneId();
            default:
                // if time zone id doesn't exist it'll return GMT
                return TimeZone.getTimeZone(timeZoneId).getID();
        }
    }
}
