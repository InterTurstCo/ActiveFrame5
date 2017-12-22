package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.dao.api.DatabaseInfo;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

/**
 * Интеграционный тест работы с коллекциями.
 * @author atsvetkov
 */
@RunWith(Arquillian.class)
public class CollectionsIT extends IntegrationTestBase {

    private static final String ADMIN = "admin";

    private static final String PERSON_2_LOGIN = "person2";

    private static Logger logger = Logger.getLogger(CollectionsIT.class.getName());
    
    private static final String DEPARTMENT_TYPE = "Department";
    private static final String PERSON_TYPE = "Person";

    private static final String DEPARTMENT_2 = "Подразделение 2";

    private static final String DEPARTMENT_1 = "Подразделение 1";

    private static final String EMPLOYEE_1_NAME = "Сотрудник 1";

    @EJB
    private CollectionsService.Remote collectionService;

    @EJB
    private CrudService.Remote crudService;

    protected DomainObjectTypeIdCache domainObjectTypeIdCache;
    
    protected AuthenticationService authenticationService; 

    private ConfigurationExplorer configurationExplorer;

    private DatabaseInfo databaseInfo;

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
        LoginContext lc = login(ADMIN, ADMIN);
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
        authenticationInfo.setPassword(ADMIN);
        try {
            authenticationService.insertAuthenticationInfoAndRole(authenticationInfo, null);
        } catch (Exception e) {
            // auth info already exists. Just skip it.
            logger.warning("Authentication info already exists : " + authenticationInfo);

        }
    }

    private void initializeSpringBeans() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        domainObjectTypeIdCache = applicationContext.getBean(DomainObjectTypeIdCache.class);
        authenticationService = applicationContext.getBean(AuthenticationService.class);
        configurationExplorer = applicationContext.getBean(ConfigurationExplorer.class);
        databaseInfo = applicationContext.getBean(DatabaseInfo.class);
    }

    @Test
    public void testFindCollectionByQuery() throws LoginException {
        String query = "select t.id from Employee t where t.Name='" + EMPLOYEE_1_NAME + "'";
        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);
        
        query = "select e.id as id, e.department as department_id, e.name from employee e union select e.id as id, null as department_id, e.name from employee e";
        collection = collectionService.findCollectionByQuery(query);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);
        
        query = "select * from country_al";
        collection = collectionService.findCollectionByQuery(query);
        assertNotNull(collection);
//        assertTrue(collection.size() >= 1);

        query = "select * from country_al ca inner join country c on ca.domain_object_id = c.id where c.id = {0}";
        List<Value> params = new ArrayList<Value>();
        Integer countryTypeid = domainObjectTypeIdCache.getId("country");
        params.add(new ReferenceValue(new RdbmsId(countryTypeid, 33)));

        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
