package ru.intertrust.cm.core.dao.impl;

import org.junit.Before;
import org.junit.Test;
import ru.intertrust.cm.core.config.model.*;

import static org.junit.Assert.assertEquals;

/**
 * @author vmatsukevich
 *         Date: 5/29/13
 *         Time: 12:39 PM
 */
public class PostgreSQLQueryHelperTest {

    private DomainObjectConfig domainObjectConfig;

    @Before
    public void setUp() throws Exception {
        initBusinessObjectConfig();
    }

    @Test
    public void testGenerateCreateTableQuery() throws Exception {
        String query = PostgreSQLQueryHelper.generateCreateTableQuery(domainObjectConfig);
        String checkQuery = "create table OUTGOING_DOCUMENT ( ID bigint not null, CREATED_DATE timestamp not null, " +
                "UPDATED_DATE timestamp not null, REGISTRATION_NUMBER varchar(128), REGISTRATION_DATE timestamp, AUTHOR bigint, " +
                "LONG_FIELD bigint, DECIMAL_FIELD_1 decimal(10, 2), DECIMAL_FIELD_2 decimal(10), " +
                "constraint PK_OUTGOING_DOCUMENT_ID primary key (ID), " +
                "constraint U_OUTGOING_DOCUMENT_REGISTRATION_NUMBER_REGISTRATION_DATE unique (REGISTRATION_NUMBER, REGISTRATION_DATE), " +
                "constraint FK_OUTGOING_DOCUMENT_AUTHOR foreign key (AUTHOR) references EMPLOYEE(ID))";
        assertEquals(query, checkQuery);
    }

    @Test
    public void testGenerateCreateIndexesQuery() throws Exception {
        String query = PostgreSQLQueryHelper.generateCreateIndexesQuery(domainObjectConfig);
        String checkQuery = "create index I_OUTGOING_DOCUMENT_AUTHOR on OUTGOING_DOCUMENT (AUTHOR) ;\n";
        assertEquals(query, checkQuery);
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
