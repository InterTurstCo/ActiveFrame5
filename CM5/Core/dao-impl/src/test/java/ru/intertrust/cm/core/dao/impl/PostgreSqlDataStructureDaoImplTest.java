package ru.intertrust.cm.core.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.intertrust.cm.core.dao.impl.PostgreSqlDataStructureDaoImpl.DOES_TABLE_EXISTS_QUERY;
import static ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper.*;

/**
 * @author vmatsukevich
 *         Date: 5/29/13
 *         Time: 5:32 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class PostgreSqlDataStructureDaoImplTest {
    @InjectMocks
    private final PostgreSqlDataStructureDaoImpl dataStructureDao = new PostgreSqlDataStructureDaoImpl();

    @Mock
    private DomainObjectTypeIdDao domainObjectTypeIdDao;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private DomainObjectTypeConfig domainObjectTypeConfig;

    @Before
    public void setUp() throws Exception {
        initDomainObjectConfig();
    }


    @Test
    public void testCreateTable() throws Exception {
        when(domainObjectTypeIdDao.insert(domainObjectTypeConfig)).thenReturn(Integer.valueOf(7)); // ID
        // конфигурации доменного объекта
        dataStructureDao.createTable(domainObjectTypeConfig);

        verify(jdbcTemplate).update(generateCreateTableQuery(domainObjectTypeConfig));
        verify(jdbcTemplate).update(generateCreateIndexesQuery(domainObjectTypeConfig.getName(),
                domainObjectTypeConfig.getFieldConfigs()));

        verify(domainObjectTypeIdDao).insert(domainObjectTypeConfig);

        assertEquals(Long.valueOf(7), domainObjectTypeConfig.getId());
    }

    @Test
    public void testCreateAclTables() {
        dataStructureDao.createAclTables(domainObjectTypeConfig);
        verify(jdbcTemplate).update(generateCreateAclTableQuery(domainObjectTypeConfig));
        verify(jdbcTemplate).update(generateCreateAclReadTableQuery(domainObjectTypeConfig));

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

        dataStructureDao.updateTableStructure(domainObjectTypeConfig.getName(), newColumns);

        verify(jdbcTemplate).update(generateAddColumnsQuery(domainObjectTypeConfig.getName(), newColumns));
        verify(jdbcTemplate).update(generateCreateIndexesQuery(domainObjectTypeConfig.getName(), newColumns));
    }

    @Test
    public void testCountTables() throws Exception {
        dataStructureDao.countTables();
        verify(jdbcTemplate).queryForObject(generateCountTablesQuery(), Integer.class);
    }

    @Test
    public void testCreateServiceTables() throws Exception {
        dataStructureDao.createServiceTables();
        verify(jdbcTemplate).update(generateCreateDomainObjectTableQuery());
        verify(jdbcTemplate).update(generateCreateConfigurationTableQuery());
    }

    @Test
    public void testDoesTableExistsWhenFalse() throws Exception {
        String tableName = "DOCUMENT";

        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyString())).thenReturn(0);
        boolean tableExists = dataStructureDao.doesTableExists(tableName);

        assertFalse(tableExists);
        verify(jdbcTemplate).queryForObject(DOES_TABLE_EXISTS_QUERY, Integer.class, tableName);
    }

    @Test
    public void testDoesTableExistsWhenTrue() throws Exception {
        String tableName = "DOCUMENT";

        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyString())).thenReturn(1);
        boolean tableExists = dataStructureDao.doesTableExists(tableName);

        assertTrue(tableExists);
        verify(jdbcTemplate).queryForObject(DOES_TABLE_EXISTS_QUERY, Integer.class, tableName);
    }

    private void initDomainObjectConfig() {
        domainObjectTypeConfig = new DomainObjectTypeConfig();
        domainObjectTypeConfig.setName("Outgoing Document");
        domainObjectTypeConfig.setExtendsAttribute("Document");

        StringFieldConfig registrationNumber = new StringFieldConfig();
        registrationNumber.setName("Registration Number");
        registrationNumber.setLength(128);
        domainObjectTypeConfig.getFieldConfigs().add(registrationNumber);

        DateTimeFieldConfig registrationDate = new DateTimeFieldConfig();
        registrationDate.setName("Registration Date");
        domainObjectTypeConfig.getFieldConfigs().add(registrationDate);

        ReferenceFieldConfig referenceFieldConfig = new ReferenceFieldConfig();
        referenceFieldConfig.setName("Author");
        referenceFieldConfig.setType("Employee");
        domainObjectTypeConfig.getFieldConfigs().add(referenceFieldConfig);

        LongFieldConfig longFieldConfig = new LongFieldConfig();
        longFieldConfig.setName("Long_Field");
        domainObjectTypeConfig.getFieldConfigs().add(longFieldConfig);

        DecimalFieldConfig decimalFieldConfig1 = new DecimalFieldConfig();
        decimalFieldConfig1.setName("Decimal_Field_1");
        decimalFieldConfig1.setNotNull(false);
        decimalFieldConfig1.setPrecision(10);
        decimalFieldConfig1.setScale(2);
        domainObjectTypeConfig.getFieldConfigs().add(decimalFieldConfig1);

        DecimalFieldConfig decimalFieldConfig2 = new DecimalFieldConfig();
        decimalFieldConfig2.setName("Decimal_Field_2");
        decimalFieldConfig2.setNotNull(false);
        decimalFieldConfig2.setPrecision(10);
        domainObjectTypeConfig.getFieldConfigs().add(decimalFieldConfig2);

        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        domainObjectTypeConfig.getUniqueKeyConfigs().add(uniqueKeyConfig);

        UniqueKeyFieldConfig uniqueKeyFieldConfig1 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig1.setName("Registration Number");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig1);

        UniqueKeyFieldConfig uniqueKeyFieldConfig2 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig2.setName("Registration Date");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig2);
    }
}
