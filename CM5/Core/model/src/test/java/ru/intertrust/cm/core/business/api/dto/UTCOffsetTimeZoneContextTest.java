package ru.intertrust.cm.core.business.api.dto;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UTCOffsetTimeZoneContextTest {

    @Test
    public void testGetTimeZoneId() {
        UTCOffsetTimeZoneContext utcOffsetContext = new UTCOffsetTimeZoneContext((long) (3.5*60*60*1000));
        String timeZoneId = utcOffsetContext.getTimeZoneId();
        assertEquals("GMT+03:30", timeZoneId);
    }
}
