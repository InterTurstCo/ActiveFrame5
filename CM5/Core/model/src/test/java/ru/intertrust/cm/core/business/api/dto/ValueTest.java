package ru.intertrust.cm.core.business.api.dto;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Denis Mitavskiy
 *         Date: 24.04.14
 *         Time: 15:29
 */
public class ValueTest {
    @Test
    public void testValueComparison() {
        final StringValue[] notNullStringValueArray = {
                new StringValue("A"), new StringValue("C"), new StringValue(null), new StringValue("D")
        };
        final StringValue[] ascNullsFirst = {
                new StringValue(null), new StringValue("A"), new StringValue("C"), new StringValue("D")
        };
        final StringValue[] ascNullsLast = {
                new StringValue("A"), new StringValue("C"), new StringValue("D"), new StringValue(null)
        };
        final StringValue[] descNullsFirst = {
                new StringValue("D"), new StringValue("C"), new StringValue("A"), new StringValue(null)
        };
        final StringValue[] descNullsLast = {
                new StringValue(null), new StringValue("D"), new StringValue("C"), new StringValue("A")
        };
        Arrays.sort(notNullStringValueArray);
        Assert.assertArrayEquals("", notNullStringValueArray, ascNullsFirst);

        Arrays.sort(notNullStringValueArray, Value.<StringValue>getComparator(true, true));
        Assert.assertArrayEquals("", notNullStringValueArray, ascNullsFirst);

        Arrays.sort(notNullStringValueArray, Value.<StringValue>getComparator(true, false));
        Assert.assertArrayEquals("", notNullStringValueArray, ascNullsLast);

        Arrays.sort(notNullStringValueArray, Value.<StringValue>getComparator(false, true));
        Assert.assertArrayEquals("", notNullStringValueArray, descNullsFirst);

        Arrays.sort(notNullStringValueArray, Value.<StringValue>getComparator(false, false));
        Assert.assertArrayEquals("", notNullStringValueArray, descNullsLast);

        final StringValue[] stringValueArray = {
                new StringValue("A"), null, new StringValue("C"), null, new StringValue("D")
        };
        final StringValue[] stringValuesSortedDescNullsLast = {
                null, null, new StringValue("D"), new StringValue("C"), new StringValue("A")
        };
        Arrays.sort(stringValueArray, Value.<StringValue>getComparator(false, false));
        Assert.assertArrayEquals("", stringValueArray, stringValuesSortedDescNullsLast);

        final DateTimeWithTimeZoneValue[] dateTimeWithTimeZoneValues = {
                new DateTimeWithTimeZoneValue(new DateTimeWithTimeZone("Europe/Kiev", 2014, 3, 24, 12, 0, 0, 0)),
                new DateTimeWithTimeZoneValue(new DateTimeWithTimeZone("Europe/Kiev", 2014, 3, 24, 11, 59, 59, 999)),
                new DateTimeWithTimeZoneValue(null),
                new DateTimeWithTimeZoneValue(new DateTimeWithTimeZone("Europe/Moscow", 2014, 3, 24, 12, 0, 0, 0)),
        };
        final DateTimeWithTimeZoneValue[] dateTimeWithTimeZoneValuesSortedDescNullsLast = {
                new DateTimeWithTimeZoneValue(null),
                new DateTimeWithTimeZoneValue(new DateTimeWithTimeZone("Europe/Kiev", 2014, 3, 24, 12, 0, 0, 0)),
                new DateTimeWithTimeZoneValue(new DateTimeWithTimeZone("Europe/Kiev", 2014, 3, 24, 11, 59, 59, 999)),
                new DateTimeWithTimeZoneValue(new DateTimeWithTimeZone("Europe/Moscow", 2014, 3, 24, 12, 0, 0, 0)),
        };
        Arrays.sort(dateTimeWithTimeZoneValues, Value.<DateTimeWithTimeZoneValue>getComparator(false, false));
        Assert.assertArrayEquals("", dateTimeWithTimeZoneValues, dateTimeWithTimeZoneValuesSortedDescNullsLast);
    }
}
