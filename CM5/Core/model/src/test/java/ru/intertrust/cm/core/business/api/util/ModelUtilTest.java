package ru.intertrust.cm.core.business.api.util;

import org.junit.Assert;
import org.junit.Test;
import ru.intertrust.cm.core.business.api.dto.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Sergey.Okolot
 *         Created on 06.03.14 13:10.
 */
public class ModelUtilTest {

    public static final String TYPE_1 = "Type1";
    public static final String TYPE_2 = "Type2";
    public static final String TYPE_3 = "Type3";

    @Test
    public void testGetUTCTimeZoneId() throws Exception {
        Assert.assertEquals("GMT-4:30", ModelUtil.getUTCTimeZoneId(-16200000));
        Assert.assertEquals("GMT-4", ModelUtil.getUTCTimeZoneId(-14400000));
        Assert.assertEquals("GMT", ModelUtil.getUTCTimeZoneId(0));
        Assert.assertEquals("GMT+3:07", ModelUtil.getUTCTimeZoneId(11224000));
        Assert.assertEquals("GMT+3:30", ModelUtil.getUTCTimeZoneId(12600000));
        Assert.assertEquals("GMT+4", ModelUtil.getUTCTimeZoneId(14400000));
    }

    @Test
    public void testSort() {
        /*
        This table should be a result of the sort (nulls are the last)
        Sort order: type1 asc, type2 asc, type3 desc
        ----------------------------------------------
        |   Type1    |      Type2       |    Type3   |
        ----------------------------------------------
        |     A      |        U         |     Y      |
        |     A      |        U         |     X      |
        |     A      |        U         |     W      |
        |     A      |        U         |            |
        |     B      |        C         |     D      |
        |     B      |        D         |     A      |
        ----------------------------------------------

         */
        //Put rows in mixed order:
        List<IdentifiableObject> collection = new ArrayList<>();
        collection.add(createIdentifiableObject("A", "U", "X"));
        collection.add(createIdentifiableObject("B", "D", "A"));
        collection.add(createIdentifiableObject("B", "C", "D"));
        collection.add(createIdentifiableObject("A", "U", "Y"));
        collection.add(createIdentifiableObject("A", "U", "W"));
        collection.add(createIdentifiableObject("A", "U", null));

        SortOrder order = new SortOrder();
        order.add(new SortCriterion(TYPE_1, SortCriterion.Order.ASCENDING));
        order.add(new SortCriterion(TYPE_2, SortCriterion.Order.ASCENDING));
        order.add(new SortCriterion(TYPE_3, SortCriterion.Order.DESCENDING));
        ModelUtil.sort(collection, order);

        assertEquals(collection.get(0).getValue(TYPE_1), new StringValue("A"));
        assertEquals(collection.get(0).getValue(TYPE_2), new StringValue("U"));
        assertEquals(collection.get(0).getValue(TYPE_3), new StringValue("Y"));

        assertEquals(collection.get(1).getValue(TYPE_1), new StringValue("A"));
        assertEquals(collection.get(1).getValue(TYPE_2), new StringValue("U"));
        assertEquals(collection.get(1).getValue(TYPE_3), new StringValue("X"));

        assertEquals(collection.get(2).getValue(TYPE_1), new StringValue("A"));
        assertEquals(collection.get(2).getValue(TYPE_2), new StringValue("U"));
        assertEquals(collection.get(2).getValue(TYPE_3), new StringValue("W"));

        assertEquals(collection.get(3).getValue(TYPE_1), new StringValue("A"));
        assertEquals(collection.get(3).getValue(TYPE_2), new StringValue("U"));
        assertEquals(collection.get(3).getValue(TYPE_3), null);

        assertEquals(collection.get(4).getValue(TYPE_1), new StringValue("B"));
        assertEquals(collection.get(4).getValue(TYPE_2), new StringValue("C"));
        assertEquals(collection.get(4).getValue(TYPE_3), new StringValue("D"));

        assertEquals(collection.get(5).getValue(TYPE_1), new StringValue("B"));
        assertEquals(collection.get(5).getValue(TYPE_2), new StringValue("D"));
        assertEquals(collection.get(5).getValue(TYPE_3), new StringValue("A"));
    }

    private static IdentifiableObject createIdentifiableObject(String... values) {
        IdentifiableObject obj = new GenericDomainObject();
        final double rnd = Math.random();
        // setting fields in random order, to check that it doesn't affect behavior
        if (rnd < 0.333) {
            obj.setString(TYPE_1, values[0]);
            obj.setString(TYPE_2, values[1]);
            obj.setString(TYPE_3, values[2]);
        } else if (rnd < 0.667) {
            obj.setString(TYPE_2, values[1]);
            obj.setString(TYPE_1, values[0]);
            obj.setString(TYPE_3, values[2]);
        } else {
            obj.setString(TYPE_3, values[2]);
            obj.setString(TYPE_2, values[1]);
            obj.setString(TYPE_1, values[0]);
        }
        return obj;
    }
}
