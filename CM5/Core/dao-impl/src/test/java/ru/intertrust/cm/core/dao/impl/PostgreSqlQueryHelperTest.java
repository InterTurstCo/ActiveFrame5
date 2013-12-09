package ru.intertrust.cm.core.dao.impl;

import org.junit.Before;
import org.junit.Test;
import ru.intertrust.cm.core.config.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static ru.intertrust.cm.core.dao.api.ConfigurationDao.CONFIGURATION_TABLE;
import static ru.intertrust.cm.core.dao.api.DataStructureDao.AUTHENTICATION_INFO_TABLE;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.ID_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.TYPE_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao.DOMAIN_OBJECT_TYPE_ID_TABLE;

/**
 * @author vmatsukevich
 *         Date: 5/29/13
 *         Time: 12:39 PM
 */
public class PostgreSqlQueryHelperTest {

    private DomainObjectTypeConfig domainObjectTypeConfig;

    @Before
    public void setUp() throws Exception {
        initDomainObjectConfig();
    }

    @Test
    public void testGenerateCountTablesQuery() {
        String query = "select count(table_name) FROM information_schema.tables WHERE table_schema = 'public'";
        String testQuery = PostgreSqlQueryHelper.generateCountTablesQuery();
        assertEquals(testQuery, query);
    }

    @Test
    public void testGenerateCreateDomainObjectTableQuery() {
        String query = "create table \"" + DOMAIN_OBJECT_TYPE_ID_TABLE + "\" (" +
                "\"id\" bigint not null default nextval('\"" + DOMAIN_OBJECT_TYPE_ID_TABLE + "_seq" + "\"'), " +
                "\"name\" varchar(256) not null, " +
                "constraint \"pk_" + DOMAIN_OBJECT_TYPE_ID_TABLE + "\" primary key (\"id\"), " +
                "constraint \"u_" + DOMAIN_OBJECT_TYPE_ID_TABLE + "\" unique (\"name\"))";
        String testQuery = PostgreSqlQueryHelper.generateCreateDomainObjectTypeIdTableQuery();
        assertEquals(query, testQuery);
    }

    @Test
    public void testGenerateCreateConfigurationTableQuery() {
        String query = "create table \"" + CONFIGURATION_TABLE + "\" (\"id\" bigserial not null, " +
                "\"content\" text not null, \"loaded_date\" timestamp not null, " +
                "constraint \"pk_" + CONFIGURATION_TABLE + "\" primary key (\"id\"))";
        String testQuery = PostgreSqlQueryHelper.generateCreateConfigurationTableQuery();
        assertEquals(query, testQuery);
    }

    @Test
    public void testGenerateCreateAuthenticationInfoTableQuery() {
        String query = "CREATE TABLE \"" + AUTHENTICATION_INFO_TABLE + "\" (\"id\" bigint not null, " +
                "\"user_uid\" character varying(64) NOT NULL, \"password\" character varying(128), " +
                "constraint \"pk_" + AUTHENTICATION_INFO_TABLE + "_id\" primary key (\"id\"), " +
                "constraint \"u_" + AUTHENTICATION_INFO_TABLE + "_user_uid\" unique(\"user_uid\"))";
        String testQuery = PostgreSqlQueryHelper.generateCreateAuthenticationInfoTableQuery();
        assertEquals(query, testQuery);
    }

    @Test
    public void testGenerateSequenceQuery() {
        String query = "create sequence \"outgoing_document_seq\"";
        String testQuery = PostgreSqlQueryHelper.generateSequenceQuery(domainObjectTypeConfig);
        assertEquals(query, testQuery);
    }

    @Test
     public void testGenerateCreateTableQuery() throws Exception {
        String query = PostgreSqlQueryHelper.generateCreateTableQuery(domainObjectTypeConfig);
        String checkQuery = "create table \"outgoing_document\" ( \"id\" bigint not null, \"" + TYPE_COLUMN +
                "\" integer, \"registration_number\" varchar(128), " +
                "\"registration_date\" timestamp, \"author\" bigint, \"author_type\" integer, " +
                "\"long_field\" bigint, \"decimal_field_1\" decimal(10, 2), \"decimal_field_2\" decimal(10), " +
                "constraint \"pk_outgoing_document_id\" primary key (\"id\"), " +
                "constraint \"u_outgoing_document_id_id_type\" unique (\"id\", \"" + TYPE_COLUMN + "\"), " +
                "constraint \"fk_outgoing_document_id\"" + " foreign key (\"id\") references " +
                "\"document\" (\"id\"), constraint \"fk_outgoing_document_" + TYPE_COLUMN + "\" " +
                "foreign key (\""+ TYPE_COLUMN + "\") references \"domain_object_type_id\" (\"id\"))";
        assertEquals(checkQuery, query);
    }

