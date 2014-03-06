package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ModelUtil;

/**
* @author vmatsukevich
*         Date: 10/29/13
*         Time: 12:38 PM
*/
public class UTCOffsetTimeZoneContext extends TimeZoneContext {
    private int offset;

    public UTCOffsetTimeZoneContext(int offset) {
        this.offset = offset;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public String getTimeZoneId() {
        final String timeZoneId = ModelUtil.getUTCTimeZoneId(offset);
        return timeZoneId;
    }
}
