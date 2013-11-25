package ru.intertrust.cm.core.dao.impl.doel;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DomainObjectCacheServiceImpl;
import ru.intertrust.cm.core.dao.impl.SqlStatementMatcher;
import ru.intertrust.cm.core.util.SpringApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
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
    @Mock
    private DomainObjectCacheServiceImpl domainObjectCacheService;
    @Mock
    private ApplicationContext context;

    RdbmsId docId = new RdbmsId(1, 105L);
    RdbmsId comm1Id = new RdbmsId(2, 11L);
    RdbmsId comm2Id = new RdbmsId(2, 12L);

    @Test
    @SuppressWarnings("unchecked")
    public void testEvaluation() {
        DoelExpression expr = DoelExpression.parse("Commission^parent.Job^parent.Assignee.Department");
        doelResolver.evaluate(expr, docId);

        String correctSql =
                "select t2.department, t2.department_type " +
                "from commission as t0 " +
                "join job as t1 on t0.id = t1.parent " +
                "join person as t2 on t1.assignee = t2.id " +
                "where t0.parent = 105";
        verify(jdbcTemplate).query(argThat(new SqlStatementMatcher(correctSql)), any(RowMapper.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEvaluationWithCache() {
        GenericDomainObject comm1 = new GenericDomainObject();
        comm1.setId(comm1Id);
        GenericDomainObject comm2 = new GenericDomainObject();
        comm2.setId(comm2Id);
        when(domainObjectCacheService.getObjectToCache(docId, "Commission", "parent", "0", "0"))
                .thenReturn(Arrays.asList((DomainObject) comm1, comm2));

        DoelExpression expr = DoelExpression.parse("Commission^parent.Job^parent.Assignee.Department");
        doelResolver.evaluate(expr, docId);

        String correctSql =
                "select t1.department, t1.department_type " +
                "from job as t0 " +
                "join person as t1 on t0.assignee = t1.id " +
                "where t0.parent in (11, 12)";
        verify(jdbcTemplate).query(argThat(new SqlStatementMatcher(correctSql)), any(RowMapper.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEvaluationWithInheritance() {

        DoelExpression expr = DoelExpression.parse("Commission^parent.Job^parent.Assignee.Department.Name");
        doelResolver.evaluate(expr, docId);

        String correctSql =
                "select t3.name " +
                "from commission as t0 " +
                "join job as t1 on t0.id = t1.parent " +
                "join person as t2 on t1.assignee = t2.id " +
                "join unit as t3 on t2.department = t3.id " +
                "where t0.parent = 105";
        verify(jdbcTemplate).query(argThat(new SqlStatementMatcher(correctSql)), any(RowMapper.class));
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
        new SpringApplicationContext().setApplicationContext(context);
        when(context.getBean(ConfigurationExplorer.class)).thenReturn(configurationExplorer);

        when(domainObjectTypeIdCache.getName(docId)).thenReturn("Document");
        when(domainObjectCacheService.getObjectToCache(any(Id.class), Matchers.<String>anyVararg())).thenReturn(null);

        DomainObjectTypeConfig commissionConfig = new DomainObjectTypeConfig();
        commissionConfig.setName("Commission");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Commission")).thenReturn(commissionConfig);

        DomainObjectTypeConfig jobConfig = new DomainObjectTypeConfig();
        jobConfig.setName("Job");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Job")).thenReturn(jobConfig);

        DomainObjectTypeConfig personConfig = new DomainObjectTypeConfig();
        personConfig.setName("Person");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Person")).thenReturn(personConfig);

        DomainObjectTypeConfig unitConfig = new DomainObjectTypeConfig();
        unitConfig.setName("Unit");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Unit")).thenReturn(unitConfig);

        DomainObjectTypeConfig departmentConfig = new DomainObjectTypeConfig();
        departmentConfig.setName("Department");
        departmentConfig.setExtendsAttribute("Unit");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Department")).thenReturn(departmentConfig);

        ReferenceFieldConfig commissionParentConfig = new ReferenceFieldConfig();
        commissionParentConfig.setName("parent");
        commissionParentConfig.setType("Document");
        when(configurationExplorer.getFieldConfig("Commission", "parent")).thenReturn(commissionParentConfig);
        when(configurationExplorer.getFieldConfig("Commission", "parent", false)).thenReturn(commissionParentConfig);

        ReferenceFieldConfig jobParentConfig = new ReferenceFieldConfig();
        jobParentConfig.setName("parent");
        jobParentConfig.setType("Commission");
        when(configurationExplorer.getFieldConfig("Job", "parent")).thenReturn(jobParentConfig);
        when(configurationExplorer.getFieldConfig("Job", "parent", false)).thenReturn(jobParentConfig);

        ReferenceFieldConfig jobAssigneeConfig = new ReferenceFieldConfig();
        jobAssigneeConfig.setName("Assignee");
        jobAssigneeConfig.setType("Person");
        when(configurationExplorer.getFieldConfig("Job", "Assignee")).thenReturn(jobAssigneeConfig);
        when(configurationExplorer.getFieldConfig("Job", "Assignee", false)).thenReturn(jobAssigneeConfig);

        ReferenceFieldConfig personDepartmentConfig = new ReferenceFieldConfig();
        personDepartmentConfig.setName("Department");
        personDepartmentConfig.setType("Department");
        when(configurationExplorer.getFieldConfig("Person", "Department")).thenReturn(personDepartmentConfig);
        when(configurationExplorer.getFieldConfig("Person", "Department", false)).thenReturn(personDepartmentConfig);

        StringFieldConfig unitNameConfig = new StringFieldConfig();
        unitNameConfig.setName("Name");
        when(configurationExplorer.getFieldConfig("Unit", "Name")).thenReturn(unitNameConfig);
        when(configurationExplorer.getFieldConfig("Unit", "Name", false)).thenReturn(unitNameConfig);
        when(configurationExplorer.getFieldConfig("Department", "Name")).thenReturn(unitNameConfig);
        when(configurationExplorer.getFieldConfig("Department", "Name", false)).thenReturn(null);
    }
}
