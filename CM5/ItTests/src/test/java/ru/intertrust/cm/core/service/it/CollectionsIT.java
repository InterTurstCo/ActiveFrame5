package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

/**
 * Интеграционный тест работы с коллекциями.
 * @author atsvetkov
 */
@RunWith(Arquillian.class)
public class CollectionsIT extends IntegrationTestBase {

    private static final String PERSON_2_PASSWORD = "admin";

    private static final String PERSON_2_LOGIN = "person2";

    private static Logger logger = Logger.getLogger(CollectionsIT.class.getName());
    
    private static final String DEPARTMENT_TYPE = "Department";
    private static final String PERSON_TYPE = "Person";

    private static final String DEPARTMENT_2 = "Подразделение 2";

    private static final String DEPARTMENT_1 = "Подразделение 1";

    private static final String EMPLOYEE_1_NAME = "Сотрудник 1";

    @EJB
    private CollectionsService.Remote collectionService;

    protected DomainObjectTypeIdCache domainObjectTypeIdCache;
    
    protected AuthenticationService authenticationService; 

    /**
     * Предотвращает загрузку данных для каждого теста. Данные загружаются один раз для всех тестов в данном классе.
     */
    private boolean isDataLoaded = false;

    @Test
    public void testArquillianInjection() {
        Assert.assertNotNull(collectionService);
    }

    @Before
    public void init() throws IOException, LoginException {
        LoginContext lc = login(PERSON_2_PASSWORD, PERSON_2_PASSWORD);
        lc.login();
        try {
            if (!isDataLoaded) {
                importTestData("test-data/import-department.csv");
                importTestData("test-data/import-organization.csv");
                importTestData("test-data/import-employee.csv");
                isDataLoaded = true;
            }
        } finally {
            lc.logout();
        }

        
        initializeSpringBeans();        
    }

    private void createAuthenticationInfo() {
        AuthenticationInfoAndRole authenticationInfo = new AuthenticationInfoAndRole();
        authenticationInfo.setUserUid(PERSON_2_LOGIN);
        authenticationInfo.setPassword(PERSON_2_PASSWORD);
        try {
            authenticationService.insertAuthenticationInfoAndRole(authenticationInfo);
        } catch (Exception e) {
            // auth info already exists. Just skip it.
            logger.warning("Authentication info already exists : " + authenticationInfo);

        }
    }

    private void initializeSpringBeans() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        domainObjectTypeIdCache = applicationContext.getBean(DomainObjectTypeIdCache.class);
        authenticationService = applicationContext.getBean(AuthenticationService.class);
    }

    @Test
    public void testFindCollectionByQuery() {
        String query = "select t.id from Employee t where t.Name='" + EMPLOYEE_1_NAME + "'";
        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);
    }

//    @Test
    public void testFindCollectionByQueryWithCastExpression() throws LoginException {
        String query = "select c.id,  cast (c.name as char(3))  from country c";
        List<Value> params = new ArrayList<Value>();

        IdentifiableObjectCollection collection = null;
        LoginContext lc = login(PERSON_2_LOGIN, PERSON_2_PASSWORD);
        lc.login();
        try {
            collection = collectionService.findCollectionByQuery(query, params);

        } finally {
            lc.logout();
        }

        assertNotNull(collection);
        assertTrue(collection.size() >= 1);


    }
    
    @Test
    public void testFindCollectionByQueryWithParams() {
        String query = "select * from Employee e where e.department = {0} and name = {1}";
        List<Value> params = new ArrayList<Value>();
        Integer departmentTypeid = domainObjectTypeIdCache.getId(DEPARTMENT_TYPE);
        params.add(new StringValue(new RdbmsId(departmentTypeid, 1).toStringRepresentation()));
        params.add(new StringValue(EMPLOYEE_1_NAME));

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);


    }

    @Test
    public void testFindCollectionByQueryWithReferenceParamsInSubSubQuery() {

        String query = "select p2.id, (select p3.login from person p3 where p3.id = {0}) as login " +
        		" from (select * from person p where p.id = {1}) p2 " +
        		" where p2.id = {2}";
        List<Value> params = new ArrayList<Value>();
        Integer personTypeid = domainObjectTypeIdCache.getId(PERSON_TYPE);

        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));        
        
        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        assertTrue(collection.size() > 0);

        query = "select p2.id, (select p3.login from person p3 where p3.id = {0}) as login " +
                "from (select * from person p where p.id = {1}) p2 " +
                " inner join (select * from person p where p.id = {2}) p5 on p5.id = p2.id" +
                " where p2.id in (select p4.id from person p4 where p4.id = {3}) or p2.id = {4}";
        params = new ArrayList<Value>();
        personTypeid = domainObjectTypeIdCache.getId(PERSON_TYPE);

        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));        
        
        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        assertTrue(collection.size() > 0);
        
        query = "select distinct year from (select year from schedule where created_by = {0}) t";
        
        params = new ArrayList<Value>();
        personTypeid = domainObjectTypeIdCache.getId(PERSON_TYPE);
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        
        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        
        query = "select t.year from (select id, year, created_by from schedule where created_by = {0}) t " +
        		"inner join (select * from schedule p where created_by = {1}) t2 on " +
        		"t2.created_by = t.created_by " +
        		"where t2.created_by in (select t4.created_by from schedule t4 where t4.created_by = {2}) or " +
        		"t2.created_by = {3}";
        
        params = new ArrayList<Value>();
        personTypeid = domainObjectTypeIdCache.getId(PERSON_TYPE);
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        
        collection = collectionService.findCollectionByQuery(query, params);

    }
    
    @Test
    public void testFindCollectionWithReferenceParamsInSubSubQuery() throws LoginException {
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("id", Order.ASCENDING));

        List<Filter> filterValues = new ArrayList<Filter>();
        Filter filter = new Filter();
        filter.setFilter("byPersonId");
        filter.addCriterion(0, new LongValue(1));
        filter.addCriterion(1, new LongValue(1));
        filter.addCriterion(2, new LongValue(1));
        filter.addCriterion(3, new LongValue(1));

        filterValues.add(filter);

        IdentifiableObjectCollection employeesCollection = null;
        employeesCollection =
                collectionService.findCollection("Person_Test", sortOrder, filterValues, 0, 0);

        assertNotNull(employeesCollection);
        assertTrue(employeesCollection.size() >= 1);

    }

    
    @Test
    public void testFindCollectionWithFilters() throws LoginException {
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("e.id", Order.ASCENDING));

        List<Filter> filterValues = new ArrayList<Filter>();
        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new LongValue(1));
        filterValues.add(filter);

        IdentifiableObjectCollection employeesCollection = null;
        employeesCollection =
                collectionService.findCollection("Employees", sortOrder, filterValues, 0, 0);

        assertNotNull(employeesCollection);
        assertTrue(employeesCollection.size() >= 1);

    }

