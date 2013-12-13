package ru.intertrust.cm.core.dao.impl;

import org.junit.Test;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.TYPE_COLUMN;

public class SqlQueryModifierTest {

    private static final String ID_FIELD = "id";

    private static final String UNION_QUERY = "SELECT * FROM EMPLOYEE e, Department d where 1 = 1 and e.id = 1 " +
            "union SELECT * FROM EMPLOYEE e, Department d where 1 = 1 and e.id = 2";

    private static final String PLAIN_SELECT_QUERY = "SELECT * FROM EMPLOYEE e, Department d where 1 = 1 and e.id = 1";

    private static final String PLAIN_SELECT_QUERY_WITHOUT_WHERE = "SELECT * FROM EMPLOYEE e, Department d";

    private static final String PLAIN_SELECT_QUERY_WITHOUT_WHERE_ACL_APPLIED = "SELECT * FROM EMPLOYEE AS e, " +
            "Department AS d WHERE EXISTS " +
            "(SELECT r.object_id FROM employee_read AS r INNER JOIN group_member AS gm ON r.group_id = gm.usergroup " +
            "WHERE gm.person_id = :user_id AND r.object_id = id)";

    private static final String PLAIN_SELECT_QUERY_WITH_TYPE = "SELECT * FROM " +
            "EMPLOYEE AS e, " +
            "Department AS d WHERE 1 = 1 AND e.id = 1";

    private static final String PLAIN_SELECT_QUERY_WITH_IDS_INCLUDED_FILTERS = "SELECT * FROM EMPLOYEE AS e, " +
            "Department AS d WHERE 1 = 1 AND (e.id = :idsIncluded10 AND e." + TYPE_COLUMN +
            " = :idsIncluded10_type) AND ((e.id = :idsIncluded20 AND e." + TYPE_COLUMN +
            " = :idsIncluded20_type) OR (e.id = :idsIncluded21 AND e." + TYPE_COLUMN + " = :idsIncluded21_type))";

    private static final String PLAIN_SELECT_QUERY_WITH_IDS_EXCLUDED_FILTERS = "SELECT * FROM EMPLOYEE AS e, " +
            "Department AS d WHERE 1 = 1 AND (e.person <> :idsExcluded10 OR e.person_type <> :idsExcluded10_type) " +
            "AND ((e.person <> :idsExcluded20 OR e.person_type <> :idsExcluded20_type) AND " +
            "(e.person <> :idsExcluded21 OR e.person_type <> :idsExcluded21_type))";

    private static final String UNION_QUERY_WITH_TYPE = "(SELECT * FROM EMPLOYEE AS e, " +
            "Department AS d WHERE 1 = 1 AND e.id = 1) " +
            "UNION (SELECT * FROM EMPLOYEE AS e, Department AS d WHERE 1 = 1 " +
            "AND e.id = 2)";

    private static final String PLAIN_SELECT_QUERY_WITH_ACL = "SELECT * FROM EMPLOYEE AS e, " +
            "Department AS d WHERE EXISTS (SELECT r.object_id FROM employee_read AS r INNER JOIN group_member AS gm " +
            "ON r.group_id = gm.usergroup WHERE gm.person_id = :user_id AND r.object_id = id) AND 1 = 1 AND e.id = 1";

    private static final String UNION_QUERY_WITH_ACL = "(SELECT * FROM EMPLOYEE AS e, Department AS d WHERE " +
            "EXISTS (SELECT r.object_id FROM employee_read AS r INNER JOIN group_member AS gm ON " +
            "r.group_id = gm.usergroup WHERE gm.person_id = :user_id AND r.object_id = id) AND 1 = 1 AND e.id = 1) " +
            "UNION (SELECT * FROM EMPLOYEE AS e, Department AS d WHERE EXISTS (SELECT r.object_id " +
            "FROM employee_read AS r INNER JOIN group_member AS gm ON r.group_id = gm.usergroup WHERE " +
            "gm.person_id = :user_id AND r.object_id = id) AND 1 = 1 AND e.id = 2)";

