package ru.intertrust.cm.core;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 18.02.14 15:39.
 */
public class UserInfo implements Dto {

    private String timeZoneId;

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    @Override
    public String toString() {
        return new StringBuilder(UserInfo.class.getSimpleName())
                .append(": timeZoneId=").append(timeZoneId)
                .toString();
    }
}
