package ru.intertrust.cm.core.business.api.dto;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class GenericDomainObjectTest {

    GenericDomainObject object;

    @Before
    public void setUp() {
        object = new GenericDomainObject();
    }

    @Test
    public void testSetReferenceByDomainObject() throws Exception {
        GenericDomainObject referenceObject = createDomainObject();
        object.setReference("field", referenceObject);
        Assert.assertEquals(referenceObject.getId(), object.getId("field"));
        Assert.assertTrue(object.getValue("field") instanceof ReferenceValue);
        Assert.assertEquals(new ReferenceValue(referenceObject.getId()), object.getValue("field"));
    }

    @Test
    public void testSetReferenceById() throws Exception {
        Id referenceId = createId();
        object.setReference("field", referenceId);
        Assert.assertEquals(referenceId, object.getId("field"));
        Assert.assertTrue(object.getValue("field") instanceof ReferenceValue);
        Assert.assertEquals(new ReferenceValue(referenceId), object.getValue("field"));
    }

    private RdbmsId createId() {
        return new RdbmsId("object|12345");
    }

    private GenericDomainObject createDomainObject() {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setId(createId());
        return domainObject;
    }
}
