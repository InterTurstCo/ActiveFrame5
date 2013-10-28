package ru.intertrust.cm.core.dao.impl;

import org.junit.Test;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.model.base.Configuration;

import static org.junit.Assert.assertEquals;

public class SqlQueryModifierTest {

    private static final String ID_FIELD = "id";

    private static final String UNION_QUERY = "SELECT * FROM EMPLOYEE e, Department d where 1 = 1 and e.id = 1 " +
            "union SELECT * FROM EMPLOYEE e, Department d where 1 = 1 and e.id = 2";

    private static final String PLAIN_SELECT_QUERY = "SELECT * FROM EMPLOYEE e, Department d where 1 = 1 and e.id = 1";

    private static final String PLAIN_SELECT_QUERY_WITHOUT_WHERE = "SELECT * FROM EMPLOYEE e, Department d";

    private static final String PLAIN_SELECT_QUERY_WITHOUT_WHERE_ACL_APPLIED = "SELECT * FROM EMPLOYEE AS e, " +
            "Department AS d WHERE EXISTS " +
            "(SELECT r.object_id FROM EMPLOYEE_READ AS r INNER JOIN group_member AS gm ON r.group_id = gm.usergroup " +
            "WHERE gm.person_id = :user_id AND r.object_id = id)";

    private static final String PLAIN_SELECT_QUERY_WITH_TYPE = "SELECT * FROM " +
            "EMPLOYEE AS e, " +
            "Department AS d WHERE 1 = 1 AND e.id = 1";

    private static final String UNION_QUERY_WITH_TYPE = "(SELECT * FROM EMPLOYEE AS e, " +
            "Department AS d WHERE 1 = 1 AND e.id = 1) " +
            "UNION (SELECT * FROM EMPLOYEE AS e, Department AS d WHERE 1 = 1 " +
            "AND e.id = 2)";

    private static final String PLAIN_SELECT_QUERY_WITH_ACL = "SELECT * FROM EMPLOYEE AS e, " +
            "Department AS d WHERE EXISTS (SELECT r.object_id FROM EMPLOYEE_READ AS r INNER JOIN group_member AS gm " +
            "ON r.group_id = gm.usergroup WHERE gm.person_id = :user_id AND r.object_id = id) AND 1 = 1 AND e.id = 1";

    private static final String UNION_QUERY_WITH_ACL = "(SELECT * FROM EMPLOYEE AS e, Department AS d WHERE " +
            "EXISTS (SELECT r.object_id FROM EMPLOYEE_READ AS r INNER JOIN group_member AS gm ON " +
            "r.group_id = gm.usergroup WHERE gm.person_id = :user_id AND r.object_id = id) AND 1 = 1 AND e.id = 1) " +
            "UNION (SELECT * FROM EMPLOYEE AS e, Department AS d WHERE EXISTS (SELECT r.object_id " +
            "FROM EMPLOYEE_READ AS r INNER JOIN group_member AS gm ON r.group_id = gm.usergroup WHERE " +
            "gm.person_id = :user_id AND r.object_id = id) AND 1 = 1 AND e.id = 2)";

    @Test
    public void testAddTypeColumn() {
        ConfigurationExplorer configurationExplorer = new ConfigurationExplorerImpl(new Configuration());
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

}
