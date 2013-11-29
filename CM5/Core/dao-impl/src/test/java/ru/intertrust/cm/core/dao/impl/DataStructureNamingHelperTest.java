package ru.intertrust.cm.core.dao.impl;

import org.junit.Test;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;

import static org.junit.Assert.assertEquals;

/**
 * @author vmatsukevich
 *         Date: 5/29/13
 *         Time: 11:30 AM
 */
public class DataStructureNamingHelperTest {
    @Test
    public void testGetSqlNameForDomainObjectConfig() throws Exception {
        DomainObjectTypeConfig domainObjectTypeConfig = new DomainObjectTypeConfig();
        domainObjectTypeConfig.setName("Outgoing_Document");

        String sqlName = DataStructureNamingHelper.getSqlName(domainObjectTypeConfig);
        assertEquals("outgoing_document", sqlName);
    }

    @Test
    public void testGetSqlNameForFieldConfig() throws Exception {
        FieldConfig fieldConfig = new StringFieldConfig();
        fieldConfig.setName("Registration_Number");

        String sqlName = DataStructureNamingHelper.getSqlName(fieldConfig);
        assertEquals("registration_number", sqlName);
    }

    @Test
    public void testGetSqlNameForString() throws Exception {
        String sqlName = DataStructureNamingHelper.getSqlName("Registration_Date");
        assertEquals("registration_date", sqlName);
    }
}
