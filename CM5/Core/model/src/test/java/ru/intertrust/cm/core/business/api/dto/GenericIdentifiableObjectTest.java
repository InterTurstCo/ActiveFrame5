package ru.intertrust.cm.core.business.api.dto;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Calendar;

public class GenericIdentifiableObjectTest {

    GenericIdentifiableObject object;

    @Before
    public void setUp() {
        object = new GenericIdentifiableObject();
    }

    @Test
    public void testSetAndGetString() throws Exception {
        object.setString("field", "field-value");
        Assert.assertEquals("field-value", object.getString("field"));
        Assert.assertTrue(object.getValue("field") instanceof StringValue);
        Assert.assertEquals(new StringValue("field-value"), object.getValue("field"));
    }

    @Test
    public void testSetAndGetLong() throws Exception {
        object.setLong("field", 20L);
        Assert.assertEquals(new Long(20L), object.getLong("field"));
        Assert.assertEquals(new LongValue(20L), object.getValue("field"));
    }

    @Test
    public void testSetAndGetBoolean() throws Exception {
        object.setBoolean("field", true);
        Assert.assertEquals(new Boolean(true), object.getBoolean("field"));
        Assert.assertTrue(object.getValue("field") instanceof BooleanValue);
        Assert.assertEquals(new BooleanValue(true), object.getValue("field"));
    }

    @Test
    public void testSetAndGetDecimal() throws Exception {
        object.setDecimal("field", new BigDecimal(1000));
        Assert.assertEquals(new BigDecimal(1000), object.getDecimal("field"));
        Assert.assertTrue(object.getValue("field") instanceof DecimalValue);
        Assert.assertEquals(new DecimalValue(new BigDecimal(1000)), object.getValue("field"));
    }

    @Test
    public void testSetAndGetTimestamp() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, 02, 12);
        object.setTimestamp("field", calendar.getTime());
        Assert.assertEquals(calendar.getTime(), object.getTimestamp("field"));
        Assert.assertTrue(object.getValue("field") instanceof TimestampValue);
        Assert.assertEquals(new TimestampValue(calendar.getTime()), object.getValue("field"));
    }
}

