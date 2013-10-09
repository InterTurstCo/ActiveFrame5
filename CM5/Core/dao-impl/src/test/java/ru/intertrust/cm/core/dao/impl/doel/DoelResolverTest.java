package ru.intertrust.cm.core.dao.impl.doel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.doel.DoelExpression;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DoelResolverTest {
    @InjectMocks
    private final DoelResolver doelResolver = new DoelResolver();
    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Mock
    private ConfigurationExplorer configurationExplorer;
    @Mock
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Test
    @SuppressWarnings("unchecked")
    public void testEvaluation() {
        when(domainObjectTypeIdCache.getName(1)).thenReturn("Employee");
        RdbmsId id = new RdbmsId(1, 1L);
        DoelExpression expr = DoelExpression.parse("Commission^parent.Job^parent.Assignee.Department");
        doelResolver.evaluate(expr, id);

        String correctSql =
                "select t4.department from internaldoc t1 " +
                "join commission t2 on t1.id=t2.parent " +
                "join job t3 on t2.id=t3.parent " +
                "join person t4 on t3.assignee=t4.id " +
                "where t1.id=:id";
//        verify(jdbcTemplate).queryForList(argThat(new SqlStatementMatcher(correctSql)), anyMap());
    }

    @Test
    public void testReverseExpression() {
        DoelExpression expr = DoelExpression.parse("Commission^parent.Job^parent.Assignee.Department");
        DoelExpression exprBack = doelResolver.createReverseExpression(expr, "InternalDoc");
        assertEquals(DoelExpression.parse("Person^Department.Job^Assignee.parent.parent"), exprBack);
    }

    @Test
    public void testPartialReverseExpression() {
        DoelExpression expr = DoelExpression.parse("Commission^parent.Job^parent.Assignee.Department");
        DoelExpression exprBack = doelResolver.createReverseExpression(expr, 2, "InternalDoc");
        assertEquals(DoelExpression.parse("parent.parent"), exprBack);
    }

    @Before
    public void prepareConfiguration() {
        ReferenceFieldConfig jobAssigneeConfig = new ReferenceFieldConfig();
        jobAssigneeConfig.setName("Assignee");
        jobAssigneeConfig.setType("Person");
        when(configurationExplorer.getFieldConfig("Job", "Assignee")).thenReturn(jobAssigneeConfig);

        ReferenceFieldConfig personDepartmentConfig = new ReferenceFieldConfig();
        personDepartmentConfig.setName("Department");
        personDepartmentConfig.setType("Department");
        when(configurationExplorer.getFieldConfig("Person", "Department")).thenReturn(personDepartmentConfig);
    }
}