    private static final String WRAP_AND_LOWERCASE_QUERY = "SELECT module.Id, module.type_id " +
            "FROM SS_MODULE AS module " +
            "JOIN SS_ModuleType AS type ON type.id = module.Type " +
            "JOIN SS_ModuleOrg AS org ON org.Module = module.id " +
            "JOIN SS_ModuleOrg AS org2 ON org2.Organization = org.Organization " +
            "WHERE 1 = 1 AND type.Name = ? AND org2.Module = ? AND type.Name = ? AND org2.Module = ?";

    private static final String WRAP_AND_LOWERCASE_CHECK_QUERY = "SELECT module.\"id\", module.\"type_id\" " +
            "FROM \"ss_module\" AS module " +
            "JOIN \"ss_moduletype\" AS type ON type.\"id\" = module.\"type\" " +
            "JOIN \"ss_moduleorg\" AS org ON org.\"module\" = module.\"id\" " +
            "JOIN \"ss_moduleorg\" AS org2 ON org2.\"organization\" = org.\"organization\" " +
            "WHERE 1 = 1 AND type.\"name\" = ? AND org2.\"module\" = ? AND type.\"name\" = ? AND org2.\"module\" = ?";

    @Test
    public void testAddTypeColumn() {
        Configuration configuration = new Configuration();
        GlobalSettingsConfig globalSettings = new GlobalSettingsConfig();
        configuration.getConfigurationList().add(globalSettings);
        ConfigurationExplorer configurationExplorer = new ConfigurationExplorerImpl(configuration);
        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier();
        String modifiedQuery = collectionQueryModifier.addServiceColumns(PLAIN_SELECT_QUERY, configurationExplorer);

        assertEquals(PLAIN_SELECT_QUERY_WITH_TYPE, modifiedQuery);

        modifiedQuery = collectionQueryModifier.addServiceColumns(UNION_QUERY, configurationExplorer);

        assertEquals(UNION_QUERY_WITH_TYPE, modifiedQuery);
    }

    @Test
    public void testWrapAndLowerCaseNames() {
        String modifiedQuery = SqlQueryModifier.wrapAndLowerCaseNames(WRAP_AND_LOWERCASE_QUERY);
        assertEquals(WRAP_AND_LOWERCASE_CHECK_QUERY, modifiedQuery);
    }

    @Test
    public void testAddAclQuery() {
        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier();
        String modifiedQuery = collectionQueryModifier.addAclQuery(PLAIN_SELECT_QUERY, ID_FIELD);

        assertEquals(PLAIN_SELECT_QUERY_WITH_ACL, modifiedQuery);

        modifiedQuery = collectionQueryModifier.addAclQuery(UNION_QUERY, ID_FIELD);

        assertEquals(UNION_QUERY_WITH_ACL, modifiedQuery);
    }

    @Test
    public void testAddAclQueryToSqlWithoutWhereClause() {
        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier();
        String modifiedQuery = collectionQueryModifier.addAclQuery(PLAIN_SELECT_QUERY_WITHOUT_WHERE, ID_FIELD);

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

        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier();
        String modifiedQuery = collectionQueryModifier.addIdBasedFilters(PLAIN_SELECT_QUERY_WITHOUT_WHERE,
                Arrays.asList(new Filter[] {idsIncludedFilter1, idsIncludedFilter2}), "id");

        assertEquals(PLAIN_SELECT_QUERY_WITH_IDS_INCLUDED_FILTERS, modifiedQuery);
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

        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier();
        String modifiedQuery = collectionQueryModifier.addIdBasedFilters(PLAIN_SELECT_QUERY_WITHOUT_WHERE,
                Arrays.asList(new Filter[]{idsExcludedFilter1, idsExcludedFilter2}), "person");

        assertEquals(PLAIN_SELECT_QUERY_WITH_IDS_EXCLUDED_FILTERS, modifiedQuery);
    }

}
