package ru.intertrust.cm.core.dao.impl;

import java.util.Arrays;
import java.util.Map;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdsExcludedFilter;
import ru.intertrust.cm.core.business.api.dto.IdsIncludedFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.SecurityStamp;
import ru.intertrust.cm.core.dao.impl.sqlparser.FakeConfigurationExplorer;
import ru.intertrust.cm.core.dao.impl.sqlparser.FakeConfigurationExplorer.TypeConfigBuilder;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqlQueryModifierTest {

    private static final String UNION_QUERY = "SELECT * FROM \"employee\" e, \"department\" d where 1 = 1 and e.\"id\" = 1 " +
            "union SELECT * FROM \"employee\" e, \"department\" d where 1 = 1 and e.\"id\" = 2";

    private static final String PLAIN_SELECT_QUERY = "SELECT * FROM \"employee\" e, \"department\" d where 1=1 and e.\"id\" = 1";

    private static final String PLAIN_SELECT_QUERY_WITH_WITH = "WITH a as (select * from department) " +
            "SELECT * FROM \"employee\" e where 1=1 and e.\"id\" = 1";

    private static final String PLAIN_SELECT_QUERY_WITHOUT_WHERE = "SELECT * FROM \"employee\" e, \"department\" d";

    private static final String PLAIN_SELECT_QUERY_WITH_TYPE = "SELECT * FROM " +
            "\"employee\" e, " +
            "\"department\" d WHERE 1 = 1 AND e.\"id\" = 1";

    private static final String UNION_QUERY_WITH_TYPE = "SELECT * FROM \"employee\" e, " +
            "\"department\" d WHERE 1 = 1 AND e.\"id\" = 1 " +
            "UNION SELECT * FROM \"employee\" e, \"department\" d WHERE 1 = 1 " +
            "AND e.\"id\" = 2";

    private static final String PLAIN_SELECT_QUERY_WITHOUT_WHERE_ACL =
            "WITH cur_user_groups AS (" +
                    "SELECT DISTINCT gg.\"parent_group_id\" FROM \"group_member\" gm " +
                    "INNER JOIN \"group_group\" gg ON gg.\"child_group_id\" = gm.\"usergroup\" " +
                    "WHERE gm.\"person_id\" = :user_id) " +
                    "SELECT * FROM (SELECT employee.* FROM \"employee\" employee WHERE 1 = 1 AND " +
                    "EXISTS (SELECT 1 FROM \"employee_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) AND " +
                    "r.\"object_id\" = employee.\"access_object_id\")) e, " +
                    "(SELECT department.* FROM \"department\" department WHERE 1 = 1 AND " +
                    "EXISTS (SELECT 1 FROM \"department_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) " +
                    "AND r.\"object_id\" = department.\"access_object_id\")) d";

    private static final String PLAIN_SELECT_QUERY_WITH_ACL =
            "WITH cur_user_groups AS (" +
                    "SELECT DISTINCT gg.\"parent_group_id\" FROM \"group_member\" gm " +
                    "INNER JOIN \"group_group\" gg ON gg.\"child_group_id\" = gm.\"usergroup\" " +
                    "WHERE gm.\"person_id\" = :user_id) " +
                    "SELECT * FROM (SELECT employee.* FROM \"employee\" employee " +
                    "WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"employee_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) AND " +
                    "r.\"object_id\" = employee.\"access_object_id\")) e, " +
                    "(SELECT department.* FROM \"department\" department " +
                    "WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"department_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) " +
                    "AND r.\"object_id\" = department.\"access_object_id\")) d " +
                    "WHERE 1 = 1 AND e.\"id\" = 1";

    private static final String PLAIN_SELECT_QUERY_WITH_WITH_ACL =
            "WITH cur_user_groups AS (SELECT DISTINCT gg.\"parent_group_id\" FROM \"group_member\" gm INNER JOIN \"group_group\" gg ON gg.\"child_group_id\" = gm.\"usergroup\" WHERE gm.\"person_id\" = :user_id), " +
                    "a AS (SELECT * FROM (SELECT department.* FROM \"department\" department WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"department_read\" r WHERE r.\"group_id\" " +
                    "IN (SELECT \"parent_group_id\" FROM cur_user_groups) AND r.\"object_id\" = department.\"access_object_id\")) department) " +
                    "SELECT * FROM (SELECT employee.* FROM \"employee\" employee WHERE 1 = 1 " +
                    "AND EXISTS (SELECT 1 FROM \"employee_read\" r WHERE r.\"group_id\" " +
                    "IN (SELECT \"parent_group_id\" FROM cur_user_groups) AND r.\"object_id\" = employee.\"access_object_id\")) e WHERE 1 = 1 AND e.\"id\" = 1";

    private static final String UNION_QUERY_WITH_ACL =
            "WITH cur_user_groups AS (" +
                    "SELECT DISTINCT gg.\"parent_group_id\" FROM \"group_member\" gm " +
                    "INNER JOIN \"group_group\" gg ON gg.\"child_group_id\" = gm.\"usergroup\" " +
                    "WHERE gm.\"person_id\" = :user_id) " +
                    "SELECT * FROM (SELECT employee.* FROM \"employee\" employee " +
                    "WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"employee_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) AND " +
                    "r.\"object_id\" = employee.\"access_object_id\")) e, " +
                    "(SELECT department.* FROM \"department\" department " +
                    "WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"department_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) " +
                    "AND r.\"object_id\" = department.\"access_object_id\")) d " +
                    "WHERE 1 = 1 AND e.\"id\" = 1 UNION " +
                    "SELECT * FROM (SELECT employee.* FROM \"employee\" employee " +
                    "WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"employee_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) AND " +
                    "r.\"object_id\" = employee.\"access_object_id\")) e, " +
                    "(SELECT department.* FROM \"department\" department WHERE 1 = 1 AND " +
                    "EXISTS (SELECT 1 FROM \"department_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) " +
                    "AND r.\"object_id\" = department.\"access_object_id\")) d " +
                    "WHERE 1 = 1 AND e.\"id\" = 2";

    private static final String WRAP_AND_LOWERCASE_QUERY = "SELECT module.Id, module.type_id " +
            "FROM SS_MODULE module " +
            "JOIN SS_ModuleType AS type ON type.id = module.Type " +
            "JOIN SS_ModuleOrg AS org ON org.Module = module.id " +
            "JOIN SS_ModuleOrg AS org2 ON org2.Organization = org.Organization " +
            "WHERE 1 = 1 AND type.Name = ? AND org2.Module = ? AND type.Name = ? AND org2.Module = ?";

    private static final String WRAP_AND_LOWERCASE_CHECK_QUERY = "SELECT module.\"id\", module.\"type_id\" " +
            "FROM \"ss_module\" module " +
            "JOIN \"ss_moduletype\" AS type ON type.\"id\" = module.\"type\" " +
            "JOIN \"ss_moduleorg\" AS org ON org.\"module\" = module.\"id\" " +
            "JOIN \"ss_moduleorg\" AS org2 ON org2.\"organization\" = org.\"organization\" " +
            "WHERE 1 = 1 AND type.\"name\" = ? AND org2.\"module\" = ? AND type.\"name\" = ? AND org2.\"module\" = ?";

    @Mock
    private ConfigurationExplorer configurationExplorer;

    @Mock
    private CurrentUserAccessor currentUserAccessor;

    @Mock
    private UserGroupGlobalCache userGroupCache;

    private final DomainObjectQueryHelper domainObjectQueryHelper = new DomainObjectQueryHelper();

    @Before
    public void setUp() {
        when(configurationExplorer.isReadPermittedToEverybody(anyString())).thenReturn(false);
        when(configurationExplorer.getDomainObjectRootType(anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArguments()[0].toString());
        when(configurationExplorer.getConfig(eq(DomainObjectTypeConfig.class), anyString())).thenReturn(new DomainObjectTypeConfig());
        when(userGroupCache.isAdministrator(any(Id.class))).thenReturn(false);
        when(currentUserAccessor.getCurrentUserId()).thenReturn(new RdbmsId(1, 1));

        domainObjectQueryHelper.setConfigurationExplorer(configurationExplorer);
        domainObjectQueryHelper.setCurrentUserAccessor(currentUserAccessor);
        domainObjectQueryHelper.setUserGroupCache(userGroupCache);
        ReflectionTestUtils.setField(domainObjectQueryHelper, "securityStamp", mock(SecurityStamp.class));
    }

    @Test
    public void testAddTypeColumn() {
        Configuration configuration = new Configuration();
        GlobalSettingsConfig globalSettings = new GlobalSettingsConfig();
        configuration.getConfigurationList().add(globalSettings);
        ConfigurationExplorerImpl configurationExplorer = new ConfigurationExplorerImpl(configuration);
        configurationExplorer.init();
        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier(configurationExplorer, userGroupCache,
                currentUserAccessor, domainObjectQueryHelper);
        SqlQueryParser sqlQueryParser = new SqlQueryParser(PLAIN_SELECT_QUERY);

        collectionQueryModifier.addServiceColumns(sqlQueryParser.getSelectStatement());

        assertEquals(PLAIN_SELECT_QUERY_WITH_TYPE, sqlQueryParser.toString());

        sqlQueryParser = new SqlQueryParser(UNION_QUERY);
        collectionQueryModifier.addServiceColumns(sqlQueryParser.getSelectStatement());

        assertEquals(UNION_QUERY_WITH_TYPE, sqlQueryParser.toString());
    }

    @Test
    public void testWrapAndLowerCaseNames() {
        String modifiedQuery = SqlQueryModifier.wrapAndLowerCaseNames(new SqlQueryParser(WRAP_AND_LOWERCASE_QUERY).getSelectStatement());
        assertEquals(WRAP_AND_LOWERCASE_CHECK_QUERY, modifiedQuery);
    }

    @Test
    public void testAddAclQuery() {
        SqlQueryModifier collectionQueryModifier = createSqlQueryModifier();

        SqlQueryParser aclSqlParser = new SqlQueryParser(PLAIN_SELECT_QUERY);
        Select select = aclSqlParser.getSelectStatement();
        collectionQueryModifier.addAclQuery(select);

        String modifiedQuery = select.toString();
        assertEquals(PLAIN_SELECT_QUERY_WITH_ACL, modifiedQuery);

        aclSqlParser = new SqlQueryParser(PLAIN_SELECT_QUERY_WITH_WITH);
        select = aclSqlParser.getSelectStatement();
        collectionQueryModifier.addAclQuery(select);

        modifiedQuery = select.toString();
        assertEquals(PLAIN_SELECT_QUERY_WITH_WITH_ACL, modifiedQuery);

        aclSqlParser = new SqlQueryParser(UNION_QUERY);
        select = aclSqlParser.getSelectStatement();
        collectionQueryModifier.addAclQuery(select);

        modifiedQuery = select.toString();
        assertEquals(UNION_QUERY_WITH_ACL, modifiedQuery);
    }

    private SqlQueryModifier createSqlQueryModifier() {
        return new SqlQueryModifier(configurationExplorer, userGroupCache, currentUserAccessor, domainObjectQueryHelper);
    }

    @Test
    public void testAddAclQueryToSqlWithoutWhereClause() {
        SqlQueryModifier collectionQueryModifier = createSqlQueryModifier();

        SqlQueryParser aclSqlParser = new SqlQueryParser(PLAIN_SELECT_QUERY_WITHOUT_WHERE);
        Select select = aclSqlParser.getSelectStatement();
        collectionQueryModifier.addAclQuery(select);

        String modifiedQuery = select.toString();
        assertEquals(PLAIN_SELECT_QUERY_WITHOUT_WHERE_ACL, modifiedQuery);
    }

    @Test
    public void testIdsIncludedFilter() {
        IdsIncludedFilter idsIncludedFilter1 = new IdsIncludedFilter();
        idsIncludedFilter1.setFilter("idsIncluded1");
        idsIncludedFilter1.addCriterion(0, new ReferenceValue(new RdbmsId(1, 100)));

        IdsIncludedFilter idsIncludedFilter2 = new IdsIncludedFilter();
        idsIncludedFilter2.setFilter("idsIncluded2");
        idsIncludedFilter2.addCriterion(0, new ReferenceValue(new RdbmsId(1, 101)));
        idsIncludedFilter2.addCriterion(1, new ReferenceValue(new RdbmsId(1, 102)));

        SqlQueryModifier collectionQueryModifier = createSqlQueryModifier();
        SqlQueryParser sqlParser = new SqlQueryParser(PLAIN_SELECT_QUERY_WITHOUT_WHERE);
        SelectBody selectBody = sqlParser.getSelectBody();

        selectBody = collectionQueryModifier.addIdBasedFilters(selectBody,
                Arrays.asList(idsIncludedFilter1, idsIncludedFilter2), "id");

        assertEquals("SELECT * FROM \"employee\" e, \"department\" d WHERE 1 = 1 AND e.\"id\" IN ("
                + CollectionsDaoImpl.PARAM_NAME_PREFIX + "idsIncluded10" + ") AND e.\"id\" IN ("
                + CollectionsDaoImpl.PARAM_NAME_PREFIX + "idsIncluded20" + ")", selectBody.toString());
    }

    @Test
    public void testIdsExcludedFilter() {
        IdsExcludedFilter idsExcludedFilter1 = new IdsExcludedFilter();
        idsExcludedFilter1.setFilter("idsExcluded1");
        idsExcludedFilter1.addCriterion(0, new ReferenceValue(new RdbmsId(1, 100)));

        IdsExcludedFilter idsExcludedFilter2 = new IdsExcludedFilter();
        idsExcludedFilter2.setFilter("idsExcluded2");
        idsExcludedFilter2.addCriterion(0, new ReferenceValue(new RdbmsId(1, 101)));
        idsExcludedFilter2.addCriterion(1, new ReferenceValue(new RdbmsId(1, 102)));

        SqlQueryModifier collectionQueryModifier = createSqlQueryModifier();
        SqlQueryParser sqlParser = new SqlQueryParser(PLAIN_SELECT_QUERY_WITHOUT_WHERE);
        SelectBody selectBody = sqlParser.getSelectBody();

        selectBody = collectionQueryModifier.addIdBasedFilters(selectBody,
                Arrays.asList(idsExcludedFilter1, idsExcludedFilter2), "person");

        assertEquals("SELECT * FROM \"employee\" e, \"department\" d WHERE 1 = 1 AND e.\"person\" NOT IN ("
                + CollectionsDaoImpl.PARAM_NAME_PREFIX + "idsExcluded10" + ") AND e.\"person\" NOT IN ("
                + CollectionsDaoImpl.PARAM_NAME_PREFIX + "idsExcluded20" + ")", selectBody.toString());
    }

    @Test
    public void testTransformCountQuery() {
        assertEquals("WITH t AS (SELECT id, name FROM docs) SELECT count(*) FROM t"
                , SqlQueryModifier.transformToCountQuery("WITH t AS (SELECT id, name FROM docs) SELECT id, name FROM t"));
    }


    @Test
    public void testSubSelectRefFields() {
        FakeConfigurationExplorer confExplorer = new FakeConfigurationExplorer();
        confExplorer.createTypeConfig((new TypeConfigBuilder("cg_action")).addReferenceField("id", "cg_action").addReferenceField("Module", "SS_Module"));
        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier(confExplorer, userGroupCache, currentUserAccessor, domainObjectQueryHelper);
        SqlQueryParser sqlParser = new SqlQueryParser("SELECT id, Module FROM (SELECT cg_action.id AS id, cg_action.module as Module FROM CG_Action cg_action ) s");
        Select select = sqlParser.getSelectStatement();

        collectionQueryModifier.addServiceColumns(select);

        assertEquals("SELECT id, id_type, Module, module_type FROM (SELECT cg_action.id AS id, cg_action.id_type \"id_type\", cg_action.module AS Module, cg_action.module_type \"module_type\" FROM CG_Action cg_action) s", select.toString());
    }


    @Test
    @Ignore("Бага в парсере")
    public void testRefFieldFromJoin() {
        FakeConfigurationExplorer confExplorer = new FakeConfigurationExplorer();
        confExplorer.createTypeConfig((new TypeConfigBuilder("action")).addReferenceField("id", "action").addReferenceField("module_id", "module"));
        confExplorer.createTypeConfig((new TypeConfigBuilder("module")).addReferenceField("id", "module").addReferenceField("module_type_id", "module_type"));
        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier(confExplorer, userGroupCache, currentUserAccessor, domainObjectQueryHelper);
        SqlQueryParser sqlParser = new SqlQueryParser("select a.id, module_type_id from action a join module m on m.id = a.module_id");
        Select select = sqlParser.getSelectStatement();

        collectionQueryModifier.addServiceColumns(select);

        assertEquals("SELECT a.id, a.id_type, module_type_id, module_type_id_type FROM action a JOIN module m on m.id = a.module_id", select.toString());
    }

    @Test
    public void testReferenceAndStringFieldsWithTheSameFieldName() {
        FakeConfigurationExplorer explorer = new FakeConfigurationExplorer();
        explorer.createTypeConfig(new TypeConfigBuilder("ss_moduletype").addReferenceField("id", "ss_moduletype"));
        explorer.createTypeConfig(new TypeConfigBuilder("ss_module").addReferenceField("id", "ss_module")
                .addReferenceField("type", "ss_moduletype"));
        explorer.createTypeConfig(new TypeConfigBuilder("f_dp_rkk").addReferenceField("id", "f_dp_rkk")
                .addStringField("type").addReferenceField("module", "ss_module"));

        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier(explorer, userGroupCache, currentUserAccessor, domainObjectQueryHelper);
        SqlQueryParser sqlParser = new SqlQueryParser(
                "SELECT rkk.id rid, rkk.\"type\" doctype, m.type t " +
                        "                FROM f_dp_rkk rkk " +
                        "                LEFT JOIN ss_module m ON m.id = rkk.module " +
                        "                LEFT JOIN ss_moduletype mt ON mt.id = m.type");
        Select select = sqlParser.getSelectStatement();
        collectionQueryModifier.addServiceColumns(select);

        assertEquals("SELECT rkk.id rid, rkk.id_type rid_type, rkk.type doctype, m.type t, m.type_type t_type FROM f_dp_rkk rkk LEFT JOIN ss_module m ON m.id = rkk.module LEFT JOIN ss_moduletype mt ON mt.id = m.type", select.toString().replace("\"", ""));

    }

    @Test
    public void testBuildColumnToConfigMapForSelectItemsWithJoin() {
        FakeConfigurationExplorer confExplorer = new FakeConfigurationExplorer();
        confExplorer.createTypeConfig((new TypeConfigBuilder("f_dp_resolution")).addReferenceField("hierparent", "f_dp_resolution").addReferenceField("id", "f_dp_resolution"));
        confExplorer.createTypeConfig((new TypeConfigBuilder("f_dp_resltnbase")).addReferenceField("id", "f_dp_resolution"));

        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier(confExplorer, userGroupCache, currentUserAccessor, domainObjectQueryHelper);
        SqlQueryParser sqlParser = new SqlQueryParser("SELECT parent_id, parent_id_type FROM (SELECT resf.hierparent parent_id ,resf.hierparent_type parent_id_type FROM (SELECT f_dp_resolution.* FROM f_dp_resolution f_dp_resolution) resf JOIN ( SELECT f_dp_resltnbase.* FROM f_dp_resltnbase f_dp_resltnbase ) res2 ON res2.id = resf.hierparent) s");
        Select select = sqlParser.getSelectStatement();

        Map<String, FieldConfig> columnConfig = collectionQueryModifier.buildColumnToConfigMapForSelectItems(select);

        assertNotNull(columnConfig.get("parent_id"));
        assertEquals(ReferenceFieldConfig.class, columnConfig.get("parent_id").getClass());
    }

    @Test
    public void caseTest() {
        String select = "select CASE \n" +
                "       WHEN d.parentdepartment is null \n" +
                "       THEN d.id \n" +
                "       ELSE d.parentdepartment \n" +
                "       END as id \n" +
                "from department d";

        FakeConfigurationExplorer confExplorer = new FakeConfigurationExplorer();
        confExplorer.createTypeConfig(new TypeConfigBuilder("department").addReferenceField("id", "department").addReferenceField("parentdepartment", "department"));

        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier(confExplorer, userGroupCache, currentUserAccessor, domainObjectQueryHelper);

        SqlQueryParser sqlParser = new SqlQueryParser(select);
        final Select selectStatement = sqlParser.getSelectStatement();
        collectionQueryModifier.addServiceColumns(selectStatement);

        assertEquals("SELECT CASE WHEN d.parentdepartment IS NULL THEN d.id ELSE d.parentdepartment END AS id, CASE WHEN d.parentdepartment IS NULL THEN d.\"id_type\" ELSE d.\"parentdepartment_type\" END id_type FROM department d", selectStatement.toString());
    }

    @Test
    public void constantsWithID() {
        String select = "select null as null_id, 'some_string' as st, '1111000000000001' as system_id, d.id from department d";

        FakeConfigurationExplorer confExplorer = new FakeConfigurationExplorer();
        confExplorer.createTypeConfig(new TypeConfigBuilder("department").addReferenceField("id", "department"));

        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier(confExplorer, userGroupCache, currentUserAccessor, domainObjectQueryHelper);

        SqlQueryParser sqlParser = new SqlQueryParser(select);
        final Select selectStatement = sqlParser.getSelectStatement();
        collectionQueryModifier.addServiceColumns(selectStatement);

        // Не знаю, почему NULL null_id_type, а не NULL AS null_id_type, но запрос в целом корректен, оставлю
        assertEquals("SELECT NULL AS null_id, NULL null_id_type, 'some_string' AS st, 1 AS system_id, 1111 system_id_type, d.id, d.id_type FROM department d", selectStatement.toString());
    }

    @Test
    public void withExternalTables() {
        String select = "with person_data as ( select id, Login from person), emp_data as (select em.id as emId, pData.id as pId, em.name, em.depId from person_data as pData left join employee em on (pData.id = em.personId) ) select ppData.id, ppData.Login, dep.name from person_data ppData left join emp_data emData on (ppData.id = emData.pId) left join department depp on (depp.id = emData.depId)";


        FakeConfigurationExplorer confExplorer = new FakeConfigurationExplorer();
        confExplorer.createTypeConfig(new TypeConfigBuilder("person")
                .addReferenceField("id", "person")
                .addStringField("Login"));
        confExplorer.createTypeConfig(new TypeConfigBuilder("employee")
                .addReferenceField("personId", "person")
                .addReferenceField("id", "employee")
                .addStringField("name")
                .addReferenceField("depId", "department"));
        confExplorer.createTypeConfig(new TypeConfigBuilder("department")
                .addReferenceField("id", "department"));

        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier(confExplorer, userGroupCache, currentUserAccessor, domainObjectQueryHelper);

        SqlQueryParser sqlParser = new SqlQueryParser(select);
        final Select selectStatement = sqlParser.getSelectStatement();
        collectionQueryModifier.addServiceColumns(selectStatement);

        assertEquals("WITH person_data AS (SELECT id, id_type, Login FROM person), emp_data AS (SELECT em.id AS emId, em.id_type \"emid_type\", pData.id AS pId, em.name, em.depId, em.depid_type FROM person_data AS pData LEFT JOIN employee em ON (pData.id = em.personId)) SELECT id, id_type, ppData.Login, dep.name FROM person_data ppData LEFT JOIN emp_data emData ON (ppData.id = emData.pId) LEFT JOIN department depp ON (depp.id = emData.depId)", selectStatement.toString());
    }

}