    @Test
    public void testGenerateCreateTableQueryWithoutExtendsAttribute() throws Exception {
        String checkQuery = "create table \"outgoing_document\" ( \"id\" bigint not null, \"" + TYPE_COLUMN + "\" integer, " +
                "\"created_date\" timestamp not null, " + "\"updated_date\" timestamp not null, \"status\" bigint, " +
                "\"status_type\" integer, " +
                "\"registration_number\" varchar(128), \"registration_date\" timestamp, \"author\" bigint, " +
                "\"author_type\" integer, " +
                "\"long_field\" bigint, \"decimal_field_1\" decimal(10, 2), \"decimal_field_2\" decimal(10), " +
                "constraint \"pk_outgoing_document_id\" primary key (\"id\"), " +
                "constraint \"u_outgoing_document_id_id_type\" unique (\"id\", \"" + TYPE_COLUMN + "\"), " +
                "constraint \"fk_outgoing_document_" + TYPE_COLUMN + "\" foreign key (\"" + TYPE_COLUMN + "\") " +
                "references \"domain_object_type_id\" (\"id\"))";
        domainObjectTypeConfig.setExtendsAttribute(null);
        String query = PostgreSqlQueryHelper.generateCreateTableQuery(domainObjectTypeConfig);
        assertEquals(checkQuery, query);
    }

    @Test
    public void testGenerateCreateAclTableQuery() throws Exception {
        String query = PostgreSqlQueryHelper.generateCreateAclTableQuery(domainObjectTypeConfig);

        String checkQuery = "create table \"outgoing_document_acl\" (\"object_id\" bigint not null, " +
                "\"group_id\" bigint not null, \"operation\" varchar(256) not null, " +
                "constraint \"pk_outgoing_document_acl\" primary key (\"object_id\", \"group_id\", \"operation\"), " +
                "CONSTRAINT \"fk_outgoing_document_acl_outgoing_document\" FOREIGN KEY (\"object_id\") " +
                "REFERENCES \"outgoing_document\" (\"id\"), " +
                "CONSTRAINT \"fk_outgoing_document_user_group\" FOREIGN KEY (\"group_id\") REFERENCES " +
                "\"user_group\" (\"id\"))";
        assertEquals(checkQuery, query);
    }

    @Test
    public void testGenerateCreateAclReadTableQuery() throws Exception {
        String query = PostgreSqlQueryHelper.generateCreateAclReadTableQuery(domainObjectTypeConfig);

        String checkQuery = "create table \"outgoing_document_read\" (\"object_id\" bigint not null, " +
                "\"group_id\" bigint not null, constraint \"pk_outgoing_document_read\" " +
                "primary key (\"object_id\", \"group_id\"), " +
                "CONSTRAINT \"fk_outgoing_document_read_outgoing_document\" FOREIGN KEY (\"object_id\") " +
                "REFERENCES \"outgoing_document\" (\"id\"), " +
                "CONSTRAINT \"fk_outgoing_document_user_group\" FOREIGN KEY (\"group_id\") " +
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


        String testQuery = PostgreSqlQueryHelper.generateAddColumnsQuery(domainObjectTypeConfig.getName(), newColumns);

        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testGenerateCreateForeignKeyAndUniqueConstraintsQuery() {
        String expectedQuery = "alter table \"outgoing_document\" " +
                "add constraint \"fk_outgoing_document_executor_executor_type\" " +
                "foreign key (\"executor\", \"executor_type\") references \"employee\" (\"" + ID_COLUMN + "\", \"" +
                TYPE_COLUMN + "\"), " +
                "add constraint \"u_outgoing_document_registration_number\"" + " unique (\"registration_number\")";

        ReferenceFieldConfig executorFieldConfig = new ReferenceFieldConfig();
        executorFieldConfig.setName("Executor");
        executorFieldConfig.setType("Employee");
        executorFieldConfig.setNotNull(true);

        List<ReferenceFieldConfig> newColumns = new ArrayList<>();
        newColumns.add(executorFieldConfig);

        UniqueKeyFieldConfig uniqueKeyFieldConfig = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig.setName("Registration_Number");

        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig);

        String testQuery = PostgreSqlQueryHelper.generateCreateForeignKeyAndUniqueConstraintsQuery
                (domainObjectTypeConfig.getName(), newColumns, Collections.singletonList(uniqueKeyConfig));

        assertEquals(expectedQuery, testQuery);
    }

    @Test
    public void testGenerateCreateIndexesQuery() throws Exception {
        String query = PostgreSqlQueryHelper.generateCreateIndexesQuery(domainObjectTypeConfig.getName(),
                domainObjectTypeConfig.getFieldConfigs());
        String checkQuery = "create index \"i_outgoing_document_author\" on \"outgoing_document\" (\"author\");\n";
        assertEquals(query, checkQuery);
    }



    private void initDomainObjectConfig() {
        domainObjectTypeConfig = new DomainObjectTypeConfig();
        domainObjectTypeConfig.setName("Outgoing_Document");
        domainObjectTypeConfig.setExtendsAttribute("Document");

        StringFieldConfig registrationNumber = new StringFieldConfig();
        registrationNumber.setName("Registration_Number");
        registrationNumber.setLength(128);
        domainObjectTypeConfig.getFieldConfigs().add(registrationNumber);

        DateTimeFieldConfig registrationDate = new DateTimeFieldConfig();
        registrationDate.setName("Registration_Date");
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
        uniqueKeyFieldConfig1.setName("Registration_Number");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig1);

        UniqueKeyFieldConfig uniqueKeyFieldConfig2 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig2.setName("Registration_Date");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig2);
    }
}
