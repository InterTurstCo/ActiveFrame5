package ru.intertrust.cm.core.dao.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import ru.intertrust.cm.core.business.api.FilterForCache;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.config.CollectionQueryCacheConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.config.UniqueKeyFieldConfig;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterCriteriaConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterReferenceConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.CollectionQueryEntry;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.dao.impl.utils.CollectionRowMapper;

/**
 * 'employee'
 * @author vmatsukevich Date: 7/2/13 Time: 12:55 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class CollectionsDaoImplTest {

    private static final String COLLECTION_NOT_EQUALS_REFERENCE_RESULT = "SELECT s.\"id\", s.\"id_type\" FROM \"person\" s WHERE s.\"boss\" = :PARAM0 AND s.\"boss_type\" = :PARAM0_type AND (s.\"id\" <> :PARAM1 OR s.\"id_type\" <> :PARAM1_type)";

    private static final String COLLECTION_NOT_EQUALS_REFERENCE = "select s.id from Person s where s.boss = {0} and s.id !={1}";

    private static final String COLLECTION_ACL_QUERY = "EXISTS (SELECT r.\"object_id\" FROM \"employee_read\" AS r " +
            "INNER JOIN \"group_group\" AS gg ON r.\"group_id\" = gg.\"parent_group_id\" INNER JOIN \"group_member\" " +
            "AS gm ON gg.\"child_group_id\" = gm.\"usergroup\" WHERE gm.\"person_id\" = :user_id AND " +
            "r.\"object_id\" = \"id\") ";

    private static final String COLLECTION_COUNT_WITH_FILTERS =
            "SELECT count(*), 'employee' AS TEST_CONSTANT FROM employee AS e " +
                    "INNER JOIN department AS d ON e.department = d.id WHERE EXISTS " +
                    "(SELECT r.object_id FROM employee_READ AS r INNER JOIN group_member AS gm ON r.group_id = gm.usergroup " +
                    "WHERE gm.person_id = :user_id " +
                    "AND r.object_id = id) " +
                    "AND 1 = 1 AND d.name = 'dep1' AND e.name = 'employee1'";

    private static final String COLLECTION_QUERY_WITH_FILTER_AND_LIMITS =
            "SELECT e.id, e.name, e.position, e.created_date, e.updated_date, 'employee' AS TEST_CONSTANT " +
                    "FROM employee AS e INNER JOIN department AS d ON e.department = d.id WHERE " + COLLECTION_ACL_QUERY +
                    "AND 1 = 1 AND d.name = 'dep1' ORDER BY e.name LIMIT 100 OFFSET 10";

    private static final String COLLECTION_QUERY =
            "SELECT e.\"id\", e.email, e.login, e.password, e.created_date, e.updated_date, 'employee' AS TEST_CONSTANT " +
                    "FROM person e INNER JOIN department AS d ON e.department = d.id";

    private static final String ACTUAL_COLLECTION_QUERY_WITH_LIMITS =
            "WITH cur_user_groups AS (SELECT DISTINCT gg.\"parent_group_id\" FROM \"group_member\" gm " +
                    "INNER JOIN \"group_group\" gg ON gg.\"child_group_id\" = gm.\"usergroup\" " +
                    "WHERE gm.\"person_id\" = :user_id) " +
                    "SELECT e.\"id\", e.\"id_type\", e.\"email\", e.\"login\", e.\"password\", e.\"created_date\", " +
                    "e.\"updated_date\", 'employee' \"test_constant\" FROM (SELECT person.* FROM \"person\" person " +
                    "WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"person_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM \"cur_user_groups\") AND " +
                    "r.\"object_id\" = person.\"access_object_id\")) e INNER JOIN (SELECT department.* " +
                    "FROM \"department\" department WHERE 1 = 1 AND " +
                    "EXISTS (SELECT 1 FROM \"department_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM \"cur_user_groups\") AND " +
                    "r.\"object_id\" = department.\"access_object_id\")) AS d " +
                    "ON e.\"department\" = d.\"id\" LIMIT 100 OFFSET 10";

    private static final String FIND_COLLECTION_QUERY_WITH_FILTERS =
            "WITH cur_user_groups AS (SELECT DISTINCT gg.\"parent_group_id\" FROM \"group_member\" gm " +
                    "INNER JOIN \"group_group\" gg ON gg.\"child_group_id\" = gm.\"usergroup\" " +
                    "WHERE gm.\"person_id\" = :user_id) " +
                    "SELECT e.\"id\", e.\"id_type\", e.\"name\", e.\"position\", e.\"created_date\", e.\"updated_date\", " +
                    "'employee' \"test_constant\" FROM (SELECT employee.* FROM \"employee\" employee " +
                    "WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"employee_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM \"cur_user_groups\") AND " +
                    "r.\"object_id\" = employee.\"access_object_id\")) e " +
                    "INNER JOIN (SELECT department.* FROM \"department\" department " +
                    "WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"department_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM \"cur_user_groups\") AND " +
                    "r.\"object_id\" = department.\"access_object_id\")) d ON e.\"department\" = d.\"id\" " +
                    "WHERE 1 = 1 AND (d.\"name\" = 'dep1') ORDER BY e.\"name\" ASC";

    private static final String FIND_COLLECTION_QUERY_WITH_MULTIPLE_TYPE_REFERENCE =
            "WITH cur_user_groups AS (SELECT DISTINCT gg.\"parent_group_id\" FROM \"group_member\" gm " +
                    "INNER JOIN \"group_group\" gg ON gg.\"child_group_id\" = gm.\"usergroup\" " +
                    "WHERE gm.\"person_id\" = :user_id) " +
                    "SELECT p.\"id\", p.\"id_type\", p.\"login\", p.\"password\", coalesce(p.\"boss1\", p.\"boss2\") \"boss\", " +
                    "p.\"created_date\", p.\"updated_date\", 'person' \"test_constant\" FROM " +
                    "(SELECT person.* FROM \"person\" person WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"person_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM \"cur_user_groups\") AND " +
                    "r.\"object_id\" = person.\"access_object_id\")) p WHERE 1 = 1";

    private static final String FIND_COMPLEX_COLLECTION_QUERY_WITH_FILTERS =
            "SELECT e.id, e.name, e.position, e.created_date, e.updated_date, 'employee' AS TEST_CONSTANT" +
                    " FROM employee AS e " +
                    "INNER JOIN department AS d ON e.department = d.id " +
                    "INNER JOIN authentication_info AS a ON e.login = a.id WHERE " +
                    COLLECTION_ACL_QUERY +
                    "AND 1 = 1 AND d.name = 'dep1' AND e.name = 'employee1' AND a.id = 1 ORDER BY e.name";

    private static final String COLLECTION_QUERY_WITHOUT_FILTERS =
            "SELECT e.id, e.name, e.position, e.created_date, e.updated_date, 'employee' AS TEST_CONSTANT " +
                    "FROM employee AS e WHERE " + COLLECTION_ACL_QUERY +
                    "AND 1 = 1 ORDER BY e.name";

    private static final String COLLECTION_QUERY_WITHOUT_SORT_ORDER =
            "SELECT e.id, e.name, e.position, e.created_date, e.updated_date, 'employee' AS TEST_CONSTANT FROM employee AS e WHERE "
                    + COLLECTION_ACL_QUERY +
                    "AND 1 = 1";

    private static final String EMLOYEES_PROROTYPE = "select\n" +
            "                    e.id, e.name, e.position, e.created_date, e.updated_date, 'employee' AS TEST_CONSTANT\n" +
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
            "                    p.id, p.login, p.password, coalesce(p.BOSS1, p.BOSS2) AS BOSS, p.created_date, " +
            "                    p.updated_date, 'person' AS TEST_CONSTANT\n" +
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
    private CurrentUserAccessor currentUserAccessor;

    @Mock
    private UserGroupGlobalCache userGroupCache;

    @Mock
    private NamedParameterJdbcOperations jdbcTemplate;

    @Mock
    private GlobalCacheClient globalCacheClient;

    private DomainObjectQueryHelper domainObjectQueryHelper = new DomainObjectQueryHelper();

    private ConfigurationExplorerImpl configurationExplorer;
    CollectionQueryCacheImpl collectionQueryCache = new CollectionQueryCacheImpl();

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
        initConfigurationExplorer();
        when(userGroupCache.isAdministrator(any(Id.class))).thenReturn(false);
        collectionsDaoImpl.setCollectionQueryCache(collectionQueryCache);

        domainObjectQueryHelper.setUserGroupCache(userGroupCache);
        domainObjectQueryHelper.setCurrentUserAccessor(currentUserAccessor);
        domainObjectQueryHelper.setConfigurationExplorer(configurationExplorer);
        collectionsDaoImpl.setDomainObjectQueryHelper(domainObjectQueryHelper);
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
        boss.setType("Internal_Employee");
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

        DomainObjectTypeConfig employee = new DomainObjectTypeConfig();
        employee.setName("employee");

        DomainObjectTypeConfig departmnet = new DomainObjectTypeConfig();
        departmnet.setName("department");

        Configuration configuration = new Configuration();
        configuration.getConfigurationList().add(doTypeConfig);
        configuration.getConfigurationList().add(internalEmployee);
        configuration.getConfigurationList().add(externalEmployee);
        configuration.getConfigurationList().add(employee);
        configuration.getConfigurationList().add(departmnet);

        CollectionConfig childrenCollectionConfig = new CollectionConfig();
        childrenCollectionConfig.setPrototype("select name from child where 1 = 1 ::where-clause");
        childrenCollectionConfig.setName("children");
        CollectionFilterConfig parentFilterConfig = new CollectionFilterConfig();
        parentFilterConfig.setName("byParent");
        CollectionFilterCriteriaConfig parentFilterCriteriaConfig = new CollectionFilterCriteriaConfig();
        parentFilterCriteriaConfig.setPlaceholder("where-clause");
        parentFilterCriteriaConfig.setValue("parent = {0}");
        parentFilterConfig.setFilterCriteria(parentFilterCriteriaConfig);
        childrenCollectionConfig.setFilters(singletonList(parentFilterConfig));
        configuration.getConfigurationList().add(childrenCollectionConfig);

        collectionConfig = createEmployeesCollectionConfig();
        configuration.getConfigurationList().add(collectionConfig);

        complexCollectionConfig = createEmployeesComplexCollectionConfig();
        configuration.getConfigurationList().add(complexCollectionConfig);

        personsCollectionConfig = createPersonsCollectionConfig();
        configuration.getConfigurationList().add(personsCollectionConfig);

        byDepartmentFilterConfig = createByDepartmentFilterConfig();
        collectionConfig.getFilters().add(byDepartmentFilterConfig);
        complexCollectionConfig.getFilters().add(byDepartmentFilterConfig);

        byDepartmentComplexFilterConfig = createByDepartmentComplexFilterConfig();
        complexCollectionConfig.getFilters().add(byDepartmentComplexFilterConfig);

        byNameFilterConfig = createByNameFilterConfig();
        collectionConfig.getFilters().add(byNameFilterConfig);
        complexCollectionConfig.getFilters().add(byNameFilterConfig);

        byNameComplexFilterConfig = createByNameComplexFilterConfig();
        collectionConfig.getFilters().add(byNameComplexFilterConfig);
        complexCollectionConfig.getFilters().add(byNameComplexFilterConfig);

        byAuthenticationInfoFilterConfig = createByAuthenticationInfoFilterConfig();
        collectionConfig.getFilters().add(byAuthenticationInfoFilterConfig);
        complexCollectionConfig.getFilters().add(byAuthenticationInfoFilterConfig);

        sortOrder = createByNameSortOrder();

        GlobalSettingsConfig globalSettingsConfig = new GlobalSettingsConfig();
        CollectionQueryCacheConfig collectionQueryCacheConfig = new CollectionQueryCacheConfig();
        collectionQueryCacheConfig.setMaxSize(1000);
        globalSettingsConfig.setCollectionQueryCacheConfig(collectionQueryCacheConfig);

        configuration.getConfigurationList().add(globalSettingsConfig);

        configurationExplorer = new ConfigurationExplorerImpl(configuration);
        collectionsDaoImpl.setConfigurationExplorer(configurationExplorer);
        collectionQueryCache.setConfigurationExplorer(configurationExplorer);

    }

    private AccessToken createMockAccessToken() {
        AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.isDeferred()).thenReturn(true);

        UserSubject subject = mock(UserSubject.class);
        when(subject.getUserId()).thenReturn(1);
        when(accessToken.getSubject()).thenReturn(subject);
        return accessToken;
    }

    private AccessToken createMockSystemAccessToken() {
        AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.isDeferred()).thenReturn(false);

        UserSubject subject = mock(UserSubject.class);
        when(subject.getUserId()).thenReturn(1);
        when(accessToken.getSubject()).thenReturn(subject);
        return accessToken;
    }

    @Test
    public void testFindCollectionWithFilters() throws Exception {
        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        AccessToken accessToken = createMockAccessToken();

        collectionsDaoImpl.findCollection("Employees", singletonList(filter), sortOrder, 0, 0, accessToken);

        verify(jdbcTemplate).query(eq(FIND_COLLECTION_QUERY_WITH_FILTERS),
                anyMapOf(String.class, Object.class), any(CollectionRowMapper.class));
    }

    @Test
    public void testGetCollectionQueryFromCache() throws Exception {
        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        AccessToken accessToken = createMockAccessToken();
        filter.addCriterion(0, new ReferenceValue(new RdbmsId(1, 2)));

        CollectionQueryEntry collectionQueryEntry =
                collectionQueryCache.getCollectionQuery("Employees", filtersForCache(singletonList(filter)), null, sortOrder, 2, 0, accessToken);
        assertNull(collectionQueryEntry);

        collectionsDaoImpl.findCollection("Employees", singletonList(filter), sortOrder, 2, 0, accessToken);

        // значения фильтра в ключе кеша не должны использоваться
        filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new ReferenceValue(new RdbmsId(2, 2)));

        collectionQueryEntry = collectionQueryCache.getCollectionQuery("Employees", filtersForCache(singletonList(filter)), null, sortOrder, 2, 0, accessToken);
        assertNotNull(collectionQueryEntry);

        // значения фильтра в ключе кеша не должны использоваться
        filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new ReferenceValue(new RdbmsId(2, 2)));
        filter.addCriterion(1, new ReferenceValue(new RdbmsId(2, 2)));

        collectionsDaoImpl.findCollection("Employees", singletonList(filter), sortOrder, 2, 0, accessToken);

        collectionQueryEntry = collectionQueryCache.getCollectionQuery("Employees", filtersForCache(singletonList(filter)), null, sortOrder, 2, 0, accessToken);
        assertNotNull(collectionQueryEntry);

        // очередность параметров в фильтре не должна влиять на кеширование
        // запроса
        filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(1, new ReferenceValue(new RdbmsId(2, 2)));
        filter.addCriterion(0, new ReferenceValue(new RdbmsId(2, 2)));

        collectionQueryEntry = collectionQueryCache.getCollectionQuery("Employees", filtersForCache(singletonList(filter)), null, sortOrder, 2, 0, accessToken);
        assertNotNull(collectionQueryEntry);

        filter = new Filter();
        filter.setFilter("byDepartment1");
        filter.addCriterion(1, new ReferenceValue(new RdbmsId(2, 2)));
        collectionQueryEntry = collectionQueryCache.getCollectionQuery("Employees", filtersForCache(singletonList(filter)), null, sortOrder, 2, 0, accessToken);
        assertNull(collectionQueryEntry);

    }

    @Test
    public void testGetQueryFromCache() throws Exception {
        String collectionQuery = "Select * from country where id in ({0})";
        AccessToken accessToken = createMockAccessToken();

        List<Value<?>> referenceValues =
                Arrays.<Value<?>> asList(new ReferenceValue(new RdbmsId(1, 1)), new ReferenceValue(new RdbmsId(1, 2)));
        ListValue listValue = ListValue.createListValue(referenceValues);

        List<Value> params = new ArrayList<>();
        params.add(listValue);

        Set<ListValue> listParams = new HashSet<>();
        listParams.add(listValue);

        CollectionQueryEntry collectionQueryEntry =
                collectionQueryCache.getCollectionQuery(collectionQuery, 0, 0, null, accessToken);
        assertNull(collectionQueryEntry);

        collectionsDaoImpl.findCollectionByQuery(collectionQuery, params, 0, 0, accessToken);

        collectionQueryEntry =
                collectionQueryCache.getCollectionQuery(collectionQuery, 0, 0, null, accessToken);
        assertNotNull(collectionQueryEntry);

        // другие offset и limit
        collectionQueryEntry =
                collectionQueryCache.getCollectionQuery(collectionQuery, 2, 2, null, accessToken);
        assertNull(collectionQueryEntry);

        collectionsDaoImpl.findCollectionByQuery(collectionQuery, params, 2, 2, accessToken);

        collectionQueryEntry =
                collectionQueryCache.getCollectionQuery(collectionQuery, 2, 2, null, accessToken);
        assertNotNull(collectionQueryEntry);

        // запрос без параметров
        collectionQuery = "Select * from country where id = 1";
        collectionsDaoImpl.findCollectionByQuery(collectionQuery, 4, 2, accessToken);

        collectionQueryEntry =
                collectionQueryCache.getCollectionQuery(collectionQuery, 4, 2, null, accessToken);
        assertNotNull(collectionQueryEntry);
    }

    @Test
    public void testFindCollectionWithMultipleReferenceTypes() throws Exception {
        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        AccessToken accessToken = createMockAccessToken();

        collectionsDaoImpl.findCollection("Persons", singletonList(filter), new SortOrder(), 0, 0, accessToken);

        verify(jdbcTemplate).query(eq(FIND_COLLECTION_QUERY_WITH_MULTIPLE_TYPE_REFERENCE),
                anyMapOf(String.class, Object.class), any(CollectionRowMapper.class));
    }

    @Test
    public void testFindCollectionWithReferenceParams() throws Exception {

        Filter f = new Filter();
        f.setFilter("byParent");
        f.addReferenceCriterion(0, new RdbmsId(1, 1));

        collectionsDaoImpl
                .findCollection("children", singletonList(f), new SortOrder(), 0, 0, createMockSystemAccessToken());

        HashMap<String, Object> expected = new HashMap<>();
        expected.put("byParent_0", 1L);
        expected.put("byParent_0_type", 1L);
        verify(jdbcTemplate).query(eq("SELECT \"name\" FROM \"child\" WHERE 1 = 1 AND (\"parent\" = :byParent_0 AND \"parent_type\" = :byParent_0_type)"),
                eq(expected), any(CollectionRowMapper.class));
    }

    @Test
    public void testFindCollectionWithListParams() throws Exception {

        Filter f = new Filter();
        f.setFilter("byParent");
        f.addCriterion(0, ListValue.createListValue(asList(new ReferenceValue(new RdbmsId(1, 1)))));

        collectionsDaoImpl
                .findCollection("children", singletonList(f), new SortOrder(), 0, 0, createMockSystemAccessToken());

        HashMap<String, Object> expected = new HashMap<>();
        expected.put("byParent_0_0", asList(1L));
        expected.put("byParent_0_0_type", 1L);
        verify(jdbcTemplate).query(eq("SELECT \"name\" FROM \"child\" WHERE 1 = 1 AND (\"parent\" = :byParent_0 AND \"parent_type\" = :byParent_0_type)"),
                eq(expected), any(CollectionRowMapper.class));
    }

    @Test
    public void testFindCollectionWithMultipleCriterionWithReferenceParams() throws Exception {

        Filter f = new Filter();
        f.setFilter("byParent");
        f.addMultiReferenceCriterion(0, asList((Id) new RdbmsId(1, 1)));

        collectionsDaoImpl
                .findCollection("children", singletonList(f), new SortOrder(), 0, 0, createMockSystemAccessToken());

        HashMap<String, Object> expected = new HashMap<>();
        expected.put("byParent_0_0", asList(1L));
        expected.put("byParent_0_0_type", 1L);
        verify(jdbcTemplate).query(eq("SELECT \"name\" FROM \"child\" WHERE 1 = 1 AND (\"parent\" = :byParent_0 AND \"parent_type\" = :byParent_0_type)"),
                eq(expected), any(CollectionRowMapper.class));
    }

    // @Test
    public void testFindComplexCollectionWithFilters() throws Exception {
        List<Filter> filterValues = new ArrayList<>();

        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        filterValues.add(filter);

        filter = new Filter();
        filter.setFilter("byName");
        filterValues.add(filter);

        filter = new Filter();
        filter.setFilter("byAuthenticationInfo");
        filterValues.add(filter);

        AccessToken accessToken = createMockAccessToken();
        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(complexCollectionConfig, filterValues, sortOrder, 0, 0, accessToken);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(FIND_COMPLEX_COLLECTION_QUERY_WITH_FILTERS, refinedActualQuery);
    }

    // @Test
    public void testFindCollectionWithoutFilters() throws Exception {
        AccessToken accessToken = createMockAccessToken();

        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(collectionConfig, new ArrayList<Filter>(), sortOrder, 0, 0, accessToken);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(COLLECTION_QUERY_WITHOUT_FILTERS, refinedActualQuery);
    }

    // @Test
    public void testFindCollectionWithoutSortOrder() throws Exception {
        AccessToken accessToken = createMockAccessToken();

        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(collectionConfig, new ArrayList<Filter>(), null, 0, 0, accessToken);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(COLLECTION_QUERY_WITHOUT_SORT_ORDER, refinedActualQuery);
    }

    // @Test
    public void testFindCollectionWithLimits() throws Exception {
        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        AccessToken accessToken = createMockAccessToken();

        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(collectionConfig, singletonList(filter),
                sortOrder, 10, 100, accessToken);
        String refinedActualQuery = refineQuery(actualQuery);

        assertEquals(COLLECTION_QUERY_WITH_FILTER_AND_LIMITS, refinedActualQuery);
    }

    @Test
    public void testFindCollectionByQueryWithLimits() throws Exception {
        AccessToken accessToken = createMockAccessToken();
        collectionsDaoImpl.findCollectionByQuery(COLLECTION_QUERY, 10, 100, accessToken);

        verify(jdbcTemplate).query(eq(ACTUAL_COLLECTION_QUERY_WITH_LIMITS),
                anyMapOf(String.class, Object.class), any(CollectionRowMapper.class));

    }

    @Test
    public void testFindCollectionByQueryWithReferenceParams() throws Exception {
        AccessToken accessToken = createMockSystemAccessToken();

        List<Value> referenceValues =
                Arrays.<Value> asList(new ReferenceValue(new RdbmsId(1, 1)), new ReferenceValue(new RdbmsId(1, 2)));

        List<Value> params = new ArrayList<>();
        params.addAll(referenceValues);

        HashMap<String, Object> expected = new HashMap<>();
        expected.put("PARAM0", 1L);
        expected.put("PARAM0_type", 1L);
        expected.put("PARAM1", 2L);
        expected.put("PARAM1_type", 1L);
        collectionsDaoImpl.findCollectionByQuery(COLLECTION_NOT_EQUALS_REFERENCE, params, 0, 0, accessToken);

        verify(jdbcTemplate).query(eq(COLLECTION_NOT_EQUALS_REFERENCE_RESULT),
                eq(expected), any(CollectionRowMapper.class));

    }

    // @Test
    public void testFindCollectionCountWithFilters() throws Exception {
        List<Filter> filterValues = new ArrayList<>();

        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        filterValues.add(filter);

        filter = new Filter();
        filter.setFilter("byName");
        filterValues.add(filter);

        AccessToken accessToken = createMockAccessToken();
        String actualQuery = collectionsDaoImpl.getFindCollectionCountQuery(collectionConfig, filterValues, accessToken);
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

        return result;
    }

    private CollectionConfig createEmployeesComplexCollectionConfig() {
        CollectionConfig result = new CollectionConfig();
        result.setName("EmployeesComplex");
        result.setPrototype(EMPLOYEES_COMPLEX_PROTOTYPE);
        result.setCountingPrototype(EMPLOYEES_COUNTING_PROTOTYPE);
        result.setIdField("id");
        return result;
    }

    private CollectionConfig createPersonsCollectionConfig() {
        CollectionConfig result = new CollectionConfig();
        result.setName("Persons");
        result.setPrototype(PERSONS_PROROTYPE);
        result.setCountingPrototype(PERSONS_COUNTING_PROTOTYPE);
        result.setIdField("id");

        return result;
    }

    private Set<FilterForCache> filtersForCache(List<? extends Filter> filterValues) {
        HashSet<FilterForCache> filtersForCache = new HashSet<>();
        for (Filter f : filterValues) {
            filtersForCache.add(new FilterForCache(f));
        }
        return filtersForCache;
    }
}
