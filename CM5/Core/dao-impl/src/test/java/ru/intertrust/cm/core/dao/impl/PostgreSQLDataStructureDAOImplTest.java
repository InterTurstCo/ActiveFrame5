package ru.intertrust.cm.core.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.config.model.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static ru.intertrust.cm.core.dao.impl.PostgreSQLQueryHelper.generateCountTablesQuery;
import static ru.intertrust.cm.core.dao.impl.PostgreSQLQueryHelper.generateCreateDomainObjectTableQuery;

/**
 * @author vmatsukevich
 *         Date: 5/29/13
 *         Time: 5:32 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class PostgreSQLDataStructureDAOImplTest {
    @InjectMocks
    private final PostgreSQLDataStructureDAOImpl dataStructureDAO = new PostgreSQLDataStructureDAOImpl();
    @Mock
    private JdbcTemplate jdbcTemplate;

    private DomainObjectConfig domainObjectConfig;

    @Before
    public void setUp() throws Exception {
        initBusinessObjectConfig();
    }


    @Test
    public void testCreateTable() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyString())).thenReturn(Long.valueOf(7)); // ID конфигурации бизнес-объекта
        dataStructureDAO.createTable(domainObjectConfig);

        verify(jdbcTemplate, times(2)).update(anyString());
        verify(jdbcTemplate).update(anyString(), anyString());
        verify(jdbcTemplate).queryForObject(anyString(), any(Class.class), anyString());
        assertEquals(Long.valueOf(7), domainObjectConfig.getId());
    }

    @Test
    public void testCountTables() throws Exception {
        dataStructureDAO.countTables();
        verify(jdbcTemplate).queryForObject(generateCountTablesQuery(), Integer.class);
    }

    @Test
    public void testCreateServiceTables() throws Exception {
        dataStructureDAO.createServiceTables();
        verify(jdbcTemplate).update(generateCreateDomainObjectTableQuery());
    }

    @Test
    public void testDoesTableExistsWhenFalse() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyString())).thenReturn(0);
        boolean tableExists = dataStructureDAO.doesTableExists("DOCUMENT");

        assertFalse(tableExists);
        verify(jdbcTemplate).queryForObject(anyString(), any(Class.class), anyString());
    }

    @Test
    public void testDoesTableExistsWhenTrue() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyString())).thenReturn(1);
        boolean tableExists = dataStructureDAO.doesTableExists("DOCUMENT");

        assertTrue(tableExists);
        verify(jdbcTemplate).queryForObject(anyString(), any(Class.class), anyString());
    }

    private void initBusinessObjectConfig() {
        domainObjectConfig = new DomainObjectConfig();
        domainObjectConfig.setName("Outgoing Document");
        domainObjectConfig.setParentConfig("Document");

        StringFieldConfig registrationNumber = new StringFieldConfig();
        registrationNumber.setName("Registration Number");
        registrationNumber.setLength(128);
        domainObjectConfig.getFieldConfigs().add(registrationNumber);

        DateTimeFieldConfig registrationDate = new DateTimeFieldConfig();
        registrationDate.setName("Registration Date");
        domainObjectConfig.getFieldConfigs().add(registrationDate);

        ReferenceFieldConfig referenceFieldConfig = new ReferenceFieldConfig();
        referenceFieldConfig.setName("Author");
        referenceFieldConfig.setType("Employee");
        domainObjectConfig.getFieldConfigs().add(referenceFieldConfig);

        LongFieldConfig longFieldConfig = new LongFieldConfig();
        longFieldConfig.setName("Long Field");
        domainObjectConfig.getFieldConfigs().add(longFieldConfig);

        DecimalFieldConfig decimalFieldConfig1 = new DecimalFieldConfig();
        decimalFieldConfig1.setName("Decimal Field 1");
        decimalFieldConfig1.setNotNull(false);
        decimalFieldConfig1.setPrecision(10);
        decimalFieldConfig1.setScale(2);
        domainObjectConfig.getFieldConfigs().add(decimalFieldConfig1);

        DecimalFieldConfig decimalFieldConfig2 = new DecimalFieldConfig();
        decimalFieldConfig2.setName("Decimal Field 2");
        decimalFieldConfig2.setNotNull(false);
        decimalFieldConfig2.setPrecision(10);
        domainObjectConfig.getFieldConfigs().add(decimalFieldConfig2);

        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        domainObjectConfig.getUniqueKeyConfigs().add(uniqueKeyConfig);

        UniqueKeyFieldConfig uniqueKeyFieldConfig1 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig1.setName("Registration Number");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig1);

        UniqueKeyFieldConfig uniqueKeyFieldConfig2 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig2.setName("Registration Date");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig2);
    }
}
