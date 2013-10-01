package ru.intertrust.cm.core.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.model.*;
import ru.intertrust.cm.core.config.model.base.*;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author vmatsukevich
 *         Date: 7/2/13
 *         Time: 12:55 PM
 */
public class CollectionsDaoImplTest {

    private static final String COLLECTION_ACL_QUERY = "EXISTS (SELECT r.object_id FROM employee_READ AS r INNER JOIN" +
            " group_member AS gm ON r.group_id = gm.master WHERE gm.person_id = :user_id AND r.object_id = id) ";

    private static final String COLLECTION_COUNT_WITH_FILTERS =
            "SELECT count(*), 'employee' AS TYPE_CONSTANT FROM employee AS e " +
            "INNER JOIN department AS d ON e.department = d.id WHERE EXISTS " +
            "(SELECT r.object_id FROM employee_READ AS r INNER JOIN group_member AS gm ON r.group_id = gm.master " +
                    "WHERE gm.person_id = :user_id " +
            "AND r.object_id = id) " +
            "AND 1 = 1 AND d.name = 'dep1' AND e.name = 'employee1'";

    private static final String COLLECTION_QUERY_WITH_LIMITS =
            "SELECT e.id, e.name, e.position, e.created_date, e.updated_date, 'employee' AS TYPE_CONSTANT " +
            "FROM employee AS e INNER JOIN department AS d ON e.department = d.id WHERE " + COLLECTION_ACL_QUERY +
            "AND 1 = 1 AND d.name = 'dep1' ORDER BY e.name LIMIT 100 OFFSET 10";

    private static final String FIND_COLLECTION_QUERY_WITH_FILTERS =
            "SELECT e.id, e.name, e.position, e.created_date, e.updated_date, 'employee' AS TYPE_CONSTANT " +
            "FROM employee AS e " +
            "INNER JOIN department AS d ON e.department = d.id WHERE " +
            COLLECTION_ACL_QUERY +
            "AND 1 = 1 AND d.name = 'dep1' ORDER BY e.name";

    private static final String FIND_COLLECTION_QUERY_WITH_MULTIPLE_TYPE_REFERENCE =
            "SELECT p.id, p.login, p.password, (CASE WHEN p.BOSS1 IS NOT NULL THEN p.BOSS1 WHEN p.BOSS2 IS NOT NULL " +
                    "THEN p.BOSS2 ELSE NULL END) AS BOSS, p.created_date, p.updated_date, 'person' AS TYPE_CONSTANT " +
                    "FROM person AS p WHERE EXISTS (SELECT r.object_id FROM person_READ AS r " +
                    "INNER JOIN group_member AS gm ON r.group_id = gm.master WHERE gm.person_id = :user_id AND " +
                    "r.object_id = id) AND 1 = 1";

    private static final String FIND_COMPLEX_COLLECTION_QUERY_WITH_FILTERS =
            "SELECT e.id, e.name, e.position, e.created_date, e.updated_date, 'employee' AS TYPE_CONSTANT FROM employee AS e " +
            "INNER JOIN department AS d ON e.department = d.id " +
            "INNER JOIN authentication_info AS a ON e.login = a.id WHERE " +
            COLLECTION_ACL_QUERY +
            "AND 1 = 1 AND d.name = 'dep1' AND e.name = 'employee1' AND a.id = 1 ORDER BY e.name";

    private static final String COLLECTION_QUERY_WITHOUT_FILTERS =
             "SELECT e.id, e.name, e.position, e.created_date, e.updated_date, 'employee' AS TYPE_CONSTANT FROM employee AS e WHERE " + COLLECTION_ACL_QUERY +
             "AND 1 = 1 ORDER BY e.name";


    private static final String COLLECTION_QUERY_WITHOUT_SORT_ORDER =
            "SELECT e.id, e.name, e.position, e.created_date, e.updated_date, 'employee' AS TYPE_CONSTANT FROM employee AS e WHERE "
                    + COLLECTION_ACL_QUERY +
                    "AND 1 = 1";

    private static final String EMLOYEES_PROROTYPE = "select\n" +
            "                    e.id, e.name, e.position, e.created_date, e.updated_date\n" +
            "                from\n" +
            "                    employee e\n" +
            "                     ::from-clause\n" +
            "                where\n" +
            "                    1=1 ::where-clause";

