package ru.intertrust.cm.core.dao.impl.access;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.dao.access.AccessType;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
/**
 *
 * @author atsvetkov
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PostgresDatabaseAccessAgentTest {

    private static final String CHECK_DOMAIN_OBJECT_ACCESS_QUERY = "select count(*) from Employee_ACL a inner join " +
    		"group_member gm on a.group_id = gm.master where gm.person_id = :user_id and a.object_id = :object_id " +
    		"and a.operation = :operation";

    private static final String CHECK_MULTI_DOMAIN_OBJECT_ACCESS_FOR_EMPLOYEE_QUERY =
            "select a.object_id object_id from Employee_ACL " +
                    "a inner join group_member gm on a.group_id = gm.master where gm.person_id = :user_id " +
                    "and a.object_id in (:object_ids) and a.operation = :operation";

    private static final String CHECK_MULTI_DOMAIN_OBJECT_ACCESS_FOR_DEPARTMENT_QUERY =
            "select a.object_id object_id from Department_ACL a inner join group_member gm on a.group_id = gm.master " +
                    "where gm.person_id = :user_id and a.object_id in (:object_ids) and a.operation = :operation";

    private static final String CHECK_DOMAIN_OBJECT_MULTI_ACCESS_QUERY =
            "select a.operation operation from Employee_ACL a " +
                    "inner join group_member gm on a.group_id = gm.master where gm.person_id = :user_id " +
                    "and a.object_id = :object_id and a.operation in (:operations)";

    @InjectMocks
    private final PostgresDatabaseAccessAgent accessAgent = new PostgresDatabaseAccessAgent();

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Mock
    private DomainObjectTypeIdCache domainObjetcTypeIdCache;

    private RdbmsId employeeId;
    private RdbmsId departmentId;

    @Before
    public void setUp() throws Exception {
        employeeId = new RdbmsId(1, 1);
        departmentId = new RdbmsId(2, 1);

    }

    @Test
    public void testCheckDomainObjectAccess() {
        when(jdbcTemplate.queryForObject(eq(CHECK_DOMAIN_OBJECT_ACCESS_QUERY), anyMapOf(String.class, Object.class), eq(Integer.class))).thenReturn(1);
        when(domainObjetcTypeIdCache.getName(1)).thenReturn("Employee");
        boolean result = accessAgent.checkDomainObjectAccess(1, employeeId, DomainObjectAccessType.WRITE);
        verify(jdbcTemplate, times(1)).queryForObject(eq(CHECK_DOMAIN_OBJECT_ACCESS_QUERY), anyMapOf(String.class, Object.class), eq(Integer.class));
        assertEquals(result, true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCheckMultiDomainObjectAccess() {
        List<Id> employeeIds = new ArrayList<Id>();
        employeeIds.add(employeeId);
        List<Id> departmentIds = new ArrayList<Id>();
        employeeIds.add(departmentId);

        when(jdbcTemplate.query(eq(CHECK_MULTI_DOMAIN_OBJECT_ACCESS_FOR_EMPLOYEE_QUERY),
                anyMapOf(String.class, Object.class), any(RowMapper.class))).thenReturn(employeeIds);
        when(jdbcTemplate.query(eq(CHECK_MULTI_DOMAIN_OBJECT_ACCESS_FOR_DEPARTMENT_QUERY),
                anyMapOf(String.class, Object.class), any(RowMapper.class))).thenReturn(departmentIds);
        when(domainObjetcTypeIdCache.getName(1)).thenReturn("Employee");
        when(domainObjetcTypeIdCache.getName(2)).thenReturn("Department");

        RdbmsId[] inputIds = new RdbmsId[2];
        inputIds[0] = employeeId;
        inputIds[1] = departmentId;
        Id[] idsWithAllowedAccess = accessAgent.checkMultiDomainObjectAccess(1, inputIds, DomainObjectAccessType.WRITE);
        verify(jdbcTemplate, times(1)).query(eq(CHECK_MULTI_DOMAIN_OBJECT_ACCESS_FOR_EMPLOYEE_QUERY),
                anyMapOf(String.class, Object.class), any(RowMapper.class));
        verify(jdbcTemplate, times(1)).query(eq(CHECK_MULTI_DOMAIN_OBJECT_ACCESS_FOR_DEPARTMENT_QUERY),
                anyMapOf(String.class, Object.class), any(RowMapper.class));

        assertTrue(Arrays.asList(idsWithAllowedAccess).contains(employeeId));
        assertTrue(Arrays.asList(idsWithAllowedAccess).contains(departmentId));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCheckDomainObjectMultiAccess() {
        List<AccessType> accessTypesToReturn = new ArrayList<AccessType>();
        accessTypesToReturn.add(DomainObjectAccessType.WRITE);
        accessTypesToReturn.add(DomainObjectAccessType.DELETE);

        when(jdbcTemplate.query(eq(CHECK_DOMAIN_OBJECT_MULTI_ACCESS_QUERY), anyMapOf(String.class, Object.class),
                any(RowMapper.class))).thenReturn(accessTypesToReturn);
        when(domainObjetcTypeIdCache.getName(1)).thenReturn("Employee");
        AccessType[] types = new AccessType[2];
        types[0] = DomainObjectAccessType.WRITE;
        types[1] = DomainObjectAccessType.DELETE;
        AccessType[] allowedAccessTypes = accessAgent.checkDomainObjectMultiAccess(1, employeeId, types);

        verify(jdbcTemplate, times(1)).query(eq(CHECK_DOMAIN_OBJECT_MULTI_ACCESS_QUERY),
                anyMapOf(String.class, Object.class), any(RowMapper.class));

        assertTrue(Arrays.asList(allowedAccessTypes).contains(DomainObjectAccessType.WRITE));
        assertTrue(Arrays.asList(allowedAccessTypes).contains(DomainObjectAccessType.DELETE));
    }
}
