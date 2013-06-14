package ru.intertrust.cm.core.dao.impl;

import org.junit.Test;
import ru.intertrust.cm.core.config.model.BusinessObjectConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.StringFieldConfig;

import static org.junit.Assert.assertEquals;

/**
 * @author vmatsukevich
 *         Date: 5/29/13
 *         Time: 11:30 AM
 */
public class DataStructureNamingHelperTest {
    @Test
    public void testGetSqlNameForBusinessObjectConfig() throws Exception {
        BusinessObjectConfig businessObjectConfig = new BusinessObjectConfig();
        businessObjectConfig.setName("Outgoing Document");

        String sqlName = DataStructureNamingHelper.getSqlName(businessObjectConfig);
        assertEquals("OUTGOING_DOCUMENT", sqlName);
    }

    @Test
    public void testGetSqlNameForFieldConfig() throws Exception {
        FieldConfig fieldConfig = new StringFieldConfig();
        fieldConfig.setName("Registration Number");

        String sqlName = DataStructureNamingHelper.getSqlName(fieldConfig);
        assertEquals("REGISTRATION_NUMBER", sqlName);
    }

    @Test
    public void testGetReferencedTypeSqlName() throws Exception {
        ReferenceFieldConfig referenceFieldConfig = new ReferenceFieldConfig();
        referenceFieldConfig.setName("Author");
        referenceFieldConfig.setType("Employee");

        String sqlName = DataStructureNamingHelper.getReferencedTypeSqlName(referenceFieldConfig);
        assertEquals("EMPLOYEE", sqlName);
    }

    @Test
    public void testGetSqlNameForString() throws Exception {
        String sqlName = DataStructureNamingHelper.getSqlName("Registration Date");
        assertEquals("REGISTRATION_DATE", sqlName);
    }
}
