package ru.intertrust.cm.core.business.api.dto;

/**
* @author vmatsukevich
*         Date: 10/29/13
*         Time: 12:38 PM
*/
public class OlsonTimeZoneContext extends TimeZoneContext {
    private String timeZoneId;

    public OlsonTimeZoneContext() {
    }

    public OlsonTimeZoneContext(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    @Override
    public String getTimeZoneId() {
        return timeZoneId;
    }
}
