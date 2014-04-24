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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OlsonTimeZoneContext that = (OlsonTimeZoneContext) o;

        if (timeZoneId != null ? !timeZoneId.equals(that.timeZoneId) : that.timeZoneId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return timeZoneId != null ? timeZoneId.hashCode() : 0;
    }
}
