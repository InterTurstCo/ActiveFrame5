package ru.intertrust.cm.core;

import java.io.Serializable;

/**
 * @author Sergey.Okolot
 *         Created on 18.02.14 15:39.
 */
public class UserInfo implements Serializable{

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
