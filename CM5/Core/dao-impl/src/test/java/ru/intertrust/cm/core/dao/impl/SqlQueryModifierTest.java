package ru.intertrust.cm.core.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.TYPE_COLUMN;

import java.util.Arrays;

import net.sf.jsqlparser.statement.select.SelectBody;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdsExcludedFilter;
import ru.intertrust.cm.core.business.api.dto.IdsIncludedFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryParser;

@RunWith(MockitoJUnitRunner.class)
public class SqlQueryModifierTest {

    private static final String ID_FIELD = "id";
    private static final String UNION_QUERY = "SELECT * FROM \"EMPLOYEE\" e, \"Department\" d where 1 = 1 and e.\"id\" = 1 " +
            "union SELECT * FROM \"EMPLOYEE\" e, \"Department\" d where 1 = 1 and e.\"id\" = 2";

    private static final String PLAIN_SELECT_QUERY = "SELECT * FROM \"EMPLOYEE\" e, \"Department\" d where 1 = 1 and e.\"id\" = 1";

    private static final String PLAIN_SELECT_QUERY_WITHOUT_WHERE = "SELECT * FROM \"EMPLOYEE\" e, \"Department\" d";

    private static final String PLAIN_SELECT_QUERY_WITHOUT_WHERE_ACL_APPLIED = "SELECT * FROM (SELECT * FROM \"EMPLOYEE\" EMPLOYEE "
            + "WHERE EXISTS (SELECT r.\"object_id\" FROM \"employee_read\" r "
            + "INNER JOIN \"group_group\" gg ON r.\"group_id\" = gg.\"parent_group_id\" "
            + "INNER JOIN \"group_member\" gm ON gg.\"child_group_id\" = gm.\"usergroup\" "
            + "INNER JOIN \"person\" rt ON r.\"object_id\" = rt.\"access_object_id\" "
            + "WHERE gm.\"person_id\" = :user_id AND rt.\"id\" = EMPLOYEE.\"id\")) e, "
            + "(SELECT * FROM \"Department\" Department WHERE EXISTS ("
            + "SELECT r.\"object_id\" FROM \"department_read\" r "
            + "INNER JOIN \"group_group\" gg ON r.\"group_id\" = gg.\"parent_group_id\" "
            + "INNER JOIN \"group_member\" gm ON gg.\"child_group_id\" = gm.\"usergroup\" "
            + "INNER JOIN \"person\" rt ON r.\"object_id\" = rt.\"access_object_id\" "
            + "WHERE gm.\"person_id\" = :user_id AND rt.\"id\" = Department.\"id\")) d";

    private static final String PLAIN_SELECT_QUERY_WITH_TYPE = "SELECT * FROM " +
            "\"EMPLOYEE\" e, " +
            "\"Department\" d WHERE 1 = 1 AND e.\"id\" = 1";

    private static final String PLAIN_SELECT_QUERY_WITH_IDS_INCLUDED_FILTERS = "SELECT * FROM \"EMPLOYEE\" e, " +
            "\"Department\" d WHERE 1 = 1 AND (e.\"id\" = :idsIncluded10 AND e.\"id_type\" = :idsIncluded10_type) AND " +
            "((e.\"id\" = :idsIncluded20 AND e.\"id_type\" = :idsIncluded20_type) OR " +
            "(e.\"id\" = :idsIncluded21 AND e.\"id_type\" = :idsIncluded21_type))";

    private static final String PLAIN_SELECT_QUERY_WITH_IDS_EXCLUDED_FILTERS = "SELECT * FROM \"EMPLOYEE\" e, " +
            "\"Department\" d WHERE 1 = 1 AND (e.\"person\" <> :idsExcluded10 OR e.\"person_type\" <> :idsExcluded10_type) " +
            "AND ((e.\"person\" <> :idsExcluded20 OR e.\"person_type\" <> :idsExcluded20_type) AND " +
            "(e.\"person\" <> :idsExcluded21 OR e.\"person_type\" <> :idsExcluded21_type))";

    private static final String UNION_QUERY_WITH_TYPE = "(SELECT * FROM \"EMPLOYEE\" e, " +
            "\"Department\" d WHERE 1 = 1 AND e.\"id\" = 1) " +
            "UNION (SELECT * FROM \"EMPLOYEE\" e, \"Department\" d WHERE 1 = 1 " +
            "AND e.\"id\" = 2)";

    private static final String PLAIN_SELECT_QUERY_WITH_ACL = "SELECT * FROM "
            + "(SELECT * FROM \"EMPLOYEE\" EMPLOYEE WHERE EXISTS (SELECT r.\"object_id\" FROM \"employee_read\" r "
            + "INNER JOIN \"group_group\" gg ON r.\"group_id\" = gg.\"parent_group_id\" "
            + "INNER JOIN \"group_member\" gm ON gg.\"child_group_id\" = gm.\"usergroup\" "
            + "INNER JOIN \"person\" rt ON r.\"object_id\" = rt.\"access_object_id\" "
            + "WHERE gm.\"person_id\" = :user_id AND rt.\"id\" = EMPLOYEE.\"id\")) e, "
            + "(SELECT * FROM \"Department\" Department WHERE EXISTS (SELECT r.\"object_id\" "
            + "FROM \"department_read\" r INNER JOIN \"group_group\" gg ON r.\"group_id\" = gg.\"parent_group_id\" "
            + "INNER JOIN \"group_member\" gm ON gg.\"child_group_id\" = gm.\"usergroup\" "
            + "INNER JOIN \"person\" rt ON r.\"object_id\" = rt.\"access_object_id\" "
            + "WHERE gm.\"person_id\" = :user_id AND rt.\"id\" = Department.\"id\")) d WHERE 1 = 1 AND e.\"id\" = 1";