    private static final String EMPLOYEES_COUNTING_PROTOTYPE = "select count(*) from employee e ::from-clause WHERE " +
            "1=1 ::where-clause";

    private static final String EMPLOYEES_COMPLEX_PROTOTYPE = "select\n" +
            "                    e.id, e.name, e.position, e.created_date, e.updated_date\n" +
            "                from\n" +
            "                    employee e\n" +
            "                     ::from-clause1 ::from-clause2\n" +
            "                where\n" +
            "                    1=1 ::where-clause1 ::where-clause2";

    private static final String PERSONS_PROROTYPE = "select\n" +
            "                    p.id, p.login, p.password, p.boss, p.created_date, p.updated_date\n" +
            "                from\n" +
            "                    person p\n" +
            "                     ::from-clause\n" +
            "                where\n" +
            "                    1=1 ::where-clause";

    private static final String PERSONS_COUNTING_PROTOTYPE = "select count(*) from person p ::from-clause WHERE " +
            "1=1 ::where-clause";

    @InjectMocks
    private final CollectionsDaoImpl collectionsDaoImpl = new CollectionsDaoImpl();

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ConfigurationExplorerImpl configurationExplorer;

    private CollectionFilterConfig byDepartmentFilterConfig;
    private CollectionFilterConfig byDepartmentComplexFilterConfig;
    private CollectionFilterConfig byNameFilterConfig;
    private CollectionFilterConfig byNameComplexFilterConfig;
    private CollectionFilterConfig byAuthenticationInfoFilterConfig;

    private CollectionConfig collectionConfig;
    private CollectionConfig complexCollectionConfig;
    private CollectionConfig personsCollectionConfig;

    private SortOrder sortOrder;

    @Before
    public void setUp() throws Exception {
        byDepartmentFilterConfig = createByDepartmentFilterConfig();
        byDepartmentComplexFilterConfig = createByDepartmentComplexFilterConfig();

        byNameFilterConfig = createByNameFilterConfig();
        byNameComplexFilterConfig = createByNameComplexFilterConfig();
        byAuthenticationInfoFilterConfig = createByAuthenticationInfoFilterConfig();

        sortOrder = createByNameSortOrder();

        collectionConfig = createEmployeesCollectionConfig();
        complexCollectionConfig = createEmployeesComplexCollectionConfig();
        personsCollectionConfig = createPersonsCollectionConfig();

        initConfigurationExplorer();
    }

    private void initConfigurationExplorer() {
        DomainObjectTypeConfig doTypeConfig = new DomainObjectTypeConfig();
        doTypeConfig.setName("Person");
        StringFieldConfig email = new StringFieldConfig();
        email.setName("EMail");
        email.setLength(128);
        doTypeConfig.getFieldConfigs().add(email);

        StringFieldConfig login = new StringFieldConfig();
        login.setName("Login");
        login.setLength(64);
        login.setNotNull(true);
        doTypeConfig.getFieldConfigs().add(login);

        StringFieldConfig password = new StringFieldConfig();
        password.setName("Password");
        password.setLength(128);
        doTypeConfig.getFieldConfigs().add(password);

        ReferenceFieldConfig boss = new ReferenceFieldConfig();
        boss.setName("Boss");
        boss.getTypes().add(new ReferenceFieldTypeConfig("Internal_Employee"));
        boss.getTypes().add(new ReferenceFieldTypeConfig("External_Employee"));
        doTypeConfig.getFieldConfigs().add(boss);


        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        doTypeConfig.getUniqueKeyConfigs().add(uniqueKeyConfig);

        UniqueKeyFieldConfig uniqueKeyFieldConfig1 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig1.setName("EMail");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig1);

        DomainObjectTypeConfig internalEmployee = new DomainObjectTypeConfig();
        internalEmployee.setName("Internal_Employee");

        DomainObjectTypeConfig externalEmployee = new DomainObjectTypeConfig();
        externalEmployee.setName("External_Employee");

        Configuration configuration = new Configuration();
        configuration.getConfigurationList().add(doTypeConfig);
        configuration.getConfigurationList().add(internalEmployee);
        configuration.getConfigurationList().add(externalEmployee);

