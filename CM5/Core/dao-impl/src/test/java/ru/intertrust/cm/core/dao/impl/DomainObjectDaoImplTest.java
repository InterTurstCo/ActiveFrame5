package ru.intertrust.cm.core.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationLogicalValidator;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.model.*;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Юнит тест для DomainObjectDaoImpl
 *
 * @author skashanski
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class DomainObjectDaoImplTest {

    private static final String DOMAIN_OBJECTS_CONFIG_PATH = "test-config/domain-objects-test.xml";
    private static final String COLLECTIONS_CONFIG_PATH = "test-config/collections-test.xml";
    private static final String CONFIGURATION_SCHEMA_PATH = "test-config/configuration-test.xsd";
    private static final Set<String> CONFIG_PATHS =
            new HashSet<>(Arrays.asList(new String[]{DOMAIN_OBJECTS_CONFIG_PATH, COLLECTIONS_CONFIG_PATH}));

    @InjectMocks
    private DomainObjectDaoImpl domainObjectDaoImpl = new DomainObjectDaoImpl();

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ConfigurationLogicalValidator logicalValidator;

    private ConfigurationExplorerImpl configurationExplorer;

    private DomainObjectTypeConfig domainObjectTypeConfig;

    @Before
    public void setUp() throws Exception {
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setConfigurationFilePaths(CONFIG_PATHS);
        configurationSerializer.setConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationExplorer = new ConfigurationExplorerImpl();
        Configuration configuration = configurationSerializer.serializeConfiguration();
        configurationExplorer.setConfiguration(configuration);
        configurationExplorer.build();

        domainObjectDaoImpl.setConfigurationExplorer(configurationExplorer);

        domainObjectTypeConfig = configurationExplorer.getDomainObjectTypeConfig("Person");
    }

    @Test
    public void testGenerateCreateQuery() throws Exception {

        DomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(domainObjectTypeConfig.getName());
        domainObject.setValue("EMail", new StringValue("testCreate@test.com"));
        domainObject.setValue("Login", new StringValue("userCreate"));
        domainObject.setValue("Password", new StringValue("passCreate"));

        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);

        String checkCreateQuery =
                "insert into PERSON (ID, PARENT, CREATED_DATE, UPDATED_DATE, EMAIL,LOGIN,PASSWORD) values (:id , " +
                        ":parent, :created_date, :updated_date, :email,:login,:password)";

        String query = domainObjectDaoImpl.generateCreateQuery(domainObjectTypeConfig);
        assertEquals(checkCreateQuery, query);

    }

    @Test
    public void testUpdateThrowsInvalidIdException() {

        DomainObject domainObject = new GenericDomainObject();
        domainObject.setId(null);
        domainObject.setTypeName(domainObjectTypeConfig.getName());
        domainObject.setValue("EMail", new StringValue("testUpdate@test.com"));
        domainObject.setValue("Login", new StringValue("userUpdate"));
        domainObject.setValue("Password", new StringValue("passUpdate"));

        //проверяем что идентификатор не нулевой
        try {
            domainObjectDaoImpl.update(domainObject);
        } catch (Exception e) {

            assertTrue(e instanceof InvalidIdException);

        }

        //проверяем что обрабатываеться неккоректный тип идентификатора
        try {
            domainObject.setId(new TestId());
            domainObjectDaoImpl.update(domainObject);
        } catch (Exception e) {

            assertTrue(e instanceof InvalidIdException);

        }

    }



    @Test
    public void testGenerateUpdateQuery() throws Exception {

        DomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(domainObjectTypeConfig.getName());
        domainObject.setValue("EMail", new StringValue("testUpdate@test.com"));
        domainObject.setValue("Login", new StringValue("userUpdate"));
        domainObject.setValue("Password", new StringValue("passUpdate"));

        Date currentDate = new Date();
        domainObject.setId(new RdbmsId("person", 1));
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);

        String checkUpdateQuery = "update PERSON set UPDATED_DATE=:current_date, PARENT=:parent, EMAIL=:email," +
                "LOGIN=:login,PASSWORD=:password where ID=:id and UPDATED_DATE=:updated_date";

        String query = domainObjectDaoImpl.generateUpdateQuery(domainObjectTypeConfig);
        assertEquals(checkUpdateQuery, query);

    }

    @Test
    public void testGenerateDeleteQuery() throws Exception {

        String checkDeleteQuery = "delete from PERSON where id=:id";

        String query = domainObjectDaoImpl.generateDeleteQuery(domainObjectTypeConfig);
        assertEquals(checkDeleteQuery, query);

    }


    @Test
    public void testGenerateDeleteAllQuery() throws Exception {

        String checkDeleteQuery = "delete from PERSON";

        String query = domainObjectDaoImpl.generateDeleteAllQuery(domainObjectTypeConfig);
        assertEquals(checkDeleteQuery, query);

    }


    @Test
    public void testGenerateExistsQuery() throws Exception {

        String checkExistsQuery = "select id from PERSON where id=:id";

        String query = domainObjectDaoImpl.generateExistsQuery(domainObjectTypeConfig.getName());
        assertEquals(checkExistsQuery, query);

    }

    private void initDomainObjectConfig() {

        /*
         * Создаем конфигурацию следующего ввида <domain-object name="Person">
         * <fields> <string name="EMail" length="128" /> <string name="Login"
         * length="64" not-null="true" /> <password name="Password" length="128"
         * /> </fields> <uniqueKey> <!-- This key means automatic key + index
         * creation--> <field name="EMail"/> </uniqueKey> </domain-object>
         */

        domainObjectTypeConfig = new DomainObjectTypeConfig();
        domainObjectTypeConfig.setName("person");
        StringFieldConfig email = new StringFieldConfig();
        email.setName("EMail");
        email.setLength(128);
        domainObjectTypeConfig.getFieldConfigs().add(email);

        StringFieldConfig Login = new StringFieldConfig();
        Login.setName("Login");
        Login.setLength(64);
        Login.setNotNull(true);
        domainObjectTypeConfig.getFieldConfigs().add(Login);

        StringFieldConfig Password = new StringFieldConfig();
        Password.setName("Password");
        Password.setLength(128);
        domainObjectTypeConfig.getFieldConfigs().add(Password);

        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        domainObjectTypeConfig.getUniqueKeyConfigs().add(uniqueKeyConfig);

        UniqueKeyFieldConfig uniqueKeyFieldConfig1 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig1.setName("EMail");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig1);

    }

    /**
     * Тестовый класс для проверки обработки неккоректного типа идентификатора
     * @author skashanski
     *
     */
    class TestId implements Id {

        @Override
        public void setFromStringRepresentation(String stringRep) {
            // TODO Auto-generated method stub

        }

        @Override
        public String toStringRepresentation() {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