    private static final String UNION_QUERY_WITH_ACL = "(SELECT * FROM (SELECT * FROM \"EMPLOYEE\" EMPLOYEE "
            + "WHERE EXISTS (SELECT r.\"object_id\" FROM \"employee_read\" r "
            + "INNER JOIN \"group_group\" gg ON r.\"group_id\" = gg.\"parent_group_id\" "
            + "INNER JOIN \"group_member\" gm ON gg.\"child_group_id\" = gm.\"usergroup\" "
            + "INNER JOIN \"person\" rt ON r.\"object_id\" = rt.\"access_object_id\" "
            + "WHERE gm.\"person_id\" = :user_id AND rt.\"id\" = EMPLOYEE.\"id\")) e, "
            + "(SELECT * FROM \"Department\" Department WHERE EXISTS (SELECT r.\"object_id\" FROM \"department_read\" r "
            + "INNER JOIN \"group_group\" gg ON r.\"group_id\" = gg.\"parent_group_id\" "
            + "INNER JOIN \"group_member\" gm ON gg.\"child_group_id\" = gm.\"usergroup\" "
            + "INNER JOIN \"person\" rt ON r.\"object_id\" = rt.\"access_object_id\" "
            + "WHERE gm.\"person_id\" = :user_id AND rt.\"id\" = Department.\"id\")) d WHERE 1 = 1 AND e.\"id\" = 1) "
            + "UNION (SELECT * FROM (SELECT * FROM \"EMPLOYEE\" EMPLOYEE WHERE EXISTS "
            + "(SELECT r.\"object_id\" FROM \"employee_read\" r INNER JOIN \"group_group\" gg ON r.\"group_id\" = gg.\"parent_group_id\" "
            + "INNER JOIN \"group_member\" gm ON gg.\"child_group_id\" = gm.\"usergroup\" "
            + "INNER JOIN \"person\" rt ON r.\"object_id\" = rt.\"access_object_id\" "
            + "WHERE gm.\"person_id\" = :user_id AND rt.\"id\" = EMPLOYEE.\"id\")) e, (SELECT * FROM \"Department\" Department "
            + "WHERE EXISTS (SELECT r.\"object_id\" FROM \"department_read\" r "
            + "INNER JOIN \"group_group\" gg ON r.\"group_id\" = gg.\"parent_group_id\" "
            + "INNER JOIN \"group_member\" gm ON gg.\"child_group_id\" = gm.\"usergroup\" "
            + "INNER JOIN \"person\" rt ON r.\"object_id\" = rt.\"access_object_id\" "
            + "WHERE gm.\"person_id\" = :user_id AND rt.\"id\" = Department.\"id\")) d WHERE 1 = 1 AND e.\"id\" = 2)";

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

    @Before
    public void setUp(){
        when(configurationExplorer.isReadPermittedToEverybody(anyString())).thenReturn(false);    
        when(configurationExplorer.getDomainObjectRootType(anyString())).thenReturn("person");
        when(configurationExplorer.getConfig(eq(DomainObjectTypeConfig.class), anyString())).thenReturn(new DomainObjectTypeConfig());
        
    }
    
    @Test
    public void testAddTypeColumn() {
        Configuration configuration = new Configuration();
        GlobalSettingsConfig globalSettings = new GlobalSettingsConfig();
        configuration.getConfigurationList().add(globalSettings);
        ConfigurationExplorer configurationExplorer = new ConfigurationExplorerImpl(configuration);
        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier(configurationExplorer);

        String modifiedQuery = collectionQueryModifier.addServiceColumns(PLAIN_SELECT_QUERY);

        assertEquals(PLAIN_SELECT_QUERY_WITH_TYPE, modifiedQuery);

        modifiedQuery = collectionQueryModifier.addServiceColumns(UNION_QUERY);

        assertEquals(UNION_QUERY_WITH_TYPE, modifiedQuery);
    }

    @Test
    public void testWrapAndLowerCaseNames() {
        String modifiedQuery = SqlQueryModifier.wrapAndLowerCaseNames(WRAP_AND_LOWERCASE_QUERY);
        assertEquals(WRAP_AND_LOWERCASE_CHECK_QUERY, modifiedQuery);
    }

    @Test
    public void testAddAclQuery() {
        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier(configurationExplorer);
        String modifiedQuery = collectionQueryModifier.addAclQuery(PLAIN_SELECT_QUERY);

        assertEquals(PLAIN_SELECT_QUERY_WITH_ACL, modifiedQuery);

        modifiedQuery = collectionQueryModifier.addAclQuery(UNION_QUERY);

        assertEquals(UNION_QUERY_WITH_ACL, modifiedQuery);
    }

    @Test
    public void testAddAclQueryToSqlWithoutWhereClause() {
        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier(configurationExplorer);
        String modifiedQuery = collectionQueryModifier.addAclQuery(PLAIN_SELECT_QUERY_WITHOUT_WHERE);

        assertEquals(PLAIN_SELECT_QUERY_WITHOUT_WHERE_ACL_APPLIED, modifiedQuery);
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

        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier(configurationExplorer);
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

        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier(configurationExplorer);
        SqlQueryParser sqlParser = new SqlQueryParser(PLAIN_SELECT_QUERY_WITHOUT_WHERE);
        SelectBody selectBody = sqlParser.getSelectBody();

        selectBody = collectionQueryModifier.addIdBasedFilters(selectBody,
                Arrays.asList(new Filter[]{idsExcludedFilter1, idsExcludedFilter2}), "person");

        assertEquals(PLAIN_SELECT_QUERY_WITH_IDS_EXCLUDED_FILTERS, selectBody.toString());
    }

}
