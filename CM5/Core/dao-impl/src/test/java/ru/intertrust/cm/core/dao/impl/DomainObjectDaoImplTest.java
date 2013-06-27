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

    private DomainObjectConfig domainObjectConfig;

    private static final String COLLECTION_COUNT_WITH_FILTERS = "select count(*) from employee e inner join department d on e.department = d.id WHERE d.name = 'dep1' and e.name = 'employee1'";
    private static final String COLLECTION_QUERY_WITH_LIMITS = "select e.id, e.name, e.position, e.created_date, e.updated_date from employee e inner join department d on e.department = d.id where d.name = 'dep1' order by e.name asc limit 100 OFFSET 10";
    private static final String COLLECTION_QUERY_WITHOUT_FILTERS = "select e.id, e.name, e.position, e.created_date, e.updated_date from employee e where 1=1 order by e.name asc";
    private static final String FIND_COLLECTION_QUERY_WITH_FILTERS = "select e.id, e.name, e.position, e.created_date, e.updated_date from employee e inner join department d on e.department = d.id where d.name = 'dep1' order by e.name asc";
    private static final String DOMAIN_OBJECTS_CONFIG_PATH = "test-config/domain-objects.xml";
    private static final String COLLECTIONS_CONFIG_PATH = "test-config/collections.xml";
    private static final String CONFIGURATION_SCHEMA_PATH = "test-config/configuration.xsd";
    private static final Set<String> CONFIG_PATHS = new HashSet<>(Arrays.asList(
            new String[]{DOMAIN_OBJECTS_CONFIG_PATH, COLLECTIONS_CONFIG_PATH}));

    @InjectMocks
    private DomainObjectDaoImpl domainObjectDaoImpl = new DomainObjectDaoImpl();
    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ConfigurationLogicalValidator logicalValidator;

    private CollectionFilterConfig byDepartmentFilterConfig;

    private CollectionFilterConfig byNameFilterConfig;

    private CollectionConfig collectionConfig;

    private ConfigurationExplorerImpl configurationExplorer;

    private SortOrder sortOrder;



    @Before
    public void setUp() throws Exception {
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setConfigurationFilePaths(CONFIG_PATHS);
        configurationSerializer.setConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationExplorer = new ConfigurationExplorerImpl();
        Configuration configuration = configurationSerializer.serializeConfiguration();
        configurationExplorer.setConfiguration(configuration);
        configurationExplorer.build();

        domainObjectConfig = configurationExplorer.getDomainObjectConfig("Person");
        collectionConfig = configurationExplorer.getCollectionConfig("Employees");
        byDepartmentFilterConfig = createByDepartmentFilterConfig();
        byNameFilterConfig = createByNameFilterConfig();

        sortOrder = createByNameSortOrder();

    }

    @Test
    public void testGenerateCreateQuery() throws Exception {

        DomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(domainObjectConfig.getName());
        domainObject.setValue("EMail", new StringValue("testCreate@test.com"));
        domainObject.setValue("Login", new StringValue("userCreate"));
        domainObject.setValue("Password", new StringValue("passCreate"));

        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);

        String checkCreateQuery =
                "insert into PERSON (ID , CREATED_DATE, UPDATED_DATE, EMAIL,LOGIN,PASSWORD) values (:id , :created_date, :updated_date, :email,:login,:password)";

        String query = domainObjectDaoImpl.generateCreateQuery(domainObject, domainObjectConfig);
        assertEquals(checkCreateQuery, query);

    }

    @Test
    public void testUpdateThrowsInvalidIdException() {

        DomainObject domainObject = new GenericDomainObject();
        domainObject.setId(null);
        domainObject.setTypeName(domainObjectConfig.getName());
        domainObject.setValue("EMail", new StringValue("testUpdate@test.com"));
        domainObject.setValue("Login", new StringValue("userUpdate"));
        domainObject.setValue("Password", new StringValue("passUpdate"));

        //проверяем что идентификатор не нулевой
        try {
            domainObjectDaoImpl.update(domainObject, domainObjectConfig);
        } catch (Exception e) {

            assertTrue(e instanceof InvalidIdException);

        }

        //проверяем что обрабатываеться неккоректный тип идентификатора
        try {
            domainObject.setId(new TestId());
            domainObjectDaoImpl.update(domainObject, domainObjectConfig);
        } catch (Exception e) {

            assertTrue(e instanceof InvalidIdException);

        }

    }



    @Test
    public void testGenerateUpdateQuery() throws Exception {

        DomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(domainObjectConfig.getName());
        domainObject.setValue("EMail", new StringValue("testUpdate@test.com"));
        domainObject.setValue("Login", new StringValue("userUpdate"));
        domainObject.setValue("Password", new StringValue("passUpdate"));

        Date currentDate = new Date();
        domainObject.setId(new RdbmsId("person", 1));
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);

        String checkUpdateQuery = "update PERSON set UPDATED_DATE=:current_date, EMAIL=:email,LOGIN=:login,PASSWORD=:password where ID=:id and UPDATED_DATE=:updated_date";

        String query = domainObjectDaoImpl.generateUpdateQuery(domainObject, domainObjectConfig);
        assertEquals(checkUpdateQuery, query);

    }

    @Test
    public void testGenerateDeleteQuery() throws Exception {

        String checkDeleteQuery = "delete from PERSON where id=:id";

        String query = domainObjectDaoImpl.generateDeleteQuery(domainObjectConfig);
        assertEquals(checkDeleteQuery, query);

    }


    @Test
    public void testGenerateDeleteAllQuery() throws Exception {

        String checkDeleteQuery = "delete from PERSON";

        String query = domainObjectDaoImpl.generateDeleteAllQuery(domainObjectConfig);
        assertEquals(checkDeleteQuery, query);

    }


    @Test
    public void testGenerateExistsQuery() throws Exception {

        String checkExistsQuery = "select id from PERSON where id=:id";

        String query = domainObjectDaoImpl.generateExistsQuery(domainObjectConfig.getName());
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

        domainObjectConfig = new DomainObjectConfig();
        domainObjectConfig.setName("person");
        StringFieldConfig email = new StringFieldConfig();
        email.setName("EMail");
        email.setLength(128);
        domainObjectConfig.getFieldConfigs().add(email);

        StringFieldConfig Login = new StringFieldConfig();
        Login.setName("Login");
        Login.setLength(64);
        Login.setNotNull(true);
        domainObjectConfig.getFieldConfigs().add(Login);

        StringFieldConfig Password = new StringFieldConfig();
        Password.setName("Password");
        Password.setLength(128);
        domainObjectConfig.getFieldConfigs().add(Password);

        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        domainObjectConfig.getUniqueKeyConfigs().add(uniqueKeyConfig);

        UniqueKeyFieldConfig uniqueKeyFieldConfig1 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig1.setName("EMail");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig1);

    }

    private SortOrder createByNameSortOrder() {
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("e.name", SortCriterion.Order.ASCENDING));
        return sortOrder;
    }

    private CollectionFilterConfig createByDepartmentFilterConfig() {
        CollectionFilterConfig byDepartmentFilterConfig = new CollectionFilterConfig();
        byDepartmentFilterConfig.setName("byDepartment");
        CollectionFilterReferenceConfig collectionFilterReference = new CollectionFilterReferenceConfig();

        collectionFilterReference.setPlaceholder("from-clause");
        collectionFilterReference.setValue("inner join department d on e.department = d.id");

        CollectionFilterCriteriaConfig collectionFilterCriteriaConfig = new CollectionFilterCriteriaConfig();
        collectionFilterCriteriaConfig.setCondition(" and ");
        collectionFilterCriteriaConfig.setPlaceholder("where-clause");
        collectionFilterCriteriaConfig.setValue(" d.name = 'dep1'");

        byDepartmentFilterConfig.setFilterReference(collectionFilterReference);
        byDepartmentFilterConfig.setFilterCriteria(collectionFilterCriteriaConfig);
        return byDepartmentFilterConfig;
    }

    private CollectionFilterConfig createByNameFilterConfig() {
        CollectionFilterConfig byNameFilterConfig = new CollectionFilterConfig();
        byNameFilterConfig.setName("byName");

        CollectionFilterCriteriaConfig collectionFilterCriteriaConfig = new CollectionFilterCriteriaConfig();
        collectionFilterCriteriaConfig.setCondition(" and ");
        collectionFilterCriteriaConfig.setPlaceholder("where-clause");
        collectionFilterCriteriaConfig.setValue(" e.name = 'employee1' ");

        byNameFilterConfig.setFilterCriteria(collectionFilterCriteriaConfig);
        return byNameFilterConfig;
    }

    @Test
    public void testFindCollectionWithFilters() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();

        filledFilterConfigs.add(byDepartmentFilterConfig);

        String actualQuery = domainObjectDaoImpl.getFindCollectionQuery(collectionConfig, filledFilterConfigs, sortOrder, 0, 0);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(FIND_COLLECTION_QUERY_WITH_FILTERS, refinedActualQuery);
        System.out.print("!!! " + refineQuery(actualQuery) + "\n");

    }

    @Test
    public void testFindCollectionWithoutFilters() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();
        String actualQuery = domainObjectDaoImpl.getFindCollectionQuery(collectionConfig, filledFilterConfigs, sortOrder, 0, 0);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(COLLECTION_QUERY_WITHOUT_FILTERS, refinedActualQuery);
        System.out.print(refinedActualQuery);

    }

    @Test
    public void testFindCollectionWithLimits() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();
        filledFilterConfigs.add(byDepartmentFilterConfig);

        String actualQuery = domainObjectDaoImpl.getFindCollectionQuery(collectionConfig, filledFilterConfigs, sortOrder, 10, 100);
        String refinedActualQuery = refineQuery(actualQuery);

        assertEquals(COLLECTION_QUERY_WITH_LIMITS, refinedActualQuery);

        System.out.print("\n " + refinedActualQuery);

    }

    @Test
    public void testFindCollectionCountWithFilters() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();
        filledFilterConfigs.add(byDepartmentFilterConfig);
        filledFilterConfigs.add(byNameFilterConfig);

        String actualQuery = domainObjectDaoImpl.getFindCollectionCountQuery(collectionConfig, filledFilterConfigs);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(COLLECTION_COUNT_WITH_FILTERS, refinedActualQuery);

        System.out.print("\n " + refinedActualQuery);

    }

    private String refineQuery(String actualQuery) {
        return actualQuery.trim().replaceAll("\\s+", " ");
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
