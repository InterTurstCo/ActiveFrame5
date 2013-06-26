package ru.intertrust.cm.core.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.config.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.intertrust.cm.core.dao.impl.PostgreSQLDataStructureDAOImpl.DOES_TABLE_EXISTS_QUERY;
import static ru.intertrust.cm.core.dao.impl.PostgreSQLDataStructureDAOImpl.INSERT_INTO_DOMAIN_OBJECT_TABLE_QUERY;
import static ru.intertrust.cm.core.dao.impl.PostgreSQLDataStructureDAOImpl.SELECT_DOMAIN_OBJECT_CONFIG_ID_BY_NAME_QUERY;
import static ru.intertrust.cm.core.dao.impl.PostgreSQLQueryHelper.*;

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
        initDomainObjectConfig();
    }


    @Test
    public void testCreateTable() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyString())).thenReturn(Long.valueOf(7)); // ID конфигурации доменного объекта
        dataStructureDAO.createTable(domainObjectConfig);

        verify(jdbcTemplate).update(generateCreateTableQuery(domainObjectConfig));
        verify(jdbcTemplate).update(generateCreateIndexesQuery(domainObjectConfig.getName(),
                domainObjectConfig.getFieldConfigs()));
        verify(jdbcTemplate).update(INSERT_INTO_DOMAIN_OBJECT_TABLE_QUERY,
                domainObjectConfig.getName());

        verify(jdbcTemplate).queryForObject(SELECT_DOMAIN_OBJECT_CONFIG_ID_BY_NAME_QUERY,
                Long.class, domainObjectConfig.getName());

        assertEquals(Long.valueOf(7), domainObjectConfig.getId());
    }

    @Test
    public void testUpdateTableStructure() {
        List<FieldConfig> newColumns = new ArrayList<>();

        StringFieldConfig descriptionFieldConfig = new StringFieldConfig();
        descriptionFieldConfig.setName("Description");
        descriptionFieldConfig.setLength(256);
        descriptionFieldConfig.setNotNull(false);
        newColumns.add(descriptionFieldConfig);

        ReferenceFieldConfig executorFieldConfig = new ReferenceFieldConfig();
        executorFieldConfig.setName("Executor");
        executorFieldConfig.setType("Employee");
        executorFieldConfig.setNotNull(true);
        newColumns.add(executorFieldConfig);

        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        UniqueKeyFieldConfig uniqueKeyFieldConfig = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig.setName("Registration Number");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig);
        List<UniqueKeyConfig> newUniqueConfigs = Collections.singletonList(uniqueKeyConfig);

        dataStructureDAO.updateTableStructure(domainObjectConfig.getName(), newColumns, newUniqueConfigs);

        verify(jdbcTemplate).update(generateUpdateTableQuery(domainObjectConfig.getName(),
                newColumns, newUniqueConfigs));
        verify(jdbcTemplate).update(generateCreateIndexesQuery(domainObjectConfig.getName(), newColumns));
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
        verify(jdbcTemplate).update(generateCreateConfigurationTableQuery());
    }

    @Test
    public void testDoesTableExistsWhenFalse() throws Exception {
        String tableName = "DOCUMENT";

        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyString())).thenReturn(0);
        boolean tableExists = dataStructureDAO.doesTableExists(tableName);

        assertFalse(tableExists);
        verify(jdbcTemplate).queryForObject(DOES_TABLE_EXISTS_QUERY, Integer.class, tableName);
    }

    @Test
    public void testDoesTableExistsWhenTrue() throws Exception {
        String tableName = "DOCUMENT";

        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyString())).thenReturn(1);
        boolean tableExists = dataStructureDAO.doesTableExists(tableName);

        assertTrue(tableExists);
        verify(jdbcTemplate).queryForObject(DOES_TABLE_EXISTS_QUERY, Integer.class, tableName);
    }

    private void initDomainObjectConfig() {
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
