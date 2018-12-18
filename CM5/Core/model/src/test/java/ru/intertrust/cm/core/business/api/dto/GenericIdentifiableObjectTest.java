package ru.intertrust.cm.core.business.api.dto;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

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
        Assert.assertTrue(object.getValue("field") instanceof DateTimeValue);
        Assert.assertEquals(new DateTimeValue(calendar.getTime()), object.getValue("field"));
    }

    @Test
    public void testSetAndGetDateTimeWithTimeZone() throws Exception {
        DateTimeWithTimeZone dateTimeWithTimeZone = new DateTimeWithTimeZone(7200000, 2012, 3, 15, 14, 31, 66, 195);

        object.setDateTimeWithTimeZone("field", dateTimeWithTimeZone);
        Assert.assertEquals(dateTimeWithTimeZone, object.getDateTimeWithTimeZone("field"));
        Assert.assertTrue(object.getValue("field") instanceof DateTimeWithTimeZoneValue);
        Assert.assertEquals(new DateTimeWithTimeZoneValue(dateTimeWithTimeZone), object.getValue("field"));
    }

    @Test
    public void testSetReferenceByDomainObject() throws Exception {
        GenericDomainObject referenceObject = createGenericDomainObject();
        object.setReference("field", referenceObject);
        Assert.assertEquals(referenceObject.getId(), object.getReference("field"));
        Assert.assertTrue(object.getValue("field") instanceof ReferenceValue);
        Assert.assertEquals(new ReferenceValue(referenceObject.getId()), object.getValue("field"));
    }

    @Test
    public void testSetReferenceById() throws Exception {
        Id referenceId = createId();
        object.setReference("field", referenceId);
        Assert.assertEquals(referenceId, object.getReference("field"));
        Assert.assertTrue(object.getValue("field") instanceof ReferenceValue);
        Assert.assertEquals(new ReferenceValue(referenceId), object.getValue("field"));
    }

    private RdbmsId createId() {
        return new RdbmsId("0012000000012345");
    }

    private GenericDomainObject createGenericDomainObject() {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setId(createId());
        return domainObject;
    }

    @Test
    public void testToString() {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setId(createId());
        domainObject.setString("str_field", "test1");
        domainObject.setTimestamp("date_field", new Date(0));
        String res = domainObject.toString();
        assertTrue(res.contains("test1"));
        assertTrue(res.contains("70"));
    }
}