//        assertTrue(collection.size() >= 1);

        query = "select * from country c join person p on p.id = c.created_by join country_union cu on cu.id = c.country_union where c.created_by = {0}";
        params = new ArrayList<Value>();
        Integer personTypeid = domainObjectTypeIdCache.getId("Person");
        
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

        query = "select * from employee e join organization o on o.boss = e.id join department d on d.boss = e.id where d.boss = {0}";
        params = new ArrayList<Value>();
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 2)));
        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

        query = "SELECT d.id FROM (SELECT id FROM employee) e join department d on d.boss = e.id WHERE d.boss={0}";
        params = new ArrayList<Value>();
        Integer employeeTypeid = domainObjectTypeIdCache.getId("Employee");
        params.add(new ReferenceValue(new RdbmsId(employeeTypeid, 3)));

        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);

        query = "SELECT d.name as boss FROM employee e join department d on d.boss = e.id WHERE e.id={0}";
        params = new ArrayList<Value>();
        employeeTypeid = domainObjectTypeIdCache.getId("Employee");
        params.add(new ReferenceValue(new RdbmsId(employeeTypeid, 3)));

        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);
        
        query = "SELECT d.boss FROM (SELECT id FROM employee) e join department d on d.boss = e.id WHERE d.boss={0}";
        params = new ArrayList<Value>();
        employeeTypeid = domainObjectTypeIdCache.getId("Employee");
        params.add(new ReferenceValue(new RdbmsId(employeeTypeid, 3)));

        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);
       
        query = "select x.id, x.col2 from ( select t.id, t.created_date as col2, t.organization as col3 from department t union select t.id, t.created_date as col2, t.organization as col3 from department t ) x where x.col3 = {0}";
        params = new ArrayList<Value>();     
        Integer organizationTypeid = domainObjectTypeIdCache.getId("Organization");
        params.add(new ReferenceValue(new RdbmsId(organizationTypeid, 1)));

        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);


        query = "SELECT so_org_desc_sys.id as id, orgDescr.Module, orgDescr.created_date FROM SO_OrgDescriptionSys so_org_desc_sys " +
        		"join SO_OrgDescription orgDescr on orgDescr.id = so_org_desc_sys.id and orgDescr.id_type = so_org_desc_sys.id_type  " +
        		"WHERE  orgDescr.IsDeleted=0 and orgDescr.Edited is null and module = {0}";
        params = new ArrayList<Value>();     
        Integer ssModuleTypeid = domainObjectTypeIdCache.getId("SS_Module");
        params.add(new ReferenceValue(new RdbmsId(ssModuleTypeid, 1)));

        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

        if (DatabaseInfo.Vendor.ORACLE.equals(databaseInfo.getDatabaseVendor())) {
            query = "select to_date(CASE WHEN tf.timelessDateField1 IS NOT NULL THEN tf.timelessDateField1 ELSE tf.timelessDateField2 END) AS timeless_date, tf.dateTimeField1 from time_field_test tf";
        } else {
            query = "select date(CASE WHEN tf.timelessDateField1 IS NOT NULL THEN tf.timelessDateField1 ELSE tf.timelessDateField2 END) AS timeless_date, tf.dateTimeField1 from time_field_test tf";
        }

        collection = collectionService.findCollectionByQuery(query);
        assertNotNull(collection);

        query = "select (CASE WHEN tf.timelessDateField1 IS NOT NULL THEN tf.timelessDateField1 ELSE tf.timelessDateField2 END) AS timeless_date, tf.dateTimeField1 from time_field_test tf";

        collection = collectionService.findCollectionByQuery(query);
        assertNotNull(collection);

        LoginContext lc = login(PERSON_2_LOGIN, ADMIN);
        lc.login();
        query = "select * from country c join person p on p.id = c.updated_by join country_union cu on cu.id = c.country_union where cu.created_by = {0}";
        params = new ArrayList<Value>();
        personTypeid = domainObjectTypeIdCache.getId("Person");
        
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

        query = "select * from country c join person p on p.id = c.updated_by join country_union cu on cu.id = c.country_union where cu.created_by = {0}";
        params = new ArrayList<Value>();
        personTypeid = domainObjectTypeIdCache.getId("Person");
        
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

        
        query = "select * from employee e join organization o on o.boss = e.id join department d on d.boss = e.id where d.boss = {0}";
        params = new ArrayList<Value>();
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 2)));
        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

        lc.logout();
    }
    
    @Test
    public void testFindCollectionByQueryForAuditLog() throws LoginException {        
        DomainObject organization = createOrganizationDomainObject();
        DomainObject savedOrganization = crudService.save(organization);
        DomainObject department = createDepartmentDomainObject(savedOrganization);
        DomainObject savedDepartment = crudService.save(department);
        
        DomainObject employee = createEmployeeDomainObject(savedDepartment);
        DomainObject savedEmployee = crudService.save(employee);

        GlobalSettingsConfig globalSettings = configurationExplorer.getGlobalSettings();
        Boolean isAuditLogEnabled = false;
        if (globalSettings != null && globalSettings.getAuditLog() != null) {
            isAuditLogEnabled = globalSettings.getAuditLog().isEnable();
        }

        LoginContext lc = login(PERSON_2_LOGIN, ADMIN);
        lc.login();
        
        String query = "select * from country_al";
        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);
        assertNotNull(collection);
        if (isAuditLogEnabled) {
            assertTrue(collection.size() >= 1);
        }
        query = "select * from department_al da inner join department d on da.domain_object_id = d.id where d.id = {0}";
        List<Value> params = new ArrayList<Value>();
        params.add(new ReferenceValue(savedDepartment.getId()));

        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        if (isAuditLogEnabled) {
            assertTrue(collection.size() >= 1);
        }
        
        query = "select * from employee_al ea inner join person_al pa on pa.id = ea.id inner join employee e on pa.domain_object_id = e.id where e.id = {0}";
        params = new ArrayList<Value>();
        params.add(new ReferenceValue(savedEmployee.getId()));

        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

        query = "select * from employee ea inner join person pa on pa.id = ea.id where ea.id = {0}";
        params = new ArrayList<Value>();
        params.add(new ReferenceValue(savedEmployee.getId()));

        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

        query = "select c.id from country c inner join domain_object_type_id t on c.id_type = t.id";
        collection = collectionService.findCollectionByQuery(query);
        assertNotNull(collection);

        lc.logout();

        lc = login(ADMIN, ADMIN);
        query = "select * from employee_al ea inner join person_al pa on pa.id = ea.id inner join employee e on pa.domain_object_id = e.id where e.id = {0}";
        params = new ArrayList<Value>();
        params.add(new ReferenceValue(savedEmployee.getId()));

        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        if (isAuditLogEnabled) {
            assertTrue(collection.size() >= 1);
        }
                
        lc.logout();
        

    }

    @Test
    public void testFindCollectionWithAnaliticExpression() throws LoginException {
        LoginContext lc = login(PERSON_2_LOGIN, ADMIN);
        lc.login();

        String query = "select row_number() over (order by id, capital,independence_day) from country c";
        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);
     
        lc.logout();

    }
    
    @Test
    public void testFindCollectionForAuditLog() throws LoginException {
        DomainObject organization = createOrganizationDomainObject();
        DomainObject savedOrganization = crudService.save(organization);
        DomainObject department = createDepartmentDomainObject(savedOrganization);
        DomainObject savedDepartment = crudService.save(department);
        GlobalSettingsConfig globalSettings = configurationExplorer.getGlobalSettings();
        Boolean isAuditLogEnabled = false;
        if (globalSettings != null && globalSettings.getAuditLog() != null) {
            isAuditLogEnabled = globalSettings.getAuditLog().isEnable();
        }

        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("da.id", Order.ASCENDING));

        List<Filter> filterValues = new ArrayList<Filter>();
        Filter filter = new Filter();
        filter.setFilter("byDepartmentId");
        filter.addCriterion(0, new ReferenceValue(savedDepartment.getId()));
        filterValues.add(filter);

        LoginContext lc = login(PERSON_2_LOGIN, ADMIN);
        lc.login();

        IdentifiableObjectCollection departmantAuditCollection =
                collectionService.findCollection("Department_Audit_Test", sortOrder, filterValues, 0, 0);

        lc.logout();
        if (isAuditLogEnabled) {
            assertNotNull(departmantAuditCollection);
            assertTrue(departmantAuditCollection.size() >= 1);
        }

    }
    
    @Test
    public void testFindCollectionWithNoValueFilter() throws LoginException {        
        DomainObject organizationDomainObject = createOrganizationTestDomainObject();
        organizationDomainObject = crudService.save(organizationDomainObject);
        DomainObject departmentTestObject = createDepartmentTestDomainObject(organizationDomainObject);
        departmentTestObject = crudService.save(departmentTestObject);
        
        DomainObject employee = createEmployeeTestDomainObject(departmentTestObject);        
        DomainObject savedEmployee = crudService.save(employee);

        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("id", Order.ASCENDING));

        List<Filter> filterValues = new ArrayList<Filter>();
        Filter filter = new Filter();
        filter.setFilter("byNullDepartment");
        filterValues.add(filter);

        IdentifiableObjectCollection testCollection =
                collectionService.findCollection("Employees_Test", sortOrder, filterValues, 0, 0);
        assertNotNull(testCollection);
        assertTrue(testCollection.size() == 0);

        filterValues = new ArrayList<Filter>();
        filter = new Filter();
        filter.setFilter("byNotNullDepartment");
        filterValues.add(filter);

        testCollection =
                collectionService.findCollection("Employees_Test", sortOrder, filterValues, 0, 0);
        assertNotNull(testCollection);
        assertTrue(testCollection.size() > 0);
        
        LoginContext lc = login(PERSON_2_LOGIN, ADMIN);
        lc.login();
        testCollection = collectionService.findCollection("Employees_Test", sortOrder, filterValues, 0, 0);
        assertNotNull(testCollection);
        assertTrue(testCollection.size() > 0);
        
        lc.logout();
    }
    
    private DomainObject createOrganizationDomainObject() {
        DomainObject organizationDomainObject = crudService.createDomainObject("Organization");
        organizationDomainObject.setString("Name", "Organization" + new Date());
        return organizationDomainObject;
    }

    private DomainObject createEmployeeDomainObject(DomainObject departmentObject) {
        DomainObject personDomainObject = crudService.createDomainObject("Employee");
        
        personDomainObject.setString("Name", "Name " + System.currentTimeMillis());
        personDomainObject.setString("Position", "Position " + System.currentTimeMillis());
        personDomainObject.setString("Phone", "" + System.currentTimeMillis());        
        personDomainObject.setReference("Department", departmentObject.getId());
        
        return personDomainObject;
    }
    
    @Test
    public void testFindCollectionByQueryWithAliasInSubSelect() throws LoginException {
        LoginContext lc = login(PERSON_2_LOGIN, ADMIN);
        lc.login();

        String query = "select id, organization from (select d.id, '<id>' as organization from department d ) t";
        List<Value> params = new ArrayList<Value>();
        params = new ArrayList<Value>();
        Integer countryTypeid = domainObjectTypeIdCache.getId("country");
        params.add(new StringValue(new RdbmsId(countryTypeid, 33).toStringRepresentation()));

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);

        query = "select id, organization from (select d.id, '<id>' as organization from department d ) t";

        collection = collectionService.findCollectionByQuery(query);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);

        query = "select d.id, '<id>' as organization from department d ";

        collection = collectionService.findCollectionByQuery(query);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);
        lc.logout();

    }

    @Test
    public void testFindCollectionByQueryWithCastExpression() throws LoginException {
        String query = "select c.id,  substr(c.name, 1, 3)  from country c";
        List<Value> params = new ArrayList<Value>();

        IdentifiableObjectCollection collection = null;
        LoginContext lc = login(PERSON_2_LOGIN, ADMIN);
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
    public void testFindCollectionWithFilters() throws LoginException {
        
        DomainObject organizationDomainObject = createOrganizationTestDomainObject();
        organizationDomainObject = crudService.save(organizationDomainObject);
        DomainObject departmentTestObject = createDepartmentTestDomainObject(organizationDomainObject);
        departmentTestObject = crudService.save(departmentTestObject);

        DomainObject employee = createEmployeeTestDomainObject(departmentTestObject);        
        DomainObject savedEmployee = crudService.save(employee);

        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("e.id", Order.ASCENDING));

        List<Filter> filterValues = new ArrayList<Filter>();
        Filter filter = new Filter();
        filter.setFilter("inDepartment");
        int departmentTypeId = domainObjectTypeIdCache.getId("department_test");
        List<Value> referenceValues = new ArrayList<>();
        referenceValues.add(new ReferenceValue(departmentTestObject.getId()));
        ListValue listValue = new ListValue(referenceValues);
        filter.addCriterion(0, listValue);

        filterValues.add(filter);

        filter = new Filter();
        filter.setFilter("byDepartmentNames");
        List<Value> departmentNames = new ArrayList<>();
        departmentNames.add(new StringValue(departmentTestObject.getString("Name")));
        listValue = new ListValue(departmentNames);
        filter.addCriterion(0, listValue);
        filterValues.add(filter);

        IdentifiableObjectCollection employeesCollection = null;
        employeesCollection =
                collectionService.findCollection("Employees_Test", sortOrder, filterValues, 0, 0);

        assertNotNull(employeesCollection);
        assertTrue(employeesCollection.size() >= 1);
        
    }
    
    @Test
    public void testFindCollectionByQueryWithParams() {
        String query = "select * from Employee e where e.department = {0} and name = {1}";
        List<Value> params = new ArrayList<Value>();
        Integer departmentTypeid = domainObjectTypeIdCache.getId(DEPARTMENT_TYPE);
        RdbmsId depId = new RdbmsId(departmentTypeid, 1);
        params.add(new ReferenceValue(depId));
        params.add(new StringValue(EMPLOYEE_1_NAME));

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);


    }    

    @Test
    public void testFindCollectionByQueryWithReferenceParamsInSubQuery() {

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
        
        query = "select id from person where created_by = {0}";
        params = new ArrayList<Value>();
        personTypeid = domainObjectTypeIdCache.getId(PERSON_TYPE);
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));

        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        assertTrue(collection.size() > 0);
        
        query = "select * from person p where p.login = {0} and p.profile = {1}";
        
        params = new ArrayList<>();
        int personProfileTypeid = domainObjectTypeIdCache.getId("person_profile");
        params.add(new StringValue("person1"));
        params.add(new ReferenceValue(new RdbmsId(personProfileTypeid, 2)));
        
        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

        int moduleTypeid = domainObjectTypeIdCache.getId("SS_Module");

        query = "SELECT cnt.id, kv.\"Owner\", cnt.\"Module\"  FROM Num_Counter cnt JOIN Num_KeyValue kv ON kv.\"Owner\" = cnt.id WHERE kv.\"Value\" = {0} AND cnt.\"Module\" = {1}";
        params = Arrays.<Value>asList(new StringValue("value"), new ReferenceValue(new RdbmsId(moduleTypeid, 1)));
        IdentifiableObjectCollection coll = collectionService.findCollectionByQuery(query, params, 0, 2);

        assertNotNull(coll);
    }       

    @Test
    public void testFindCollectionByQueryWithReferenceParamsInListValue() {

        String query = "select * from employee e where e.department in ({0})";
        int departmentTypeId = domainObjectTypeIdCache.getId("department");

        List<Value> referenceValues =
                Arrays.<Value> asList(new ReferenceValue(new RdbmsId(departmentTypeId, 1)), new ReferenceValue(new RdbmsId(departmentTypeId, 2)));
        ListValue listValue = new ListValue(referenceValues);

        List<Value> params = new ArrayList<>();
        params.add(listValue);
        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        assertTrue(collection.size() > 0);

        query = "select * from employee e where e.department not in ({0})";

        referenceValues = Arrays.<Value> asList(new ReferenceValue(new RdbmsId(departmentTypeId, 1)));
        listValue = new ListValue(referenceValues);
        params = new ArrayList<>();
        params.add(listValue);

        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        assertTrue(collection.size() > 0);
        
        Integer personTypeid = domainObjectTypeIdCache.getId(PERSON_TYPE);
        referenceValues = new ArrayList<>();
        referenceValues.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        referenceValues.add(new ReferenceValue(new RdbmsId(personTypeid, 2)));
        ListValue listValueForRef = new ListValue(referenceValues);
        
        params = new ArrayList<>();
        params.add(listValueForRef);
        
        List<Value> loginValues = new ArrayList<>();
        loginValues.add(new StringValue("person1"));
        loginValues.add(new StringValue("person2"));        
        ListValue listValueForLogin = new ListValue(loginValues);
        params.add(listValueForLogin);       
        query = "select id, login from person where id in ({0}) or login in ({1})";
        collectionService.findCollectionByQuery(query, params);

        referenceValues = new ArrayList<>();
        referenceValues.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));            
        listValueForRef = new ListValue(referenceValues);
        params = new ArrayList<>();
        params.add(listValueForRef);
        params.add(listValueForLogin);       

        collectionService.findCollectionByQuery(query, params);
    }
    
    @Test
    public void testFindCollectionByQueryWithListParam() {
        String query = "select * from Employee e where e.department in ({0}) and name = {1}";

        Integer departmentTypeid = domainObjectTypeIdCache.getId(DEPARTMENT_TYPE);

        List<Value> departments = new ArrayList<>();
        departments.add(new ReferenceValue(new RdbmsId(departmentTypeid, 1)));
        departments.add(new ReferenceValue(new RdbmsId(departmentTypeid, 2)));

        ListValue departmentsParam = new ListValue(departments);

        List<Value> params = new ArrayList<Value>();
        params.add(departmentsParam);
        params.add(new StringValue(EMPLOYEE_1_NAME));

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

        query = "select * from Employee e where name in ({0})";
        List<Value> employees = new ArrayList<>();

        employees.add(new StringValue(EMPLOYEE_1_NAME));
        employees.add(new StringValue("Сотрудник 2"));
        employees.add(new StringValue("Сотрудник 3"));

        ListValue employeesParam = new ListValue(employees);
        params = new ArrayList<Value>();
        params.add(employeesParam);

        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
    }
    
    private DomainObject createEmployeeTestDomainObject(DomainObject departmentObject) {
        DomainObject employeeDomainObject = crudService.createDomainObject("employee_test");
        
        employeeDomainObject.setString("Name", "Name " + System.currentTimeMillis());
        employeeDomainObject.setString("Position", "Position " + System.currentTimeMillis());
        employeeDomainObject.setString("Phone", "" + System.currentTimeMillis()); 
        employeeDomainObject.setString("Login", "Login" + System.currentTimeMillis()); 
        employeeDomainObject.setString("EMail", "Email" + System.currentTimeMillis()); 
        
        employeeDomainObject.setReference("Department", departmentObject.getId());
        
        return employeeDomainObject;
    }

    private DomainObject createDepartmentTestDomainObject(DomainObject organizationDomainObject) {
        
        DomainObject departmentDomainObject = crudService.createDomainObject("department_test");
        departmentDomainObject.setString("Name", "Departmment");
        departmentDomainObject.setLong("Number1", new Long(1));
        departmentDomainObject.setLong("Number2", new Long(2));
        
        departmentDomainObject.setTimestamp("Date1", new Date());
        departmentDomainObject.setTimestamp("Date2", new Date());

        departmentDomainObject.setReference("Organization", organizationDomainObject.getId());
        return departmentDomainObject;
    }

    private DomainObject createDepartmentDomainObject(DomainObject organizationDomainObject) {
        
        DomainObject departmentDomainObject = crudService.createDomainObject("Department");
        departmentDomainObject.setString("Name", "Departmment");
        departmentDomainObject.setLong("Number1", new Long(1));
        departmentDomainObject.setLong("Number2", new Long(2));
        
        departmentDomainObject.setTimestamp("Date1", new Date());
        departmentDomainObject.setTimestamp("Date2", new Date());

        departmentDomainObject.setReference("Organization", organizationDomainObject.getId());
        return departmentDomainObject;
    }

    private DomainObject createOrganizationTestDomainObject() {
        DomainObject organizationDomainObject = crudService.createDomainObject("organization_test");
        organizationDomainObject.setString("Name", "Organization");
        return organizationDomainObject;
    }
    
    @Test
    public void testFindCollectionByQueryWithSubQueryHavingJoins() {
        String query = "SELECT COUNT(*) AS child_employee_count FROM (SELECT DISTINCT e.id FROM employee e inner join department d on (d.id = e.department) WHERE d.boss={0}) t";

        Integer personTypeid = domainObjectTypeIdCache.getId(PERSON_TYPE);
        List<Value> params = new ArrayList<>();
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);
        
        query = "select name, (select count(e.id) from employee e where e.id = {0}) as count from employee where (select count(p.created_by) from person p where p.created_by = {1}) > 0";
        
        params = new ArrayList<>();
        int personProfileTypeid = domainObjectTypeIdCache.getId("person_profile");
        
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 2)));
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        
        
        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

    }
    
    @Test
    public void testFindCollectionByQueryWithWithSubSelect() throws LoginException {
        String query = "select id from employee where (select count(e.id) from employee e where e.name = 'xxx') > 0";
        List<Value> params = new ArrayList<Value>();
        Integer personTypeid = domainObjectTypeIdCache.getId(PERSON_TYPE);
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

        query = "select id from employee where (select count(e.id) from employee e where e.id = {0}) > 0";
        params = new ArrayList<Value>();
        personTypeid = domainObjectTypeIdCache.getId(PERSON_TYPE);
        params.add(new ReferenceValue(new RdbmsId(personTypeid, 1)));
        
        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

        query = "select id, name, \"created_by\", '' as test from (select s.id, s.name, s.status,  " +
        		"coalesce('<id>' || substr(created_by.\"name\", 1, 5) || ':' || '</>', '<id></><shortName></>') as \"created_by\", '' as test2 from schedule s inner join schedule created_by on s.id = created_by.id) t";
        params = new ArrayList<Value>();
        
        collection = collectionService.findCollectionByQuery(query, params);
        assertNotNull(collection);

    }
    
    @Test
    public void testFindCollectionWithReferenceParamsInSubQuery() throws LoginException {
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
        LoginContext lc = login(PERSON_2_LOGIN, ADMIN);
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
    
    @Test
    public void testFindCollectionUsingGenerator() throws LoginException {
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

        IdentifiableObjectCollection collection =
                collectionService.findCollection("EmployeesGenerator", sortOrder, filterValues, 0, 0);
        
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);
    }
    
    @Test
    public void testFindCollectionByQueryWithLimitAndOffset() {
        String query = "select * from employee e";

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query, 2, 2);
        collection = collectionService.findCollectionByQuery(query, 0, 2);
        collection = collectionService.findCollectionByQuery(query, 2, 0);

        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("id", Order.ASCENDING));
        
        collection = collectionService.findCollection("Test_Union_Limit", sortOrder, null, 2, 2);
        assertNotNull(collection);
        assertTrue(collection.size() == 2);
    }
}
