package ru.intertrust.cm.core.dao.impl.doel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DomainObjectCacheServiceImpl;
import ru.intertrust.cm.core.dao.impl.SqlStatementMatcher;
import ru.intertrust.cm.core.util.SpringApplicationContext;

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
    RdbmsId linkId = new RdbmsId(10, 1514L);

    @Test
    @SuppressWarnings("unchecked")
    public void testEvaluation() {
        DoelExpression expr = DoelExpression.parse("Commission^parent.Job^parent.Assignee.Department");
        doelResolver.evaluate(expr, docId);

        String correctSql =
                "select t2.\"department\", t2.\"department_type\" " +
                "from \"commission\" t0 " +
                "join \"job\" t1 on t0.\"id\" = t1.\"parent\" " +
                "join \"person\" t2 on t1.\"assignee\" = t2.\"id\" " +
                "where t0.\"parent\" = 105";
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
                "select t1.\"department\", t1.\"department_type\" " +
                "from \"job\" t0 " +
                "join \"person\" t1 on t0.\"assignee\" = t1.\"id\" " +
                "where t0.\"parent\" in (11, 12)";
        verify(jdbcTemplate).query(argThat(new SqlStatementMatcher(correctSql)), any(RowMapper.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEvaluationWithInheritedField() {
        DoelExpression expr = DoelExpression.parse("Commission^parent.Job^parent.Assignee.Department.Name");
        doelResolver.evaluate(expr, docId);

        String correctSql =
                "select t3.\"name\" " +
                "from \"commission\" t0 " +
                "join \"job\" t1 on t0.\"id\" = t1.\"parent\" " +
                "join \"person\" t2 on t1.\"assignee\" = t2.\"id\" " +
                "join \"unit\" t3 on t2.\"department\" = t3.\"id\" " +
                "where t0.\"parent\" = 105";
        verify(jdbcTemplate).query(argThat(new SqlStatementMatcher(correctSql)), any(RowMapper.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEvaluationWithExtendedField() {
        DoelExpression expr = DoelExpression.parse("parent.Addressee.Name");
        doelResolver.evaluate(expr, comm1Id);

        String correctSql =
                "select t2.\"name\" " +
                "from \"commission\" t0 " +
                "join \"incomingdocument\" t1 on t0.\"parent\" = t1.\"id\" " +
                "join \"person\" t2 on t1.\"addressee\" = t2.\"id\" " +
                "where t0.\"id\" = 11";
        verify(jdbcTemplate).query(argThat(new SqlStatementMatcher(correctSql)), any(RowMapper.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEvaluationWithWildcardReference() {
        DoelExpression expr = DoelExpression.parse("from.Name");
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(
                Arrays.asList(new ReferenceValue(docId), new ReferenceValue(comm1Id)),
                Collections.emptyList());
        doelResolver.evaluate(expr, linkId);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        String correctSql1 =
                "select t0.\"from\", t0.\"from_type\" " +
                "from \"universallink\" t0 " +
                "where t0.\"id\" = 1514";
        String correctSql2 =
                "select t0.\"name\" " +
                "from \"document\" t0 " +
                "where t0.\"id\" = " + docId.getId();
        //verify(jdbcTemplate, times(2)).query(argThat(new SqlStatementMatcher(correctSql)), any(RowMapper.class));
        verify(jdbcTemplate, times(2)).query(sql.capture(), any(RowMapper.class));
        assertThat(sql.getAllValues().get(0), new SqlStatementMatcher(correctSql1));
        assertThat(sql.getAllValues().get(1), new SqlStatementMatcher(correctSql2));
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
        when(domainObjectTypeIdCache.getName(comm1Id)).thenReturn("Commission");
        when(domainObjectTypeIdCache.getName(comm2Id)).thenReturn("Commission");
        when(domainObjectTypeIdCache.getName(linkId)).thenReturn("UniversalLink");
        when(domainObjectCacheService.getObjectToCache(any(Id.class), Matchers.<String>anyVararg())).thenReturn(null);

        // Объекты ====================
        DomainObjectTypeConfig documentConfig = new DomainObjectTypeConfig();
        documentConfig.setName("Document");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Document")).thenReturn(documentConfig);
        when(configurationExplorer.getDomainObjectRootType("Document")).thenReturn("Document");

        DomainObjectTypeConfig incomingDocConfig = new DomainObjectTypeConfig();
        incomingDocConfig.setName("IncomingDocument");
        incomingDocConfig.setExtendsAttribute("Document");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "IncomingDocument")).thenReturn(incomingDocConfig);
        when(configurationExplorer.getDomainObjectRootType("IncomingDocument")).thenReturn("Document");
        when(configurationExplorer.findChildDomainObjectTypes("Document", false)).thenReturn(Arrays.asList(incomingDocConfig));

        DomainObjectTypeConfig commissionConfig = new DomainObjectTypeConfig();
        commissionConfig.setName("Commission");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Commission")).thenReturn(commissionConfig);
        when(configurationExplorer.getDomainObjectRootType("Commission")).thenReturn("Commission");

        DomainObjectTypeConfig jobConfig = new DomainObjectTypeConfig();
        jobConfig.setName("Job");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Job")).thenReturn(jobConfig);
        when(configurationExplorer.getDomainObjectRootType("Job")).thenReturn("Job");

        DomainObjectTypeConfig personConfig = new DomainObjectTypeConfig();
        personConfig.setName("Person");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Person")).thenReturn(personConfig);
        when(configurationExplorer.getDomainObjectRootType("Person")).thenReturn("Person");

        DomainObjectTypeConfig unitConfig = new DomainObjectTypeConfig();
        unitConfig.setName("Unit");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Unit")).thenReturn(unitConfig);
        when(configurationExplorer.getDomainObjectRootType("Unit")).thenReturn("Unit");

        DomainObjectTypeConfig departmentConfig = new DomainObjectTypeConfig();
        departmentConfig.setName("Department");
        departmentConfig.setExtendsAttribute("Unit");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Department")).thenReturn(departmentConfig);
        when(configurationExplorer.getDomainObjectRootType("Department")).thenReturn("Unit");
        when(configurationExplorer.findChildDomainObjectTypes("Unit", false)).thenReturn(Arrays.asList(departmentConfig));

        DomainObjectTypeConfig universalLinkConfig = new DomainObjectTypeConfig();
        universalLinkConfig.setName("UniversalLink");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "UniversalLink")).thenReturn(universalLinkConfig);
        when(configurationExplorer.getDomainObjectRootType("UniversalLink")).thenReturn("UniversalLink");

        when(configurationExplorer.getConfigs(DomainObjectTypeConfig.class)).thenReturn(Arrays.asList(
                documentConfig, incomingDocConfig, commissionConfig, jobConfig,
                personConfig, unitConfig, departmentConfig, universalLinkConfig));

        // Поля =======================
        ReferenceFieldConfig documentAddresseeConfig = new ReferenceFieldConfig();
        documentAddresseeConfig.setName("Addressee");
        documentAddresseeConfig.setType("Person");
        when(configurationExplorer.getFieldConfig("IncomingDocument", "Addressee")).thenReturn(documentAddresseeConfig);
        when(configurationExplorer.getFieldConfig("IncomingDocument", "Addressee", false)).thenReturn(documentAddresseeConfig);

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

        ReferenceFieldConfig linkFromConfig = new ReferenceFieldConfig();
        linkFromConfig.setName("from");
        linkFromConfig.setType("*");
        when(configurationExplorer.getFieldConfig("UniversalLink", "from")).thenReturn(linkFromConfig);
        when(configurationExplorer.getFieldConfig("UniversalLink", "from", false)).thenReturn(linkFromConfig);

        ReferenceFieldConfig linkToConfig = new ReferenceFieldConfig();
        linkToConfig.setName("to");
        linkToConfig.setType("*");
        when(configurationExplorer.getFieldConfig("UniversalLink", "to")).thenReturn(linkToConfig);
        when(configurationExplorer.getFieldConfig("UniversalLink", "to", false)).thenReturn(linkToConfig);

        StringFieldConfig nameFieldConfig = new StringFieldConfig();
        nameFieldConfig.setName("Name");
        when(configurationExplorer.getFieldConfig("Document", "Name")).thenReturn(nameFieldConfig);
        when(configurationExplorer.getFieldConfig("Document", "Name", false)).thenReturn(nameFieldConfig);
        when(configurationExplorer.getFieldConfig("IncomingDocument", "Name")).thenReturn(nameFieldConfig);
        when(configurationExplorer.getFieldConfig("IncomingDocument", "Name", false)).thenReturn(null);
        when(configurationExplorer.getFieldConfig("Person", "Name")).thenReturn(nameFieldConfig);
        when(configurationExplorer.getFieldConfig("Person", "Name", false)).thenReturn(nameFieldConfig);
        when(configurationExplorer.getFieldConfig("Unit", "Name")).thenReturn(nameFieldConfig);
        when(configurationExplorer.getFieldConfig("Unit", "Name", false)).thenReturn(nameFieldConfig);
        when(configurationExplorer.getFieldConfig("Department", "Name")).thenReturn(nameFieldConfig);
        when(configurationExplorer.getFieldConfig("Department", "Name", false)).thenReturn(null);
    }
}
