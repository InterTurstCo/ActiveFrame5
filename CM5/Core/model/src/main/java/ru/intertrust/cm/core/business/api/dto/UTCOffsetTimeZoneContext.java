package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ModelUtil;

/**
* @author vmatsukevich
*         Date: 10/29/13
*         Time: 12:38 PM
*/
public class UTCOffsetTimeZoneContext extends TimeZoneContext {
    private int offset;

    public UTCOffsetTimeZoneContext() {
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UTCOffsetTimeZoneContext that = (UTCOffsetTimeZoneContext) o;

        if (offset != that.offset) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return offset;
    }
}
