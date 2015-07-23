package ru.intertrust.cm.core;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 18.02.14 15:39.
 */
public class UserInfo implements Dto {

    private String timeZoneId;
    private String locale;

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "timeZoneId='" + timeZoneId + '\'' +
                ", locale='" + locale + '\'' +
                '}';
    }
}
