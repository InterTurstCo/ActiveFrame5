package ru.intertrust.cm.core.dao.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import ru.intertrust.cm.core.business.api.FilterForCache;
import ru.intertrust.cm.core.business.api.QueryModifierPrompt;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IdsIncludedFilter;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.config.CollectionPlaceholderConfig;
import ru.intertrust.cm.core.config.CollectionQueryCacheConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.config.UniqueKeyFieldConfig;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterCriteriaConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterReferenceConfig;
import ru.intertrust.cm.core.config.base.CollectionGeneratorConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.CollectionQueryEntry;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.dao.api.GlobalCacheManager;
import ru.intertrust.cm.core.dao.api.ServerComponentService;
import ru.intertrust.cm.core.dao.api.component.CollectionDataGenerator;
import ru.intertrust.cm.core.dao.impl.parameters.ParametersConverter;
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

    private static final String FIND_COLLECTION_QUERY_WITH =
            "WITH cur_user_groups AS (SELECT DISTINCT gg.\"parent_group_id\" FROM \"group_member\" gm INNER JOIN \"group_group\" gg ON gg.\"child_group_id\" = gm.\"usergroup\" WHERE gm.\"person_id\" = :user_id), person_data AS (SELECT \"id\", \"id_type\", \"login\" FROM (SELECT person.* FROM \"person\" person WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"person_read\" r WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM \"cur_user_groups\") AND r.\"object_id\" = person.\"access_object_id\")) person), emp_data AS (SELECT em.\"id\" \"emid\", em.\"id_type\" \"emid_type\", pData.\"id\" \"pid\", em.\"name\", em.\"depid\" FROM \"person_data\" AS pData LEFT JOIN (SELECT employee.* FROM \"employee\" employee WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"employee_read\" r WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM \"cur_user_groups\") AND r.\"object_id\" = employee.\"access_object_id\")) em ON (pData.\"id\" = em.\"personid\")) SELECT \"id\", \"id_type\", ppData.\"login\", dep.\"name\" FROM \"person_data\" ppData LEFT JOIN \"emp_data\" emData ON (ppData.\"id\" = emData.\"pid\") LEFT JOIN (SELECT department.* FROM \"department\" department WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"department_read\" r WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM \"cur_user_groups\") AND r.\"object_id\" = department.\"access_object_id\")) depp ON (depp.\"id\" = emData.\"depid\") WHERE 1 = 1 AND (\"login\" = :byName0)";

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

    @Mock
    private GlobalCacheManager globalCacheManager;

    @Mock
    private ServerComponentService serverComponentService;

    private DomainObjectQueryHelper domainObjectQueryHelper = new DomainObjectQueryHelper();

    private ConfigurationExplorerImpl configurationExplorer;
    CollectionQueryCacheImpl collectionQueryCache = new CollectionQueryCacheImpl();

    private CollectionConfig collectionConfig;
    private CollectionConfig complexCollectionConfig;
    private CollectionConfig personsCollectionConfig;

    @Before
    public void setUp() throws Exception {
        collectionQueryCache.clearCollectionQueryCache();
        initConfigurationExplorer();
        when(serverComponentService.getServerComponent("generator")).thenReturn(new CollectionDataGenerator() {

            @Override
            public int findCollectionCount(List<? extends Filter> filterValues) {
                ReferenceValue v = (ReferenceValue) (filterValues.get(0).getCriterion(0));
                assert (v != null);
                return 0;
            }

            @Override
            public IdentifiableObjectCollection findCollection(List<? extends Filter> filters, SortOrder sortOrder, int offset, int limit) {
                ReferenceValue v = (ReferenceValue) (filters.get(0).getCriterion(0));
                assert (v != null);
                return null;
            }
        });
        when(userGroupCache.isAdministrator(any(Id.class))).thenReturn(false);
        collectionsDaoImpl.setCollectionQueryCache(collectionQueryCache);

        domainObjectQueryHelper.setUserGroupCache(userGroupCache);
        domainObjectQueryHelper.setCurrentUserAccessor(currentUserAccessor);
        domainObjectQueryHelper.setConfigurationExplorer(configurationExplorer);
        collectionsDaoImpl.setDomainObjectQueryHelper(domainObjectQueryHelper);
    }

    private void initConfigurationExplorer() {

        Configuration configuration = new Configuration();

        configuration.getConfigurationList().addAll(asList(
                typeConfig("Person", asList(
                        stringFieldConfig("EMail", 128, false)
                        , stringFieldConfig("Login", 64, true)
                        , stringFieldConfig("Password", 128, false)
                        , referenceConfig("Boss", "Internal_Employee")
                        ), asList(uniqueKey("EMail")))
                , typeConfig("Internal_Employee")
                , typeConfig("External_Employee")
                , typeConfig("employee")
                , typeConfig("department")
                ));

        collectionConfig = createCollectionConfig("Employees", EMLOYEES_PROROTYPE, EMPLOYEES_COUNTING_PROTOTYPE,
                filterConfig("byDepartment", from("inner join department d on e.department = d.id", "from-clause"),
                        where(" d.name = 'dep1'")),
                filterConfig("byName", null, where(" e.name = 'employee1' ")),
                filterConfig("byAuthenticationInfo", from("inner join authentication_info a on e.login = a.id", "from-clause2"),
                        where(" a.id = 1 ", "where-clause2"))
                );

        complexCollectionConfig = createCollectionConfig("EmployeesComplex", EMPLOYEES_COMPLEX_PROTOTYPE, EMPLOYEES_COUNTING_PROTOTYPE,
                filterConfig("byDepartment", from("inner join department d on e.department = d.id", "from-clause"),
                        where(" d.name = 'dep1'")),
                filterConfig("byDepartment", from("inner join department d on e.department = d.id", "from-clause1"),
                        where(" d.name = 'dep1'", "where-clause1")),
                filterConfig("byName", null, where(" e.name = 'employee1' ")),
                filterConfig("byName", null, where(" e.name = 'employee1' ", "where-clause1")),
                filterConfig("byAuthenticationInfo", from("inner join authentication_info a on e.login = a.id", "from-clause2"),
                        where(" a.id = 1 ", "where-clause2"))
                );

        personsCollectionConfig = createCollectionConfig("Persons", PERSONS_PROROTYPE, PERSONS_COUNTING_PROTOTYPE);
        
        configuration
                .getConfigurationList()
                .addAll(asList(
                        createCollectionConfig(
                                "children",
                                "select name from child where 1 = 1 ::where-clause",
                                null,
                                filterConfig("byParent", null, where("parent = {0}"))
                        ),
                        createCollectionConfig(
                                "generated",
                                "generator"
                        ),
                        createCollectionConfig(
                                "persons_simple",
                                "select name from person where 1 = 1 ::where-clause",
                                null
                        ),

                        createCollectionConfig(
                                "persons_with",
                                "with person_data as ( select id, Login from person), emp_data as (select em.id as emId, pData.id as pId, em.name, em.depId from person_data as pData left join employee em on (pData.id = em.personId) ) select ppData.id, ppData.Login, dep.name from person_data ppData left join emp_data emData on (ppData.id = emData.pId) left join department depp on (depp.id = emData.depId)  where 1=1 ::where-clause",
                                null,
                                filterConfig("byName", null, where("Login = {0}"))
                        ),

                        createCollectionConfig("PersonsWithGlobalPlaceholder", 
                                "select id, ::modify_date_placeholder, ::create_date_placeholder from person ::not_configured_placeholder", null),

                        collectionConfig,
                        complexCollectionConfig,
                        personsCollectionConfig,
                        createCollectionPlaceholderConfig("modify_date_placeholder", "to_char(updated_date, 'DD.MM.YYYY') as modify_date"),
                        createCollectionPlaceholderConfig("create_date_placeholder", "to_char(created_date, 'DD.MM.YYYY') as create_date")
                        ));

        GlobalSettingsConfig globalSettingsConfig = new GlobalSettingsConfig();
        CollectionQueryCacheConfig collectionQueryCacheConfig = new CollectionQueryCacheConfig();
        collectionQueryCacheConfig.setMaxSize(1000);
        globalSettingsConfig.setCollectionQueryCacheConfig(collectionQueryCacheConfig);

        configuration.getConfigurationList().add(globalSettingsConfig);

        configurationExplorer = new ConfigurationExplorerImpl(configuration);
        collectionsDaoImpl.setConfigurationExplorer(configurationExplorer);
        collectionQueryCache.setConfigurationExplorer(configurationExplorer);

    }

    private CollectionPlaceholderConfig createCollectionPlaceholderConfig(String name, String value) {
        CollectionPlaceholderConfig result = new CollectionPlaceholderConfig();
        result.setName(name);
        result.setBody(value);
        return result;
    }

    private CollectionConfig createCollectionConfig(String name, String generator) {
        CollectionConfig result = new CollectionConfig();
        result.setName(name);
        CollectionGeneratorConfig gen = new CollectionGeneratorConfig();
        gen.setClassName(generator);
        result.setGenerator(gen);
        result.setIdField("id");
        return result;
    }

    private DomainObjectTypeConfig typeConfig(String name, List<FieldConfig> fieldConfigs, List<UniqueKeyConfig> uniqueKeys) {
        DomainObjectTypeConfig c = typeConfig(name);
        c.getFieldConfigs().addAll(fieldConfigs);
        c.getUniqueKeyConfigs().addAll(uniqueKeys);
        return c;
    }

    private ReferenceFieldConfig referenceConfig(String name, String type) {
        ReferenceFieldConfig c = new ReferenceFieldConfig();
        c.setName(name);
        c.setType(type);
        return c;
    }

    private CollectionFilterConfig filterConfig(String name, CollectionFilterReferenceConfig from, CollectionFilterCriteriaConfig where) {
        CollectionFilterConfig filterConfig = new CollectionFilterConfig();
        filterConfig.setName(name);
        filterConfig.setFilterReference(from);
        filterConfig.setFilterCriteria(where);
        return filterConfig;
    }

    private UniqueKeyConfig uniqueKey(String... fields) {
        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        for (String field : fields) {
            UniqueKeyFieldConfig uniqueField = new UniqueKeyFieldConfig();
            uniqueField.setName(field);
            uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueField);
        }
        return uniqueKeyConfig;
    }

    private DomainObjectTypeConfig typeConfig(String name) {
        DomainObjectTypeConfig internalEmployee = new DomainObjectTypeConfig();
        internalEmployee.setName(name);
        return internalEmployee;
    }

    private StringFieldConfig stringFieldConfig(String name, int length, boolean isNotNull) {
        StringFieldConfig config = new StringFieldConfig();
        config.setName(name);
        config.setLength(length);
        config.setNotNull(isNotNull);
        return config;
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

        collectionsDaoImpl.findCollection("Employees", singletonList(filter), createByNameSortOrder(), 0, 0, accessToken);

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
                collectionQueryCache.getCollectionQuery("Employees", filtersForCache(singletonList(filter)), null, createByNameSortOrder(), 2, 0, accessToken);
        assertNull(collectionQueryEntry);

        collectionsDaoImpl.findCollection("Employees", singletonList(filter), createByNameSortOrder(), 2, 0, accessToken);

        // значения фильтра в ключе кеша не должны использоваться
        filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new ReferenceValue(new RdbmsId(2, 2)));

        ParametersConverter converter = new ParametersConverter();
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValuesInFilters(singletonList(filter));

        collectionQueryEntry = collectionQueryCache.getCollectionQuery("Employees", filtersForCache(singletonList(filter)), pair.getSecond(),
                createByNameSortOrder(), 2,
                0, accessToken);
        assertNotNull(collectionQueryEntry);

        // значения фильтра в ключе кеша не должны использоваться
        filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new ReferenceValue(new RdbmsId(2, 2)));
        filter.addCriterion(1, new ReferenceValue(new RdbmsId(2, 2)));

        collectionsDaoImpl.findCollection("Employees", singletonList(filter), createByNameSortOrder(), 2, 0, accessToken);

        pair = converter.convertReferenceValuesInFilters(singletonList(filter));
        collectionQueryEntry = collectionQueryCache.getCollectionQuery("Employees", filtersForCache(singletonList(filter)), pair.getSecond(),
                createByNameSortOrder(), 2,
                0, accessToken);
        assertNotNull(collectionQueryEntry);

        // очередность параметров в фильтре не должна влиять на кеширование
        // запроса
        filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(1, new ReferenceValue(new RdbmsId(2, 2)));
        filter.addCriterion(0, new ReferenceValue(new RdbmsId(2, 2)));

        pair = converter.convertReferenceValuesInFilters(singletonList(filter));
        collectionQueryEntry = collectionQueryCache.getCollectionQuery("Employees", filtersForCache(singletonList(filter)), pair.getSecond(),
                createByNameSortOrder(), 2,
                0, accessToken);
        assertNotNull(collectionQueryEntry);

        filter = new Filter();
        filter.setFilter("byDepartment1");
        filter.addCriterion(1, new ReferenceValue(new RdbmsId(2, 2)));
        pair = converter.convertReferenceValuesInFilters(singletonList(filter));
        collectionQueryEntry = collectionQueryCache.getCollectionQuery("Employees", filtersForCache(singletonList(filter)), pair.getSecond(),
                createByNameSortOrder(), 2,
                0, accessToken);
        assertNull(collectionQueryEntry);

    }

    @Test
    public void testCollectionQueryCacheListValue() {
        AccessToken accessToken = createMockAccessToken();
        String collectionQuery = "Select * from country where id in ({0})";
        List<Value<?>> referenceValues =
                Arrays.<Value<?>> asList(new ReferenceValue(new RdbmsId(1, 1)), new ReferenceValue(new RdbmsId(1, 2)));
        ListValue listValue = ListValue.createListValue(referenceValues);
        collectionsDaoImpl.findCollectionByQuery(collectionQuery, asList(listValue), 2, 2, accessToken);
        assertNull(collectionQueryCache.getCollectionQuery(collectionQuery, 2, 2, null, accessToken));
        assertEquals("SELECT * FROM \"country\" WHERE (\"id\" IN (:PARAM0_0) AND \"id_type\" = :PARAM0_0_type) LIMIT 2 OFFSET 2", collectionQueryCache
                .getCollectionQuery(collectionQuery, 2, 2, getPrompt(asList(listValue)), accessToken)
                .getQuery());
    }

    @Test
    public void testGetQueryFromCache() throws Exception {
        String collectionQuery = "Select * from country where id in ({0})";
        AccessToken accessToken = createMockAccessToken();

        List<Value<?>> referenceValues =
                Arrays.<Value<?>> asList(new ReferenceValue(new RdbmsId(1, 1)), new ReferenceValue(new RdbmsId(1, 2)));

        List<? extends Value<?>> params = asList(ListValue.createListValue(referenceValues));

        CollectionQueryEntry collectionQueryEntry =
                collectionQueryCache.getCollectionQuery(collectionQuery, 0, 0, getPrompt(params), accessToken);
        assertNull(collectionQueryEntry);

        collectionsDaoImpl.findCollectionByQuery(collectionQuery, params, 0, 0, accessToken);

        collectionQueryEntry =
                collectionQueryCache.getCollectionQuery(collectionQuery, 0, 0, getPrompt(params), accessToken);
        assertNotNull(collectionQueryEntry);

        // другие offset и limit
        collectionQueryEntry =
                collectionQueryCache.getCollectionQuery(collectionQuery, 2, 2, getPrompt(params), accessToken);
        assertNull(collectionQueryEntry);

        collectionsDaoImpl.findCollectionByQuery(collectionQuery, params, 2, 2, accessToken);

        collectionQueryEntry =
                collectionQueryCache.getCollectionQuery(collectionQuery, 2, 2, getPrompt(params), accessToken);
        assertNotNull(collectionQueryEntry);

        // запрос без параметров
        collectionQuery = "Select * from country where id = 1";
        collectionsDaoImpl.findCollectionByQuery(collectionQuery, 4, 2, accessToken);

        collectionQueryEntry =
                collectionQueryCache.getCollectionQuery(collectionQuery, 4, 2, null, accessToken);
        assertNotNull(collectionQueryEntry);
    }

    private QueryModifierPrompt getPrompt(List<? extends Value<?>> params) {
        ParametersConverter converter = new ParametersConverter();
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValues(params);
        return pair.getSecond();
    }

    @Test
    public void testCollectionWITH() throws Exception {

        Filter filter = new Filter();
        filter.setFilter("byName");
        AccessToken accessToken = createMockAccessToken();

        collectionsDaoImpl.findCollection("persons_with", singletonList(filter), new SortOrder(), 0, 0, accessToken);

        verify(jdbcTemplate).query(eq(FIND_COLLECTION_QUERY_WITH),
                anyMapOf(String.class, Object.class), any(CollectionRowMapper.class));
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
        expected.put("byParent_0_0", asList(1L));
        expected.put("byParent_0_0_type", 1L);
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
        expected.put("byParent_0", 1L);
        expected.put("byParent_0_type", 1L);
        verify(jdbcTemplate).query(eq("SELECT \"name\" FROM \"child\" WHERE 1 = 1 AND (\"parent\" = :byParent_0 AND \"parent_type\" = :byParent_0_type)"),
                eq(expected), any(CollectionRowMapper.class));
    }

    @Test
    public void testIdsBasedFilters() {
        Filter f = new IdsIncludedFilter();
        f.setFilter("ids");

        @SuppressWarnings("serial")
        ArrayList<Long> t = new ArrayList<Long>() {
            @Override
            public boolean equals(Object o) {
                if (o instanceof List) {
                    List<?> t = (List<?>) o;
                    return t.size() == this.size() && this.containsAll(t);
                }
                return false;
            }
        };

        for (int i = 0; i < 10; i++) {
            f.addReferenceCriterion(i, new RdbmsId(1, i));
            t.add((long) i);
        }
        HashMap<String, Object> expected = new HashMap<>();
        expected.put("idsIncluded0_0_0", t);
        expected.put("idsIncluded0_0_0_type", 1L);
        collectionsDaoImpl
                .findCollection("children", singletonList(f), new SortOrder(), 0, 0, createMockSystemAccessToken());
        verify(jdbcTemplate).query(eq("SELECT \"name\" FROM \"child\" WHERE 1 = 1 "
                + "AND (child.\"id\" IN (:idsIncluded0_0_0) AND child.\"id_type\" = :idsIncluded0_0_0_type)"),
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
        expected.put("byParent_0", 1L);
        expected.put("byParent_0_type", 1L);
        verify(jdbcTemplate).query(eq("SELECT \"name\" FROM \"child\" WHERE 1 = 1 AND (\"parent\" = :byParent_0 AND \"parent_type\" = :byParent_0_type)"),
                eq(expected), any(CollectionRowMapper.class));
    }

    @Test
    public void testGeneratedCountQuery() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Collections.<String, Object> singletonMap("user_id", 1)), eq(Integer.class))).thenReturn(0);
        when(globalCacheClient.getCollectionCount(anyString(), anyListOf(Filter.class), any(AccessToken.class))).thenReturn(-1);
        collectionsDaoImpl.findCollectionCount("persons_simple", Collections.<Filter> emptyList(), createMockAccessToken());
        verify(jdbcTemplate).queryForObject(eq("WITH cur_user_groups AS (SELECT DISTINCT gg.\"parent_group_id\" FROM \"group_member\" gm " +
                "INNER JOIN \"group_group\" gg ON gg.\"child_group_id\" = gm.\"usergroup\" " +
                "WHERE gm.\"person_id\" = :user_id) " + "SELECT count(*) FROM ("
                + "SELECT person.* FROM \"person\" person WHERE 1 = 1 "
                + "AND EXISTS (SELECT 1 FROM \"person_read\" r WHERE r.\"group_id\" "
                + "IN (SELECT \"parent_group_id\" FROM \"cur_user_groups\") AND r.\"object_id\" = person.\"access_object_id\")) person WHERE 1 = 1"),
                eq(Collections.<String, Object> singletonMap("user_id", 1)),
                eq(Integer.class));
    }

    @Test
    public void testMulticolumnIn() {
        IdentifiableObjectCollection collection = collectionsDaoImpl.findCollectionByQuery(
                "select id from employee where (department, department_id) in (select id, id_type from department where id = {0})",
                singletonList(new ReferenceValue(new RdbmsId(1, 1))), 0, 0, createMockAccessToken());
        // Need For SonarQube
        assertNull(collection);
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
        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(complexCollectionConfig, filterValues, createByNameSortOrder(), 0, 0, accessToken);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(FIND_COMPLEX_COLLECTION_QUERY_WITH_FILTERS, refinedActualQuery);
    }

    // @Test
    public void testFindCollectionWithoutFilters() throws Exception {
        AccessToken accessToken = createMockAccessToken();

        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(collectionConfig, new ArrayList<Filter>(), createByNameSortOrder(), 0, 0, accessToken);
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
                createByNameSortOrder(), 10, 100, accessToken);
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
        expected.put("PARAM0_0", asList(1L));
        expected.put("PARAM0_0_type", 1L);
        expected.put("PARAM1", 2L);
        expected.put("PARAM1_type", 1L);
        expected.put("PARAM1_0", asList(2L));
        expected.put("PARAM1_0_type", 1L);
        collectionsDaoImpl.findCollectionByQuery(COLLECTION_NOT_EQUALS_REFERENCE, params, 0, 0, accessToken);

        verify(jdbcTemplate).query(eq(COLLECTION_NOT_EQUALS_REFERENCE_RESULT),
                eq(expected), any(CollectionRowMapper.class));

    }

    @Test
    public void testFindCollectionCountWithFilters() throws Exception {

        when(globalCacheClient.getCollectionCount(anyString(), anyListOf(Filter.class), any(AccessToken.class))).thenReturn(-1);
        when(jdbcTemplate.queryForObject(anyString(), anyMapOf(String.class, Object.class), eq(Integer.class))).thenReturn(0);

        Filter filter = new Filter();
        filter.setFilter("byParent");
        filter.addReferenceCriterion(0, new RdbmsId(1, 1));

        collectionsDaoImpl.findCollectionCount("children", asList(filter), createMockSystemAccessToken());

        HashMap<String, Object> expected = new HashMap<>();
        expected.put("byParent_0", 1L);
        expected.put("byParent_0_type", 1L);
        expected.put("byParent_0_0", asList(1L));
        expected.put("byParent_0_0_type", 1L);

        verify(jdbcTemplate).queryForObject("SELECT count(*) FROM \"child\" WHERE 1 = 1 AND (\"parent\" = :byParent_0 AND \"parent_type\" = :byParent_0_type)",
                expected, Integer.class);
    }

    @Test
    public void testGeneratorAccessesIdsBasedFilters() {
        Filter filter = new IdsIncludedFilter(new ReferenceValue(new RdbmsId(1, 1)));
        IdentifiableObjectCollection collection = collectionsDaoImpl.findCollection("generated", asList(filter), new SortOrder(), 0, 0, createMockSystemAccessToken());
        // Need For SonarQube
        assertNull(collection);
        collectionsDaoImpl.findCollectionCount("generated", asList(filter), createMockSystemAccessToken());
    }

    @Test
    public void testWindowFunctionWithExpression() {
        collectionsDaoImpl.findCollectionByQuery("select Name, min(created_date) over (partition by Boss) from Person p",
                asList(new Value<?>[] { }), 0, 0,
                createMockSystemAccessToken());
        verify(jdbcTemplate).query(
                eq("SELECT \"name\", min(\"created_date\") OVER (PARTITION BY \"boss\" ) FROM \"person\" p"),
                eq(new HashMap<String, Object>()), any(CollectionRowMapper.class));
    }

    @Test
    public void testGlobalPlaceholder() {
        AccessToken accessToken = createMockSystemAccessToken();

        collectionsDaoImpl.findCollection("PersonsWithGlobalPlaceholder", null, null, 0, 0, accessToken);

        verify(jdbcTemplate).query(eq("SELECT \"id\", \"id_type\", to_char(\"updated_date\", 'DD.MM.YYYY') \"modify_date\", to_char(\"created_date\", 'DD.MM.YYYY') \"create_date\" FROM \"person\""),
                eq(new HashMap<String, Object>()), any(CollectionRowMapper.class));
    }

    private String refineQuery(String actualQuery) {
        return actualQuery.trim().replaceAll("\\s+", " ");
    }

    private CollectionFilterCriteriaConfig where(String where, String placeholder) {
        CollectionFilterCriteriaConfig c = new CollectionFilterCriteriaConfig();
        c.setPlaceholder(placeholder);
        c.setValue(where);
        return c;
    }

    private CollectionFilterCriteriaConfig where(String where) {
        return where(where, "where-clause");
    }

    private CollectionFilterReferenceConfig from(String from, String placeholder) {
        CollectionFilterReferenceConfig c = new CollectionFilterReferenceConfig();
        c.setPlaceholder(placeholder);
        c.setValue(from);
        return c;
    }

    private SortOrder createByNameSortOrder() {
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("e.name", SortCriterion.Order.ASCENDING));
        return sortOrder;
    }

    private CollectionConfig createCollectionConfig(String name, String prototype, String countingPrototype, CollectionFilterConfig... filterConfigs) {
        CollectionConfig result = new CollectionConfig();
        result.setName(name);
        result.setPrototype(prototype);
        result.setCountingPrototype(countingPrototype);
        result.setIdField("id");
        result.setFilters(new ArrayList<>(asList(filterConfigs)));
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
