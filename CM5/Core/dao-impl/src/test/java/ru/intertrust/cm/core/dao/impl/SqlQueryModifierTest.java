package ru.intertrust.cm.core.dao.impl;

import org.junit.Test;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.base.Configuration;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class SqlQueryModifierTest {

    private static final String ID_FIELD = "id";

    private static final String UNION_QUERY = "SELECT * FROM EMPLOYEE e, Department d where 1 = 1 and e.id = 1 " +
            "union SELECT * FROM EMPLOYEE e, Department d where 1 = 1 and e.id = 2";

    private static final String PLAIN_SELECT_QUERY = "SELECT * FROM EMPLOYEE e, Department d where 1 = 1 and e.id = 1";

    private static final String PLAIN_SELECT_QUERY_WITHOUT_WHERE = "SELECT * FROM EMPLOYEE e, Department d";

    private static final String PLAIN_SELECT_QUERY_WITHOUT_WHERE_ACL_APPLIED = "SELECT * FROM EMPLOYEE AS e, " +
            "Department AS d WHERE EXISTS " +
            "(SELECT r.object_id FROM EMPLOYEE_read AS r INNER JOIN group_member AS gm ON r.group_id = gm.usergroup " +
            "WHERE gm.person_id = :user_id AND r.object_id = id)";

    private static final String PLAIN_SELECT_QUERY_WITH_TYPE = "SELECT * FROM " +
            "EMPLOYEE AS e, " +
            "Department AS d WHERE 1 = 1 AND e.id = 1";

    private static final String PLAIN_SELECT_QUERY_WITH_IDS_INCLUDED_FILTERS = "SELECT * FROM EMPLOYEE AS e, " +
            "Department AS d WHERE 1 = 1 AND (EMPLOYEE.id = :idsIncluded10 AND EMPLOYEE.type_id = :idsIncluded10_type) " +
            "AND ((EMPLOYEE.id = :idsIncluded20 AND EMPLOYEE.type_id = :idsIncluded20_type) OR " +
            "(EMPLOYEE.id = :idsIncluded21 AND EMPLOYEE.type_id = :idsIncluded21_type))";

    private static final String PLAIN_SELECT_QUERY_WITH_IDS_EXCLUDED_FILTERS = "SELECT * FROM EMPLOYEE AS e, " +
            "Department AS d WHERE 1 = 1 AND (EMPLOYEE.person <> :idsExcluded10 AND EMPLOYEE.person_type <> :idsExcluded10_type) " +
            "AND ((EMPLOYEE.person <> :idsExcluded20 AND EMPLOYEE.person_type <> :idsExcluded20_type) AND " +
            "(EMPLOYEE.person <> :idsExcluded21 AND EMPLOYEE.person_type <> :idsExcluded21_type))";

    private static final String UNION_QUERY_WITH_TYPE = "(SELECT * FROM EMPLOYEE AS e, " +
            "Department AS d WHERE 1 = 1 AND e.id = 1) " +
            "UNION (SELECT * FROM EMPLOYEE AS e, Department AS d WHERE 1 = 1 " +
            "AND e.id = 2)";

    private static final String PLAIN_SELECT_QUERY_WITH_ACL = "SELECT * FROM EMPLOYEE AS e, " +
            "Department AS d WHERE EXISTS (SELECT r.object_id FROM EMPLOYEE_read AS r INNER JOIN group_member AS gm " +
            "ON r.group_id = gm.usergroup WHERE gm.person_id = :user_id AND r.object_id = id) AND 1 = 1 AND e.id = 1";

    private static final String UNION_QUERY_WITH_ACL = "(SELECT * FROM EMPLOYEE AS e, Department AS d WHERE " +
            "EXISTS (SELECT r.object_id FROM EMPLOYEE_read AS r INNER JOIN group_member AS gm ON " +
            "r.group_id = gm.usergroup WHERE gm.person_id = :user_id AND r.object_id = id) AND 1 = 1 AND e.id = 1) " +
            "UNION (SELECT * FROM EMPLOYEE AS e, Department AS d WHERE EXISTS (SELECT r.object_id " +
            "FROM EMPLOYEE_read AS r INNER JOIN group_member AS gm ON r.group_id = gm.usergroup WHERE " +
            "gm.person_id = :user_id AND r.object_id = id) AND 1 = 1 AND e.id = 2)";

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
        SqlQueryModifier collectionQueryModifier = new SqlQueryModifier();

        IdsIncludedFilter idsIncludedFilter1 = new IdsIncludedFilter();
        idsIncludedFilter1.setFilter("idsIncluded1");
        idsIncludedFilter1.addCriterion(0, new ReferenceValue(new RdbmsId(1, 100)));

        IdsIncludedFilter idsIncludedFilter2 = new IdsIncludedFilter();
        idsIncludedFilter2.setFilter("idsIncluded2");
        idsIncludedFilter2.addCriterion(0, new ReferenceValue(new RdbmsId(1, 101)));
        idsIncludedFilter2.addCriterion(1, new ReferenceValue(new RdbmsId(1, 102)));

        String modifiedQuery = collectionQueryModifier.addIdBasedFilters(PLAIN_SELECT_QUERY_WITHOUT_WHERE,
                Arrays.asList(new Filter[] {idsIncludedFilter1, idsIncludedFilter2}), "id");

        assertEquals(PLAIN_SELECT_QUERY_WITH_IDS_INCLUDED_FILTERS, modifiedQuery);

        IdsExcludedFilter idsExcludedFilter1 = new IdsExcludedFilter();
        idsExcludedFilter1.setFilter("idsExcluded1");
        idsExcludedFilter1.addCriterion(0, new ReferenceValue(new RdbmsId(1, 100)));

        IdsExcludedFilter idsExcludedFilter2 = new IdsExcludedFilter();
        idsExcludedFilter2.setFilter("idsExcluded2");
        idsExcludedFilter2.addCriterion(0, new ReferenceValue(new RdbmsId(1, 101)));
        idsExcludedFilter2.addCriterion(1, new ReferenceValue(new RdbmsId(1, 102)));

        modifiedQuery = collectionQueryModifier.addIdBasedFilters(PLAIN_SELECT_QUERY_WITHOUT_WHERE,
                Arrays.asList(new Filter[]{idsExcludedFilter1, idsExcludedFilter2}), "person");

        assertEquals(PLAIN_SELECT_QUERY_WITH_IDS_EXCLUDED_FILTERS, modifiedQuery);
    }

}
