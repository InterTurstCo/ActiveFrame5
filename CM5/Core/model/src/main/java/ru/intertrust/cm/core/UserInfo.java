package ru.intertrust.cm.core;

import java.io.Serializable;

/**
 * @author Sergey.Okolot
 *         Created on 18.02.14 15:39.
 */
public class UserInfo implements Serializable{

    private String timeZone;

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return new StringBuilder(UserInfo.class.getSimpleName())
                .append(": timeZone=").append(timeZone)
                .toString();
    }
}
