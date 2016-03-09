package ru.intertrust.cm.core.dao.impl;

import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryParser;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
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

    private static final String PLAIN_SELECT_QUERY_WITH_IDS_INCLUDED_FILTERS = "SELECT * FROM \"employee\" e, " +
            "\"department\" d WHERE 1 = 1 AND (e.\"id\" = :idsIncluded10 AND e.\"id_type\" = :idsIncluded10_type) AND " +
            "((e.\"id\" = :idsIncluded20 AND e.\"id_type\" = :idsIncluded20_type) OR " +
            "(e.\"id\" = :idsIncluded21 AND e.\"id_type\" = :idsIncluded21_type))";

    private static final String PLAIN_SELECT_QUERY_WITH_IDS_EXCLUDED_FILTERS = "SELECT * FROM \"employee\" e, " +
            "\"department\" d WHERE 1 = 1 AND (e.\"person\" <> :idsExcluded10 OR e.\"person_type\" <> :idsExcluded10_type) " +
            "AND ((e.\"person\" <> :idsExcluded20 OR e.\"person_type\" <> :idsExcluded20_type) AND " +
            "(e.\"person\" <> :idsExcluded21 OR e.\"person_type\" <> :idsExcluded21_type))";

    private static final String UNION_QUERY_WITH_TYPE = "(SELECT * FROM \"employee\" e, " +
            "\"department\" d WHERE 1 = 1 AND e.\"id\" = 1) " +
            "UNION (SELECT * FROM \"employee\" e, \"department\" d WHERE 1 = 1 " +
            "AND e.\"id\" = 2)";

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
            "WITH a AS (SELECT * FROM department), cur_user_groups AS (" +
                    "SELECT DISTINCT gg.\"parent_group_id\" FROM \"group_member\" gm " +
                    "INNER JOIN \"group_group\" gg ON gg.\"child_group_id\" = gm.\"usergroup\" " +
                    "WHERE gm.\"person_id\" = :user_id) " +
                    "SELECT * FROM (SELECT employee.* FROM \"employee\" employee " +
                    "WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"employee_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) AND " +
                    "r.\"object_id\" = employee.\"access_object_id\")) e WHERE 1 = 1 AND e.\"id\" = 1";

    private static final String UNION_QUERY_WITH_ACL =
            "WITH cur_user_groups AS (" +
                    "SELECT DISTINCT gg.\"parent_group_id\" FROM \"group_member\" gm " +
                    "INNER JOIN \"group_group\" gg ON gg.\"child_group_id\" = gm.\"usergroup\" " +
                    "WHERE gm.\"person_id\" = :user_id) " +
                    "(SELECT * FROM (SELECT employee.* FROM \"employee\" employee " +
                    "WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"employee_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) AND " +
                    "r.\"object_id\" = employee.\"access_object_id\")) e, " +
                    "(SELECT department.* FROM \"department\" department " +
                    "WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"department_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) " +
                    "AND r.\"object_id\" = department.\"access_object_id\")) d " +
                    "WHERE 1 = 1 AND e.\"id\" = 1) UNION " +
                    "(SELECT * FROM (SELECT employee.* FROM \"employee\" employee " +
                    "WHERE 1 = 1 AND EXISTS (SELECT 1 FROM \"employee_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) AND " +
                    "r.\"object_id\" = employee.\"access_object_id\")) e, " +
                    "(SELECT department.* FROM \"department\" department WHERE 1 = 1 AND " +
                    "EXISTS (SELECT 1 FROM \"department_read\" r " +
                    "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) " +
                    "AND r.\"object_id\" = department.\"access_object_id\")) d " +
                    "WHERE 1 = 1 AND e.\"id\" = 2)";

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

    private DomainObjectQueryHelper domainObjectQueryHelper = new DomainObjectQueryHelper();

    @Before
    public void setUp(){
        when(configurationExplorer.isReadPermittedToEverybody(anyString())).thenReturn(false);    
        when(configurationExplorer.getDomainObjectRootType("employee")).thenReturn("employee");
        when(configurationExplorer.getDomainObjectRootType("department")).thenReturn("department");
        when(configurationExplorer.getConfig(eq(DomainObjectTypeConfig.class), anyString())).thenReturn(new DomainObjectTypeConfig());
        when(userGroupCache.isAdministrator(any(Id.class))).thenReturn(false);
        when(currentUserAccessor.getCurrentUserId()).thenReturn(new RdbmsId(1, 1));

        domainObjectQueryHelper.setConfigurationExplorer(configurationExplorer);
        domainObjectQueryHelper.setCurrentUserAccessor(currentUserAccessor);
        domainObjectQueryHelper.setUserGroupCache(userGroupCache);
    }
    
    @Test
    public void testAddTypeColumn() {
        Configuration configuration = new Configuration();
        GlobalSettingsConfig globalSettings = new GlobalSettingsConfig();
        configuration.getConfigurationList().add(globalSettings);
        ConfigurationExplorer configurationExplorer = new ConfigurationExplorerImpl(configuration);
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
                Arrays.asList(new Filter[] {idsIncludedFilter1, idsIncludedFilter2}), "id");

        assertEquals(PLAIN_SELECT_QUERY_WITH_IDS_INCLUDED_FILTERS, selectBody.toString());
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
                Arrays.asList(new Filter[]{idsExcludedFilter1, idsExcludedFilter2}), "person");

        assertEquals(PLAIN_SELECT_QUERY_WITH_IDS_EXCLUDED_FILTERS, selectBody.toString());
    }

}
