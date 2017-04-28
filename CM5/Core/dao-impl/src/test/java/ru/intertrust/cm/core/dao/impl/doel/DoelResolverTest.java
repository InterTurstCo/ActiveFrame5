package ru.intertrust.cm.core.dao.impl.doel;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.doel.AnnotationFunctionValidator;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.config.doel.DoelFunction;
import ru.intertrust.cm.core.config.doel.DoelFunctionRegistry;
import ru.intertrust.cm.core.config.doel.DoelFunctionValidator;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.dao.impl.DomainObjectCacheServiceImpl;
import ru.intertrust.cm.core.dao.impl.SqlStatementMatcher;
import ru.intertrust.cm.core.dao.impl.access.AccessControlServiceImpl;
import ru.intertrust.cm.core.util.SpringApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class DoelResolverTest {
    @InjectMocks
    private final DoelResolver doelResolver = new DoelResolver();
    @Mock
    private CollectionsDao collectionsDao;
    @Mock
    private ConfigurationExplorer configurationExplorer;
    @Mock
    private DomainObjectTypeIdCache domainObjectTypeIdCache;
    @Mock
    private DomainObjectCacheServiceImpl domainObjectCacheService;
    @Mock
    private GlobalCacheClient globalCacheClient;
    @Mock
    private DoelFunctionRegistry doelFunctionRegistry;
    @Mock
    private ApplicationContext context;

    private AccessControlService accessControlService = new AccessControlServiceImpl();

    RdbmsId docId = new RdbmsId(1, 105L);
    RdbmsId comm1Id = new RdbmsId(2, 11L);
    RdbmsId comm2Id = new RdbmsId(2, 12L);
    RdbmsId linkId = new RdbmsId(10, 1514L);

    @Before
    public void setUp(){
        if (doelResolver.getAccessControlService() == null) {
            doelResolver.setAccessControlService(accessControlService);
        }
    }

    @Test
    public void testEvaluation() {
        DoelExpression expr = DoelExpression.parse("Commission^parent.Job^parent.Assignee.Department");
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        when(globalCacheClient.getLinkedDomainObjects(any(Id.class), anyString(), anyString(), anyBoolean(), any(AccessToken.class))).thenReturn(null);

        doelResolver.evaluate(expr, docId, accessToken);

        String correctSql =
                "select t2.\"department\", t2.\"department_type\" " +
                "from \"commission\" t0 " +
                "join \"job\" t1 on t0.\"id\" = t1.\"parent\" " +
                "join \"person\" t2 on t1.\"assignee\" = t2.\"id\" " +
                "where t0.\"parent\" = 105";
        verify(collectionsDao).findCollectionByQuery(argThat(new SqlStatementMatcher(correctSql)), any(Integer.class),
                any(Integer.class), any(AccessToken.class));
    }

    @Test
    public void testEvaluationWithCache() {
        GenericDomainObject comm1 = new GenericDomainObject();
        comm1.setId(comm1Id);
        GenericDomainObject comm2 = new GenericDomainObject();
        comm2.setId(comm2Id);

        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        when(domainObjectCacheService.getAll(docId, accessToken, "Commission", "parent"))
                .thenReturn(Arrays.asList((DomainObject) comm1, comm2));

        DoelExpression expr = DoelExpression.parse("Commission^parent.Job^parent.Assignee.Department");

        when(globalCacheClient.getLinkedDomainObjects(any(Id.class), anyString(), anyString(), anyBoolean(), any(AccessToken.class))).thenReturn(null);

        doelResolver.evaluate(expr, docId, accessToken);

        String correctSql =
                "select t1.\"department\", t1.\"department_type\" " +
                "from \"job\" t0 " +
                "join \"person\" t1 on t0.\"assignee\" = t1.\"id\" " +
                "where t0.\"parent\" in (11, 12)";
        verify(collectionsDao).findCollectionByQuery(argThat(new SqlStatementMatcher(correctSql)), any(Integer.class),
                any(Integer.class), any(AccessToken.class));
    }

    @Test
    public void testEvaluationWithInheritedField() {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        DoelExpression expr = DoelExpression.parse("Commission^parent.Job^parent.Assignee.Department.Name");

        when(globalCacheClient.getLinkedDomainObjects(any(Id.class), anyString(), anyString(), anyBoolean(), any(AccessToken.class))).thenReturn(null);

        doelResolver.evaluate(expr, docId, accessToken);

        String correctSql =
                "select t3.\"name\" " +
                "from \"commission\" t0 " +
                "join \"job\" t1 on t0.\"id\" = t1.\"parent\" " +
                "join \"person\" t2 on t1.\"assignee\" = t2.\"id\" " +
                "join \"unit\" t3 on t2.\"department\" = t3.\"id\" " +
                "where t0.\"parent\" = 105";
        verify(collectionsDao).findCollectionByQuery(argThat(new SqlStatementMatcher(correctSql)), any(Integer.class),
                any(Integer.class), any(AccessToken.class));
    }

    @Test
    public void testEvaluationWithExtendedField() {
        DoelExpression expr = DoelExpression.parse("parent.Addressee.Name");
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        doelResolver.evaluate(expr, comm1Id, accessToken);

        String correctSql =
                "select t2.\"name\" " +
                "from \"commission\" t0 " +
                "join \"incomingdocument\" t1 on t0.\"parent\" = t1.\"id\" " +
                "join \"person\" t2 on t1.\"addressee\" = t2.\"id\" " +
                "where t0.\"id\" = 11";
        verify(collectionsDao).findCollectionByQuery(argThat(new SqlStatementMatcher(correctSql)), any(Integer.class),
                any(Integer.class), any(AccessToken.class));
    }

    @Test
    public void testEvaluationWithWildcardReference() {
        DoelExpression expr = DoelExpression.parse("from.Name");

        IdentifiableObjectCollection collectionValues = new GenericIdentifiableObjectCollection();
        List<FieldConfig> collectionFieldConfigs = new ArrayList<>(1);
        collectionFieldConfigs.add(configurationExplorer.getFieldConfig("IncomingDocument", "Addressee"));
        collectionValues.setFieldsConfiguration(collectionFieldConfigs);

        collectionValues.set(0, 0, new ReferenceValue(docId));
        collectionValues.set(0, 1, new ReferenceValue(comm1Id));

        when(collectionsDao.findCollectionByQuery(anyString(), any(Integer.class), any(Integer.class),
                any(AccessToken.class))).thenReturn(collectionValues);

        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        doelResolver.evaluate(expr, linkId, accessToken);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        String correctSql1 =
                "select t0.\"from\", t0.\"from_type\" " +
                "from \"universallink\" t0 " +
                "where t0.\"id\" = " + linkId.getId();
        String correctSql2 =
                "select t0.\"name\" " +
                "from \"document\" t0 " +
                "where t0.\"id\" = " + docId.getId();
        //verify(jdbcTemplate).query(argThat(new SqlStatementMatcher(correctSql1)), any(RowMapper.class));
        verify(collectionsDao, times(2)).findCollectionByQuery(sql.capture(), any(Integer.class),
                any(Integer.class), any(AccessToken.class));
        assertThat(sql.getAllValues().get(0), new SqlStatementMatcher(correctSql1));
        assertThat(sql.getAllValues().get(1), new SqlStatementMatcher(correctSql2));
    }

    @Test
    public void testEvaluationWithReverseLinkAfterWildcardReference() {
        DoelExpression expr = DoelExpression.parse("from.Commission^parent");

        /*IdentifiableObjectCollection collectionValues = new GenericIdentifiableObjectCollection();
        List<FieldConfig> collectionFieldConfigs = new ArrayList<>(1);
        collectionFieldConfigs.add(configurationExplorer.getFieldConfig("IncomingDocument", "Addressee"));
        collectionValues.setFieldsConfiguration(collectionFieldConfigs);

        collectionValues.set(0, 0, new ReferenceValue(docId));
        collectionValues.set(0, 1, new ReferenceValue(comm1Id));

        when(collectionsDao.findCollectionByQuery(anyString(), any(Integer.class), any(Integer.class),
                any(AccessToken.class))).thenReturn(collectionValues);*/

        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        doelResolver.evaluate(expr, linkId, accessToken);

        String correctSql =
                "select t1.\"id\", t1.\"id_type\" " +
                "from \"universallink\" t0 " +
                "join \"commission\" t1 on t0.\"from\" = t1.\"parent\" " +
                "where t0.\"id\" = " + linkId.getId();
        verify(collectionsDao).findCollectionByQuery(argThat(new SqlStatementMatcher(correctSql)), any(Integer.class),
                any(Integer.class), any(AccessToken.class));
    }

    @DoelFunction(name = "status", requiredParams = 1, optionalParams = 999, contextTypes = { FieldType.REFERENCE })
    private static class TestStatusFunction { }

    @Test
    @SuppressWarnings("unchecked")
    public void testEvaluationWithFunction() {
        DoelExpression expr = DoelExpression.parse("Commission^parent:Status(Assigned,Executing).Job^parent.Assignee");

        IdentifiableObjectCollection collectionValues = new GenericIdentifiableObjectCollection();
        List<FieldConfig> collectionFieldConfigs = new ArrayList<>(1);
        collectionFieldConfigs.add(configurationExplorer.getFieldConfig("IncomingDocument", "Addressee"));
        collectionValues.setFieldsConfiguration(collectionFieldConfigs);

        collectionValues.set(0, 0, new ReferenceValue(comm1Id));
        collectionValues.set(0, 1, new ReferenceValue(comm2Id));

        when(collectionsDao.findCollectionByQuery(anyString(), any(Integer.class), any(Integer.class),
                any(AccessToken.class))).thenReturn(collectionValues);
        DoelFunctionValidator statusValidator = new AnnotationFunctionValidator(
                TestStatusFunction.class.getAnnotation(DoelFunction.class));
        when(doelFunctionRegistry.getFunctionValidator("Status")).thenReturn(statusValidator);
        DoelFunctionImplementation statusImpl = Mockito.mock(DoelFunctionImplementation.class);
        when(statusImpl.process(anyList(), any(String[].class), any(AccessToken.class)))
                .then(new Answer<List<ReferenceValue>>() {
                    @Override
                    public List<ReferenceValue> answer(InvocationOnMock invocation) throws Throwable {
                        List<ReferenceValue> context = (List<ReferenceValue>) invocation.getArguments()[0];
                        String[] params = (String[]) invocation.getArguments()[1];
                        assertThat(context, containsInAnyOrder(
                                new ReferenceValue(comm1Id), new ReferenceValue(comm2Id)));
                        assertThat(params, arrayContaining("Assigned", "Executing"));
                        return Arrays.asList(new ReferenceValue(comm1Id));
                    }
                });
        when(doelFunctionRegistry.getFunctionImplementation("Status")).thenReturn(statusImpl);
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        when(globalCacheClient.getLinkedDomainObjects(any(Id.class), anyString(), anyString(), anyBoolean(), any(AccessToken.class))).thenReturn(null);

        doelResolver.evaluate(expr, docId, accessToken);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        String correctSql1 =
                "select t0.\"id\", t0.\"id_type\" " +
                "from \"commission\" t0 " +
                "where t0.\"parent\" = " + docId.getId();
        String correctSql2 =
                "select t0.\"assignee\", t0.\"assignee_type\" " +
                "from \"job\" t0 " +
                "where t0.\"parent\" = " + comm1Id.getId();
        verify(collectionsDao, times(2)).findCollectionByQuery(sql.capture(), any(Integer.class),
                any(Integer.class), any(AccessToken.class));
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
        when(context.getBean(DoelFunctionRegistry.class)).thenReturn(doelFunctionRegistry);

        when(domainObjectTypeIdCache.getName(docId)).thenReturn("Document");
        when(domainObjectTypeIdCache.getName(comm1Id)).thenReturn("Commission");
        when(domainObjectTypeIdCache.getName(comm2Id)).thenReturn("Commission");
        when(domainObjectTypeIdCache.getName(linkId)).thenReturn("UniversalLink");
        when(domainObjectCacheService.getAll(any(Id.class), any(AccessToken.class), Matchers.<String>anyVararg())).thenReturn(null);

        // Объекты ====================
        DomainObjectTypeConfig documentConfig = new DomainObjectTypeConfig();
        documentConfig.setName("Document");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "Document")).thenReturn(documentConfig);
        when(configurationExplorer.getDomainObjectRootType("Document")).thenReturn("Document");
        when(configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType("Document")).thenReturn(new String[] {"Document"});

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
        when(configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType("Commission")).thenReturn(new String[] {"Commission"});

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
        ReferenceFieldConfig idFieldConfig = new ReferenceFieldConfig();
        idFieldConfig.setName("id");
        when(configurationExplorer.getFieldConfig(//not("*"),
                or(eq("Document"), or(eq("IncomingDocument"), or(eq("Commission"), or(eq("Job"),
                or(eq("Person"), or(eq("Unit"), or(eq("Department"), eq("UniversalLink")))))))),
                eq("id"))).thenReturn(idFieldConfig);
    }
}
