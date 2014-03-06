package ru.intertrust.cm.core.business.api.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sergey.Okolot
 *         Created on 06.03.14 13:10.
 */
public class ModelUtilTest {

    @Test
    public void testGetUTCTimeZoneId() throws Exception {
        Assert.assertEquals("GMT-4:30", ModelUtil.getUTCTimeZoneId(-16200000));
        Assert.assertEquals("GMT-4", ModelUtil.getUTCTimeZoneId(-14400000));
        Assert.assertEquals("GMT", ModelUtil.getUTCTimeZoneId(0));
        Assert.assertEquals("GMT+3:07", ModelUtil.getUTCTimeZoneId(11224000));
        Assert.assertEquals("GMT+3:30", ModelUtil.getUTCTimeZoneId(12600000));
        Assert.assertEquals("GMT+4", ModelUtil.getUTCTimeZoneId(14400000));
    }
}
