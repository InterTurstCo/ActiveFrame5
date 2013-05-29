package ru.intertrust.cm.core.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.config.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static ru.intertrust.cm.core.dao.impl.PostgreSQLQueryHelper.generateCountTablesQuery;
import static ru.intertrust.cm.core.dao.impl.PostgreSQLQueryHelper.generateCreateAuthenticationInfoTableQuery;
import static ru.intertrust.cm.core.dao.impl.PostgreSQLQueryHelper.generateCreateBusinessObjectTableQuery;

/**
 * @author vmatsukevich
 *         Date: 5/29/13
 *         Time: 5:32 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class PostgreSQLDataStructureDAOImplTest {
    @InjectMocks
    private PostgreSQLDataStructureDAOImpl dataStructureDAO = new PostgreSQLDataStructureDAOImpl();
    @Mock
    private JdbcTemplate jdbcTemplate;

    private BusinessObjectConfig businessObjectConfig;

    @Before
    public void setUp() throws Exception {
        businessObjectConfig = new BusinessObjectConfig();
        businessObjectConfig.setName("Outgoing Document");
        businessObjectConfig.setParentConfig("Document");

        StringFieldConfig registrationNumber = new StringFieldConfig();
        registrationNumber.setName("Registration Number");
        registrationNumber.setLength(128);
        businessObjectConfig.getFieldConfigs().add(registrationNumber);

        DateTimeFieldConfig registrationDate = new DateTimeFieldConfig();
        registrationDate.setName("Registration Date");
        businessObjectConfig.getFieldConfigs().add(registrationDate);

        ReferenceFieldConfig referenceFieldConfig = new ReferenceFieldConfig();
        referenceFieldConfig.setName("Author");
        referenceFieldConfig.setType("Employee");
        businessObjectConfig.getFieldConfigs().add(referenceFieldConfig);

        LongFieldConfig longFieldConfig = new LongFieldConfig();
        longFieldConfig.setName("Long Field");
        businessObjectConfig.getFieldConfigs().add(longFieldConfig);

        DecimalFieldConfig decimalFieldConfig1 = new DecimalFieldConfig();
        decimalFieldConfig1.setName("Decimal Field 1");
        decimalFieldConfig1.setNotNull(false);
        decimalFieldConfig1.setPrecision(10);
        decimalFieldConfig1.setScale(2);
        businessObjectConfig.getFieldConfigs().add(decimalFieldConfig1);

        DecimalFieldConfig decimalFieldConfig2 = new DecimalFieldConfig();
        decimalFieldConfig2.setName("Decimal Field 2");
        decimalFieldConfig2.setNotNull(false);
        decimalFieldConfig2.setPrecision(10);
        businessObjectConfig.getFieldConfigs().add(decimalFieldConfig2);

        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        businessObjectConfig.getUniqueKeyConfigs().add(uniqueKeyConfig);

        UniqueKeyFieldConfig uniqueKeyFieldConfig1 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig1.setName("Registration Number");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig1);

        UniqueKeyFieldConfig uniqueKeyFieldConfig2 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig2.setName("Registration Date");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig2);
    }


    @Test
    public void testCreateTable() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyString())).thenReturn(Long.valueOf(7));
        dataStructureDAO.createTable(businessObjectConfig);

        verify(jdbcTemplate, times(2)).update(anyString());
        verify(jdbcTemplate).update(anyString(), anyString());
        verify(jdbcTemplate).queryForObject(anyString(), any(Class.class), anyString());
        assertEquals(Long.valueOf(7), businessObjectConfig.getId());
    }

    @Test
    public void testCountTables() throws Exception {
        dataStructureDAO.countTables();
        verify(jdbcTemplate).queryForObject(generateCountTablesQuery(), Integer.class);
    }

    @Test
    public void testCreateServiceTables() throws Exception {
        dataStructureDAO.createServiceTables();
        verify(jdbcTemplate).update(generateCreateBusinessObjectTableQuery());
        verify(jdbcTemplate).update(generateCreateAuthenticationInfoTableQuery());
    }

    @Test
    public void testDoesTableExists() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyString())).thenReturn(0);
        boolean tableExists = dataStructureDAO.doesTableExists("DOCUMENT");
        assertFalse(tableExists);

        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyString())).thenReturn(1);
        tableExists = dataStructureDAO.doesTableExists("DOCUMENT");
        assertTrue(tableExists);

        verify(jdbcTemplate, times(2)).queryForObject(anyString(), any(Class.class), anyString());
    }
}
