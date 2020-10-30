package ru.intertrust.cm.core.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static ru.intertrust.cm.core.dao.api.ConfigurationDao.CONFIGURATION_TABLE;
import static ru.intertrust.cm.core.dao.api.DataStructureDao.AUTHENTICATION_INFO_TABLE;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.ID_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.TYPE_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao.DOMAIN_OBJECT_TYPE_ID_TABLE;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DateTimeFieldConfig;
import ru.intertrust.cm.core.config.DecimalFieldConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.LongFieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.config.UniqueKeyFieldConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.dao.api.MD5Service;

/**
 * @author vmatsukevich Date: 5/29/13 Time: 12:39 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class PostgreSqlQueryHelperTest {

    @Mock
    private DomainObjectTypeIdDao domainObjectTypeIdDao;

    @Mock
    private ConfigurationExplorer configurationExplorer;
    
    private DomainObjectTypeConfig domainObjectTypeConfig;
    private ReferenceFieldConfig referenceFieldConfig;
    private PostgreSqlQueryHelper queryHelper;
    private MD5Service md5Service = new MD5ServiceImpl();

    @Before
    public void setUp() throws Exception {
        queryHelper = new PostgreSqlQueryHelper(domainObjectTypeIdDao, md5Service);
        initDomainObjectConfig();
        when(configurationExplorer.isAuditLogType(anyString())).thenReturn(false);
    }

    @Test
    public void testGenerateCountTablesQuery() {
        String query = "select count(table_name) FROM information_schema.tables WHERE table_schema = 'public'";
        String testQuery = queryHelper.generateCountTablesQuery();
        assertEquals(testQuery, query);
    }

    @Test
    public void testGenerateCreateDomainObjectTableQuery() {
        String query = "create table \"" + DOMAIN_OBJECT_TYPE_ID_TABLE + "\" (" +
                "\"id\" bigint not null, " +
                "\"name\" varchar(256) not null, " +
                "constraint \"pk_" + DOMAIN_OBJECT_TYPE_ID_TABLE + "\" primary key (\"id\"), " +
                "constraint \"u_" + DOMAIN_OBJECT_TYPE_ID_TABLE + "\" unique (\"name\"))";
        String testQuery = queryHelper.generateCreateDomainObjectTypeIdTableQuery();
        assertEquals(query, testQuery);
    }

    @Test
    public void testGenerateCreateConfigurationTableQuery() {
        String query = "create table \"" + CONFIGURATION_TABLE + "\" (\"id\" bigint not null, " +
                "\"content\" text not null, \"loaded_date\" timestamp not null, " +
                "constraint \"pk_" + CONFIGURATION_TABLE + "\" primary key (\"id\"))";
        String testQuery = queryHelper.generateCreateConfigurationTableQuery();
        assertEquals(query, testQuery);
    }

    @Test
    public void testGenerateCreateAuthenticationInfoTableQuery() {
        String query = "CREATE TABLE \"" + AUTHENTICATION_INFO_TABLE + "\" (\"id\" bigint not null, " +
                "\"user_uid\" character varying(64) NOT NULL, \"password\" character varying(128), " +
                "constraint \"pk_" + AUTHENTICATION_INFO_TABLE + "\" primary key (\"id\"), " +
                "constraint \"u_" + AUTHENTICATION_INFO_TABLE + "_user_uid\" unique(\"user_uid\"))";
        String testQuery = queryHelper.generateCreateAuthenticationInfoTableQuery();
        assertEquals(query, testQuery);
    }

    @Test
    public void testGenerateSequenceQuery() {
        String query = "create sequence \"10_sq\"";
        String testQuery = queryHelper.generateSequenceQuery(domainObjectTypeConfig);
        assertEquals(query, testQuery);
    }

    @Test
    public void testGenerateCreateTableQuery() throws Exception {
        String query = queryHelper.generateCreateTableQuery(domainObjectTypeConfig, false);
        String checkQuery = "create table \"outgoing_document\" ( \"id\" bigint not null, \"" + TYPE_COLUMN +
                "\" integer, \"registration_number\" varchar(128), " +
                "\"registration_date\" timestamp, \"author\" bigint, \"author_type\" integer, " +
                "\"long_field\" bigint, \"decimal_field_1\" numeric(10, 2), \"decimal_field_2\" numeric(10), " +
                "constraint \"pk_10\" primary key (\"id\"), " +
                "constraint \"u_10_0\" unique (\"id\", \"" + TYPE_COLUMN + "\"), " +
                "constraint \"fk_10_0\"" + " foreign key (\"id\") references " +
                "\"document\" (\"id\"), constraint \"fk_10_1\" " +
                "foreign key (\"" + TYPE_COLUMN + "\") references \"domain_object_type_id\" (\"id\"))";
        assertEquals(checkQuery, query);
    }

    @Test
    public void testGenerateCreateTableQueryWithoutExtendsAttribute() throws Exception {
        String checkQuery = "create table \"outgoing_document\" ( \"id\" bigint not null, \"" + TYPE_COLUMN + "\" integer, " +
                "\"created_date\" timestamp not null, " + "\"updated_date\" timestamp not null, \"created_by\" bigint, " +
                "\"created_by_type\" integer, \"updated_by\" bigint, \"updated_by_type\" integer, \"status\" bigint, " +
                "\"status_type\" integer, \"security_stamp\" bigint, \"security_stamp_type\"  integer, \"access_object_id\" bigint, " +
                "\"registration_number\" varchar(128), \"registration_date\" timestamp, \"author\" bigint, " +
                "\"author_type\" integer, " +
                "\"long_field\" bigint, \"decimal_field_1\" numeric(10, 2), \"decimal_field_2\" numeric(10), " +
                "constraint \"pk_10\" primary key (\"id\"), " +
                "constraint \"u_10_0\" unique (\"id\", \"" + TYPE_COLUMN + "\"), " +
                "constraint \"fk_10_0\" foreign key (\"" + TYPE_COLUMN + "\") " +
                "references \"domain_object_type_id\" (\"id\"))";
        domainObjectTypeConfig.setExtendsAttribute(null);
        when(domainObjectTypeIdDao.findIdByName("outgoing_document")).thenReturn(10);
        String query = queryHelper.generateCreateTableQuery(domainObjectTypeConfig, true);
        assertEquals(checkQuery, query);
    }

    @Test
    public void testGenerateCreateAclTableQuery() throws Exception {
        when(domainObjectTypeIdDao.findIdByName("outgoing_document")).thenReturn(10);
        String query = queryHelper.generateCreateAclTableQuery(domainObjectTypeConfig);

        String checkQuery = "create table \"outgoing_document_acl\" (\"object_id\" bigint not null, " +
                "\"group_id\" bigint not null, \"operation\" varchar(256) not null, " +
                "constraint \"pk_10_acl\" primary key (\"object_id\", \"group_id\", \"operation\"), " +
                "CONSTRAINT \"fk_10_acl_0\" FOREIGN KEY (\"object_id\") " +
                "REFERENCES \"outgoing_document\" (\"id\"), " +
                "CONSTRAINT \"fk_10_acl_1\" FOREIGN KEY (\"group_id\") REFERENCES " +
                "\"user_group\" (\"id\"))";
        assertEquals(checkQuery, query);
    }

    @Test
    public void testGenerateCreateAclReadTableQuery() throws Exception {
        when(domainObjectTypeIdDao.findIdByName("outgoing_document")).thenReturn(10);
        String query = queryHelper.generateCreateAclReadTableQuery(domainObjectTypeConfig);

        String checkQuery = "create table \"outgoing_document_read\" (\"object_id\" bigint not null, " +
                "\"group_id\" bigint not null, constraint \"pk_10_read\" " +
                "primary key (\"object_id\", \"group_id\"), " +
                "CONSTRAINT \"fk_10_read_0\" FOREIGN KEY (\"object_id\") " +
                "REFERENCES \"outgoing_document\" (\"id\"), " +
                "CONSTRAINT \"fk_10_read_1\" FOREIGN KEY (\"group_id\") " +
                "REFERENCES \"user_group\" (\"id\"))";
        assertEquals(checkQuery, query);
    }

    @Test
    public void testGenerateAddColumnsQuery() {
        String expectedQuery = "alter table \"outgoing_document\" " +
                "add column \"description\" varchar(256), " +
                "add column \"executor\" bigint not null, add column \"executor_type\" integer not null";

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

        String testQuery = queryHelper.generateAddColumnsQuery(domainObjectTypeConfig.getName(), newColumns);

        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testGenerateCreateForeignKeyConstraintsQuery() {
        String expectedQuery = "alter table \"outgoing_document\" " +
                "add constraint \"fk_10_0\" " +
                "foreign key (\"executor\", \"executor_type\") references \"employee\" (\"" + ID_COLUMN + "\", \"" +
                TYPE_COLUMN + "\")";

        ReferenceFieldConfig executorFieldConfig = new ReferenceFieldConfig();
        executorFieldConfig.setName("Executor");
        executorFieldConfig.setType("Employee");
        executorFieldConfig.setNotNull(true);

        String testQuery = queryHelper.generateCreateForeignKeyConstraintQuery(domainObjectTypeConfig, executorFieldConfig, 0);
        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testGenerateCreateUniqueConstraintsQuery() {
        when(domainObjectTypeIdDao.findIdByName("outgoing_document")).thenReturn(10);
        String expectedQuery = "alter table \"outgoing_document\" " +
                "add constraint \"u_10_0\"" + " unique (\"registration_number\")";

        UniqueKeyFieldConfig uniqueKeyFieldConfig = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig.setName("Registration_Number");

        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig);

        String testQuery = queryHelper.generateCreateUniqueConstraintQuery
                (domainObjectTypeConfig, uniqueKeyConfig, 0);
        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testGenerateCreateIndexQuery() throws Exception {
        when(domainObjectTypeIdDao.findIdByName("outgoing_document")).thenReturn(10);
        String query = queryHelper.generateCreateAutoIndexQuery(domainObjectTypeConfig, referenceFieldConfig, 0);
        String checkQuery = "create index \"i_10_0\" on \"outgoing_document\" (\"author\")";
        assertEquals(checkQuery, query);
    }

    private void initDomainObjectConfig() {
        domainObjectTypeConfig = new DomainObjectTypeConfig();
        domainObjectTypeConfig.setId(10);
        domainObjectTypeConfig.setName("Outgoing_Document");
        domainObjectTypeConfig.setExtendsAttribute("Document");

        StringFieldConfig registrationNumber = new StringFieldConfig();
        registrationNumber.setName("Registration_Number");
        registrationNumber.setLength(128);
        domainObjectTypeConfig.getFieldConfigs().add(registrationNumber);

        DateTimeFieldConfig registrationDate = new DateTimeFieldConfig();
        registrationDate.setName("Registration_Date");
        domainObjectTypeConfig.getFieldConfigs().add(registrationDate);

        referenceFieldConfig = new ReferenceFieldConfig();
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
        uniqueKeyFieldConfig1.setName("Registration_Number");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig1);

        UniqueKeyFieldConfig uniqueKeyFieldConfig2 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig2.setName("Registration_Date");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig2);
    }
}
