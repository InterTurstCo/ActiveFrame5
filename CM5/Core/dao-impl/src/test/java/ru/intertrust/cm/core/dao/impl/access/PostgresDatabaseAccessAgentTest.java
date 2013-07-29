package ru.intertrust.cm.core.dao.impl.access;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ru.intertrust.cm.core.business.api.dto.RdbmsId;

/**
 * 
 * @author atsvetkov
 *
 */
public class PostgresDatabaseAccessAgentTest {

    private static final String EMPLOYEE_DOMAIN_OBJECT_TYPE = "Employee";
    
    private static final String CHECK_DOMAIN_OBJECT_ACCESS_QUERY = "select count(*) from Employee_ACL a inner join " +
    		"group_member gm on a.group_id = gm.parent where gm.person_id = :user_id and a.object_id = :object_id " +
    		"and a.operation = :operation";

    private static final String CHECK_MULTI_DOMAIN_OBJECT_ACCESS_QUERY =
            "select a.object_id object_id from Employee_ACL " +
                    "a inner join group_member gm on a.group_id = gm.parent where gm.person_id = :user_id " +
                    "and a.object_id in (:object_ids) and a.operation = :operation";
    
    private static final String CHECK_DOMAIN_OBJECT_MULTI_ACCESS_QUERY =
            "select a.operation operation from Employee_ACL a " +
                    "inner join group_member gm on a.group_id = gm.parent where gm.person_id = :user_id " +
                    "and a.object_id = :object_id and a.operation in (:operations)";

    private PostgresDatabaseAccessAgent accessAgent = new PostgresDatabaseAccessAgent();
    
    private RdbmsId id;
    
    @Before
    public void setUp() throws Exception {
        id = new RdbmsId("Employee", 1);    
    }
    
    @Test
    public void testCheckDomainObjectAccess() {
        RdbmsId id = new RdbmsId("Employee", 1);
        String queryForCheckDomainObjectAccess = accessAgent.getQueryForCheckDomainObjectAccess(id);
        assertEquals(CHECK_DOMAIN_OBJECT_ACCESS_QUERY, queryForCheckDomainObjectAccess);
    }

    @Test
    public void testCheckMultiDomainObjectAccess() {
        String queryForCheckMultiDomainObjectAccess =
                accessAgent.getQueryForCheckMultiDomainObjectAccess(EMPLOYEE_DOMAIN_OBJECT_TYPE);
        assertEquals(CHECK_MULTI_DOMAIN_OBJECT_ACCESS_QUERY, queryForCheckMultiDomainObjectAccess);
    }

    @Test
    public void testCheckDomainObjectMultiAccess() {
        String queryForCheckDomainObjectMultiAccess = accessAgent.getQueryForCheckDomainObjectMultiAccess(id);
        assertEquals(CHECK_DOMAIN_OBJECT_MULTI_ACCESS_QUERY, queryForCheckDomainObjectMultiAccess);

    }
}
