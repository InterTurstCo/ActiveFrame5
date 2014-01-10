package ru.intertrust.cm.core.business.api.dto;

/**
* @author vmatsukevich
*         Date: 10/29/13
*         Time: 12:38 PM
*/
public class UTCOffsetTimeZoneContext extends TimeZoneContext {
    private long offset;

    public UTCOffsetTimeZoneContext(long offset) {
        this.offset = offset;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public String getTimeZoneId() {
        String timeZoneId;

        if (offset == 0) {
            timeZoneId = "GMT";
        } else {
            timeZoneId = String.format("%s%02d:%02d", offset > 0 ? "GMT+" : "GMT",
                    offset / 3600000, (offset / 60000) % 60);
        }

        return timeZoneId;
    }

}
