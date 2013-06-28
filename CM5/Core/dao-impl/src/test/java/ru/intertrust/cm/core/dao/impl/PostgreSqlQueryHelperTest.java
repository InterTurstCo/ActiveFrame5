package ru.intertrust.cm.core.dao.impl;

import org.junit.Before;
import org.junit.Test;
import ru.intertrust.cm.core.config.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static ru.intertrust.cm.core.dao.api.DataStructureDao.*;
import static ru.intertrust.cm.core.dao.api.ConfigurationDao.CONFIGURATION_TABLE;

/**
 * @author vmatsukevich
 *         Date: 5/29/13
 *         Time: 12:39 PM
 */
public class PostgreSqlQueryHelperTest {

    private DomainObjectConfig domainObjectConfig;

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
        String query = "create table " + DOMAIN_OBJECT_TABLE + "(ID bigserial not null, NAME varchar(256) not null, " +
                "constraint PK_" + DOMAIN_OBJECT_TABLE + " primary key (ID), constraint U_" + DOMAIN_OBJECT_TABLE + " unique (NAME))";
        String testQuery = PostgreSqlQueryHelper.generateCreateDomainObjectTableQuery();
        assertEquals(testQuery, query);
    }

    @Test
    public void testGenerateCreateConfigurationTableQuery() {
        String query = "create table " + CONFIGURATION_TABLE + "(ID bigserial not null, CONTENT text not null, " +
                "LOADED_DATE timestamp not null, constraint PK_" + CONFIGURATION_TABLE + " primary key (ID))";
        String testQuery = PostgreSqlQueryHelper.generateCreateConfigurationTableQuery();
        assertEquals(testQuery, query);
    }

    @Test
    public void testGenerateCreateAuthenticationInfoTableQuery() {
        String query = "CREATE TABLE " + AUTHENTICATION_INFO_TABLE + " (ID bigint not null, " +
                "user_uid character varying(64) NOT NULL, password character varying(128), constraint PK_" +
                AUTHENTICATION_INFO_TABLE + "_ID primary key (ID), constraint U_" + AUTHENTICATION_INFO_TABLE +
                "_USER_UID unique(user_uid))";
        String testQuery = PostgreSqlQueryHelper.generateCreateAuthenticationInfoTableQuery();
        assertEquals(testQuery, query);
    }

    @Test
    public void testGenerateSequenceQuery() {
        String query = "create sequence OUTGOING_DOCUMENT_SEQ";
        String testQuery = PostgreSqlQueryHelper.generateSequenceQuery(domainObjectConfig);
        assertEquals(testQuery, query);
    }

    @Test
    public void testGenerateCreateTableQuery() throws Exception {
        String query = PostgreSqlQueryHelper.generateCreateTableQuery(domainObjectConfig);
        String checkQuery = "create table OUTGOING_DOCUMENT ( ID bigint not null, CREATED_DATE timestamp not null, " +
                "UPDATED_DATE timestamp not null, REGISTRATION_NUMBER varchar(128), REGISTRATION_DATE timestamp, AUTHOR bigint, " +
                "LONG_FIELD bigint, DECIMAL_FIELD_1 decimal(10, 2), DECIMAL_FIELD_2 decimal(10), " +
                "constraint PK_OUTGOING_DOCUMENT_ID primary key (ID), " +
                "constraint U_OUTGOING_DOCUMENT_REGISTRATION_NUMBER_REGISTRATION_DATE unique (REGISTRATION_NUMBER, REGISTRATION_DATE), " +
                "constraint FK_OUTGOING_DOCUMENT_AUTHOR foreign key (AUTHOR) references EMPLOYEE(ID))";
        assertEquals(query, checkQuery);
    }

    @Test
    public void testGenerateUpdateTableQuery() {
        String query = "alter table OUTGOING_DOCUMENT " +
                "add column DESCRIPTION varchar(256), " +
                "add column EXECUTOR bigint not null, " +
                "add constraint FK_OUTGOING_DOCUMENT_EXECUTOR foreign key (EXECUTOR) references EMPLOYEE(ID), " +
                "add constraint U_OUTGOING_DOCUMENT_REGISTRATION_NUMBER unique (REGISTRATION_NUMBER)";

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


        String testQuery = PostgreSqlQueryHelper.generateUpdateTableQuery(domainObjectConfig.getName(), newColumns,
                newUniqueConfigs);

        assertEquals(testQuery, query);
    }

    @Test
    public void testGenerateCreateIndexesQuery() throws Exception {
        String query = PostgreSqlQueryHelper.generateCreateIndexesQuery(domainObjectConfig.getName(),
                domainObjectConfig.getFieldConfigs());
        String checkQuery = "create index I_OUTGOING_DOCUMENT_AUTHOR on OUTGOING_DOCUMENT (AUTHOR);\n";
        assertEquals(query, checkQuery);
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
