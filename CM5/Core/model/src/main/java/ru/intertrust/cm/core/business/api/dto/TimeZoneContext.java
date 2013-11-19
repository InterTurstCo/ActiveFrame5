package ru.intertrust.cm.core.business.api.dto;

/**
* @author vmatsukevich
*         Date: 10/29/13
*         Time: 12:38 PM
*/
public class TimeZoneContext extends DateContext {
    private String timeZoneId;

    public TimeZoneContext(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }
}