//    @Test
    public void testFindCollectionWithAcl() throws LoginException {
        createAuthenticationInfo();
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("e.id", Order.ASCENDING));

        List<Filter> filterValues = new ArrayList<Filter>();
        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new LongValue(1));
        filterValues.add(filter);

        IdentifiableObjectCollection employeesCollection = null;
        LoginContext lc = login(PERSON_2_LOGIN, PERSON_2_PASSWORD);
        lc.login();
        try {
            employeesCollection =
                    collectionService.findCollection("Employees", sortOrder, filterValues, 0, 0);

        } finally {
            lc.logout();
        }

        assertNotNull(employeesCollection);
        // assertTrue(employeesCollection.size() >= 1);

    }

    @Test
    public void testFindCollectionWithFilterMultiCriterion() {
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("e.id", Order.ASCENDING));
        List<Filter> filterValues;
        filterValues = new ArrayList<Filter>();
        Filter filter = new Filter();
        filter.setFilter("byDepartmentNames");
        List<Value> departmentNames = new ArrayList<Value>();
        departmentNames.add(new StringValue(DEPARTMENT_1));
        departmentNames.add(new StringValue(DEPARTMENT_2));
        filter.addMultiCriterion(0, departmentNames);
        filterValues.add(filter);

        IdentifiableObjectCollection multiDepartmantOEmployeeCollection =
                collectionService.findCollection("Employees", sortOrder, filterValues, 0, 0);

        assertNotNull(multiDepartmantOEmployeeCollection);
        assertTrue(multiDepartmantOEmployeeCollection.size() >= 1);
    }

    @Test
    public void testFindCollectionWithoutFilters() {
        IdentifiableObjectCollection collection = collectionService.findCollection("Departments");
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);
    }

    @Test
    public void testFindCollectionCount() {
        Integer collectionCount = collectionService.findCollectionCount("Employees", null);

        assertTrue(collectionCount >= 1);

        List<Filter> filterValues = new ArrayList<Filter>();
        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new LongValue(1));
        filterValues.add(filter);
        collectionCount = collectionService.findCollectionCount("Employees", filterValues);
        assertTrue(collectionCount >= 1);

    }
    
    @Test
    public void testIsCollectionEmpty() {
        List<Filter> filterValues = new ArrayList<Filter>();
        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new LongValue(1));
        filterValues.add(filter);

        boolean isEmpty = collectionService.isCollectionEmpty("Employees", filterValues);
        assertTrue(!isEmpty);
        
        filterValues = new ArrayList<Filter>();
        filter = new Filter();
        filter.setFilter("byName");
        filter.addCriterion(0, new StringValue(System.currentTimeMillis() + ""));
        filterValues.add(filter);        
        isEmpty = collectionService.isCollectionEmpty("Employees", filterValues);

        assertTrue(isEmpty);
        
    }
}