        configurationExplorer = new ConfigurationExplorerImpl(configuration);
        collectionsDaoImpl.setConfigurationExplorer(configurationExplorer);
    }

    private AccessToken createMockAccessToken() {
        AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.isDeferred()).thenReturn(true);

        UserSubject subject = mock(UserSubject.class);
        when(subject.getUserId()).thenReturn(1);
        when(accessToken.getSubject()).thenReturn(subject);
        return accessToken;
    }
    //TODO uncomment tests when access list check will be returned.
    //@Test
    public void testFindCollectionWithFilters() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();

        filledFilterConfigs.add(byDepartmentFilterConfig);

        AccessToken accessToken = createMockAccessToken();
        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(collectionConfig, filledFilterConfigs, sortOrder, 0, 0, accessToken);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(FIND_COLLECTION_QUERY_WITH_FILTERS, refinedActualQuery);
    }

    //@Test
    public void testFindCollectionWithMultipleReferenceTypes() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();

        AccessToken accessToken = createMockAccessToken();
        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(personsCollectionConfig, filledFilterConfigs,
                new SortOrder(), 0, 0, accessToken);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(FIND_COLLECTION_QUERY_WITH_MULTIPLE_TYPE_REFERENCE, refinedActualQuery);
    }

    //@Test
    public void testFindCopmplexCollectionWithFilters() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();

        filledFilterConfigs.add(byDepartmentComplexFilterConfig);
        filledFilterConfigs.add(byNameComplexFilterConfig);
        filledFilterConfigs.add(byAuthenticationInfoFilterConfig);

        AccessToken accessToken = createMockAccessToken();
        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(complexCollectionConfig, filledFilterConfigs, sortOrder, 0, 0, accessToken);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(FIND_COMPLEX_COLLECTION_QUERY_WITH_FILTERS, refinedActualQuery);
    }

    //@Test
    public void testFindCollectionWithoutFilters() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();
        AccessToken accessToken = createMockAccessToken();

        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(collectionConfig, filledFilterConfigs, sortOrder, 0, 0, accessToken);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(COLLECTION_QUERY_WITHOUT_FILTERS, refinedActualQuery);
    }

    //@Test
    public void testFindCollectionWithoutSortOrder() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();
        AccessToken accessToken = createMockAccessToken();

        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(collectionConfig, filledFilterConfigs, null, 0, 0, accessToken);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(COLLECTION_QUERY_WITHOUT_SORT_ORDER, refinedActualQuery);
    }

    //@Test
    public void testFindCollectionWithLimits() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();
        filledFilterConfigs.add(byDepartmentFilterConfig);
        AccessToken accessToken = createMockAccessToken();

        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(collectionConfig, filledFilterConfigs, sortOrder, 10, 100, accessToken);
        String refinedActualQuery = refineQuery(actualQuery);

        assertEquals(COLLECTION_QUERY_WITH_LIMITS, refinedActualQuery);
    }

    //@Test
    public void testFindCollectionCountWithFilters() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();
        filledFilterConfigs.add(byDepartmentFilterConfig);
        filledFilterConfigs.add(byNameFilterConfig);

        AccessToken accessToken = createMockAccessToken();
        String actualQuery = collectionsDaoImpl.getFindCollectionCountQuery(collectionConfig, filledFilterConfigs, accessToken);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(COLLECTION_COUNT_WITH_FILTERS, refinedActualQuery);
    }

    private String refineQuery(String actualQuery) {
        return actualQuery.trim().replaceAll("\\s+", " ");
    }

    private CollectionFilterConfig createByDepartmentFilterConfig() {
        CollectionFilterConfig byDepartmentFilterConfig = new CollectionFilterConfig();
        byDepartmentFilterConfig.setName("byDepartment");
        CollectionFilterReferenceConfig collectionFilterReference = new CollectionFilterReferenceConfig();

        collectionFilterReference.setPlaceholder("from-clause");
        collectionFilterReference.setValue("inner join department d on e.department = d.id");

        CollectionFilterCriteriaConfig collectionFilterCriteriaConfig = new CollectionFilterCriteriaConfig();
        collectionFilterCriteriaConfig.setPlaceholder("where-clause");
        collectionFilterCriteriaConfig.setValue(" d.name = 'dep1'");

        byDepartmentFilterConfig.setFilterReference(collectionFilterReference);
        byDepartmentFilterConfig.setFilterCriteria(collectionFilterCriteriaConfig);
        return byDepartmentFilterConfig;
    }

    private CollectionFilterConfig createByDepartmentComplexFilterConfig() {
        CollectionFilterConfig byDepartmentFilterConfig = new CollectionFilterConfig();
        byDepartmentFilterConfig.setName("byDepartment");
        CollectionFilterReferenceConfig collectionFilterReference = new CollectionFilterReferenceConfig();

        collectionFilterReference.setPlaceholder("from-clause1");
        collectionFilterReference.setValue("inner join department d on e.department = d.id");

        CollectionFilterCriteriaConfig collectionFilterCriteriaConfig = new CollectionFilterCriteriaConfig();
         collectionFilterCriteriaConfig.setPlaceholder("where-clause1");
        collectionFilterCriteriaConfig.setValue(" d.name = 'dep1'");

        byDepartmentFilterConfig.setFilterReference(collectionFilterReference);
        byDepartmentFilterConfig.setFilterCriteria(collectionFilterCriteriaConfig);
        return byDepartmentFilterConfig;
    }

    private CollectionFilterConfig createByAuthenticationInfoFilterConfig() {
        CollectionFilterConfig byDepartmentFilterConfig = new CollectionFilterConfig();
        byDepartmentFilterConfig.setName("byAuthenticationInfo");
        CollectionFilterReferenceConfig collectionFilterReference = new CollectionFilterReferenceConfig();

        collectionFilterReference.setPlaceholder("from-clause2");
        collectionFilterReference.setValue("inner join authentication_info a on e.login = a.id");

        CollectionFilterCriteriaConfig collectionFilterCriteriaConfig = new CollectionFilterCriteriaConfig();
        collectionFilterCriteriaConfig.setPlaceholder("where-clause2");
        collectionFilterCriteriaConfig.setValue(" a.id = 1 ");

        byDepartmentFilterConfig.setFilterReference(collectionFilterReference);
        byDepartmentFilterConfig.setFilterCriteria(collectionFilterCriteriaConfig);
        return byDepartmentFilterConfig;
    }

    private CollectionFilterConfig createByNameFilterConfig() {
        CollectionFilterConfig byNameFilterConfig = new CollectionFilterConfig();
        byNameFilterConfig.setName("byName");

        CollectionFilterCriteriaConfig collectionFilterCriteriaConfig = new CollectionFilterCriteriaConfig();
        collectionFilterCriteriaConfig.setPlaceholder("where-clause");
        collectionFilterCriteriaConfig.setValue(" e.name = 'employee1' ");

        byNameFilterConfig.setFilterCriteria(collectionFilterCriteriaConfig);
        return byNameFilterConfig;
    }

    private CollectionFilterConfig createByNameComplexFilterConfig() {
        CollectionFilterConfig byNameFilterConfig = new CollectionFilterConfig();
        byNameFilterConfig.setName("byName");

        CollectionFilterCriteriaConfig collectionFilterCriteriaConfig = new CollectionFilterCriteriaConfig();
        collectionFilterCriteriaConfig.setPlaceholder("where-clause1");
        collectionFilterCriteriaConfig.setValue(" e.name = 'employee1' ");

        byNameFilterConfig.setFilterCriteria(collectionFilterCriteriaConfig);
        return byNameFilterConfig;
    }

    private SortOrder createByNameSortOrder() {
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("e.name", SortCriterion.Order.ASCENDING));
        return sortOrder;
    }

    private CollectionConfig createEmployeesCollectionConfig() {
        CollectionConfig result = new CollectionConfig();
        result.setName("Employees");
        result.setPrototype(EMLOYEES_PROROTYPE);
        result.setCountingPrototype(EMPLOYEES_COUNTING_PROTOTYPE);
        result.setIdField("id");
        result.getFilters().add(byDepartmentFilterConfig);

        return result;
    }

    private CollectionConfig createEmployeesComplexCollectionConfig() {
        CollectionConfig result = new CollectionConfig();
        result.setName("EmployeesComplex");
        result.setPrototype(EMPLOYEES_COMPLEX_PROTOTYPE);
        result.setCountingPrototype(EMPLOYEES_COUNTING_PROTOTYPE);
        result.getFilters().add(byDepartmentFilterConfig);
        result.setIdField("id");
        return result;
    }

    private CollectionConfig createPersonsCollectionConfig() {
        CollectionConfig result = new CollectionConfig();
        result.setName("Persons");
        result.setDomainObjectType("Person");
        result.setPrototype(PERSONS_PROROTYPE);
        result.setCountingPrototype(PERSONS_COUNTING_PROTOTYPE);
        result.setIdField("id");

        return result;
    }
}
