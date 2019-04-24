package ru.intertrust.cm.core.service.it;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.model.AccessException;
import ru.intertrust.cm.core.model.ObjectNotFoundException;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Иитеграционный тест для {@link CrudService}
 * @author atsvetkov
 */
@RunWith(Arquillian.class)
public class CrudServiceIT extends IntegrationTestBase {

    private static final String ADMIN = "admin";

    private static final String PERSON_2_LOGIN = "person2";
    private static final String PERSON_3_LOGIN = "person3";
    
    protected DomainObjectDao domainObjectDao;
    
    protected AccessControlService accessControlService;


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ConfigurationExplorer configurationExplorer;
    @EJB
    private CrudService.Remote crudService;

    @Before
    public void init() throws IOException, LoginException {
        LoginContext lc = login("admin", "admin");
        lc.login();
        try {
            importTestData("test-data/import-organization.csv");
            importTestData("test-data/import-employee.csv");
        } finally {
            lc.logout();
        }
        initializeSpringBeans();
    }

    private void initializeSpringBeans() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        domainObjectTypeIdCache = applicationContext.getBean(DomainObjectTypeIdCache.class);
        configurationExplorer = applicationContext.getBean(ConfigurationExplorer.class);
        domainObjectDao = applicationContext.getBean(DomainObjectDao.class);
        accessControlService = applicationContext.getBean(AccessControlService.class);
    }

    
    @Test
    public void testReadEveryBody() throws LoginException {
        LoginContext lc = login(PERSON_2_LOGIN, ADMIN);
        lc.login();

        String user = PERSON_2_LOGIN;
        Integer cityTypeId = domainObjectTypeIdCache.getId("city");

        final RdbmsId id = new RdbmsId(cityTypeId, 1);
        final List<Id> ids = Collections.<Id> singletonList(new RdbmsId(cityTypeId, 1));
        final Id[] idsArray = new Id[] {new RdbmsId(cityTypeId, 1) };
        AccessToken accessToken1 = accessControlService.createAccessToken(user, null, DomainObjectAccessType.READ);
        AccessToken accessToken2 = accessControlService.createAccessToken(user, id, DomainObjectAccessType.READ);
        AccessToken accessToken3 = accessControlService.createAccessToken(user, idsArray, DomainObjectAccessType.READ, false);
        final DomainObject result1 = domainObjectDao.find(id, accessToken1);
        final DomainObject result2 = domainObjectDao.find(id, accessToken2);
        final List<DomainObject> result3 = domainObjectDao.find(ids, accessToken1);
        final List<DomainObject> result4 = domainObjectDao.find(ids, accessToken3);

        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result3.size() > 0);
        assertNotNull(result4.size() > 0);
        lc.logout();
    }
    
    @Test
    public void testSaveFindExists() throws LoginException {        
        
        DomainObject personDomainObject = createPersonDomainObject();
        DomainObject savedPersonObject = crudService.save(personDomainObject);

        assertNotNull(savedPersonObject);
        assertNotNull(savedPersonObject.getId());
        assertNotNull(savedPersonObject.getString("Login"));

        DomainObject foundPersonObject = crudService.find(savedPersonObject.getId());

        assertNotNull(foundPersonObject);
        assertNotNull(foundPersonObject.getId());
        assertNotNull(foundPersonObject.getString("Login"));
        boolean exists = crudService.exists(savedPersonObject.getId());
        assertTrue(exists);
        
        int personTypeId = domainObjectTypeIdCache.getId("Person");        
        exists = crudService.exists(new RdbmsId(personTypeId, 100000));
        assertFalse(exists);
        
        try {
            DomainObject person = crudService.find(new RdbmsId(personTypeId, 100000));
            fail("Should throw ObjectNotFoundException");
        } catch (ObjectNotFoundException e) {
        }

    }

    @Test
    public void testMultipleSave() {
        DomainObject personDomainObject = createPersonTestDomainObject();
        DomainObject savedPersonObject = crudService.save(personDomainObject);

        assertNotNull(savedPersonObject);
        assertNotNull(savedPersonObject.getId());
        assertNotNull(savedPersonObject.getString("Login"));

        savedPersonObject = crudService.save(savedPersonObject);
        assertNotNull(savedPersonObject);
        assertNotNull(savedPersonObject.getId());
        assertNotNull(savedPersonObject.getString("Login"));
    }

//    @Test
    public void testCreateWithReferenceAccessMatrix() throws LoginException {
        DomainObject organizationDomainObject = createOrganizationTestDomainObject();
        organizationDomainObject = crudService.save(organizationDomainObject);
        DomainObject departmentTestObject = createDepartmentTestDomainObject(organizationDomainObject);
        DomainObject savedDepartmentTestObject = crudService.save(departmentTestObject);
        
        DomainObject departmentTest2Object = createDepartmentTest2DomainObject(organizationDomainObject);
        DomainObject savedDepartmentTest2Object = crudService.save(departmentTest2Object);

        LoginContext lc = login(PERSON_2_LOGIN, ADMIN);
        lc.login();

        DomainObject employee = createEmployeeTestDomainObject(savedDepartmentTestObject);        
        DomainObject savedEmployee = crudService.save(employee);

        try {
            employee = createEmployeeTestDomainObject(savedDepartmentTest2Object);
            savedEmployee = crudService.save(employee);
            fail("Exception should be thrown. There is no permission to create object department_test2");
        } catch (EJBException ex) {
            if (!(ex.getCause() instanceof AccessException)) {
                fail("Cause exception should be AccessException");
            }
        }
        lc.logout();
    }

    @Test
    public void testFindDelete() {
        DomainObject organization1 = createOrganizationDomainObject();
        DomainObject savedOrganization1 = crudService.save(organization1);

        DomainObject foundOrganization = crudService.find(savedOrganization1.getId());
        assertNotNull(foundOrganization);
        crudService.delete(foundOrganization.getId());
        thrown.expect(Exception.class);
        foundOrganization = crudService.find(foundOrganization.getId());

    }

    @Test
    public void testFindAndDeleteList() {

        DomainObject organization1 = createOrganizationDomainObject();
        
        DomainObject savedOrganization1 = crudService.save(organization1);

        DomainObject department = createDepartmentDomainObject(savedOrganization1);
        DomainObject savedDepartment = crudService.save(department);
        
        Id[] objects = new Id[] {savedDepartment.getId(), savedOrganization1.getId()};
        List<Id> objectIds = Arrays.asList(objects);

        List<DomainObject> foundObjects = crudService.find(objectIds);

        List<Id> foundIds = getIdList(foundObjects);
        assertNotNull(foundObjects);
        assertTrue(foundObjects.size() == 2);

        assertTrue(foundIds.contains(savedDepartment.getId()));
        assertTrue(foundIds.contains(savedOrganization1.getId()));

        crudService.delete(objectIds);
        foundObjects = crudService.find(objectIds);
        assertTrue(foundObjects.size() == 0);

    }

    @Test
    public void testFindAll() {
        List<DomainObject> allPersons = crudService.findAll("Person");
        int empoyeeTypeId = domainObjectTypeIdCache.getId("Employee");
        
        for(DomainObject person : allPersons){
            int personTypeId = ((RdbmsId)person.getId()).getTypeId();
            if (empoyeeTypeId == personTypeId) {
                assertTrue(person.getFields().contains("Department"));
                assertTrue(person.getFields().contains("Position"));
            }
        }
    }
    
    private List<Id> getIdList(List<DomainObject> domainObjects) {
        List<Id> foundIds = new ArrayList<Id>();
        for (DomainObject domainObject : domainObjects) {
            foundIds.add(domainObject.getId());
        }
        return foundIds;
    }

    @Test
    public void testFindLinkedDomainObjects() {
        DomainObject organization = createOrganizationDomainObject();
        DomainObject savedOrganization = crudService.save(organization);
        DomainObject department = createDepartmentDomainObject(savedOrganization);
        DomainObject savedDepartment = crudService.save(department);

        DomainObject employee = createEmployeeDomainObject(savedDepartment);
        DomainObject savedEmployee = crudService.save(employee);

        List<DomainObject> linkedObjects =
                crudService.findLinkedDomainObjects(savedOrganization.getId(), "Department", "Organization");
        assertNotNull(linkedObjects);
        assertEquals(linkedObjects.get(0).getId(), savedDepartment.getId());

        List<Id> linkedObjectsIds =
                crudService.findLinkedDomainObjectsIds(savedOrganization.getId(), "Department", "Organization");
        assertNotNull(linkedObjectsIds);
        assertEquals(linkedObjectsIds.get(0), savedDepartment.getId());

        // CMFIVE-30337
        DomainObject tmpDepartment = createDepartmentDomainObject("department2", savedOrganization);
        tmpDepartment = crudService.save(department);
        List<DomainObject> tmpLinkedObjects1 =
                crudService.findLinkedDomainObjects(savedOrganization.getId(), "Department", "Organization");
        assertNotNull(tmpLinkedObjects1);
        assertFalse(tmpLinkedObjects1.isEmpty());
        DomainObject tmpEmployee = createEmployeeDomainObject(tmpLinkedObjects1.get(0));
        tmpEmployee = crudService.save(tmpEmployee);
        List<DomainObject> tmpLinkedObjects2 =
                crudService.findLinkedDomainObjects(savedOrganization.getId(), "Department", "Organization");
        assertNotNull(tmpLinkedObjects2);
        assertFalse(tmpLinkedObjects2.isEmpty());
        assertTrue(tmpLinkedObjects2.size() == tmpLinkedObjects1.size());
        // CMFIVE-30337

        GlobalSettingsConfig globalSettings = configurationExplorer.getGlobalSettings();
        Boolean isAuditLogEnabled = false;
        if (globalSettings != null && globalSettings.getAuditLog() != null) {
            isAuditLogEnabled = globalSettings.getAuditLog().isEnable();
        }
        
        List<DomainObject> linkedAuditObjects =
                crudService.findLinkedDomainObjects(savedDepartment.getId(), "Department_al", "domain_object_id");
        assertNotNull(linkedAuditObjects);
            assertNotNull(linkedAuditObjects);
        if(isAuditLogEnabled){
            assertTrue(linkedAuditObjects.size() >= 1);
            assertEquals(linkedAuditObjects.get(0).getReference("domain_object_id"), savedDepartment.getId());
            
        }
     
    }

    @Test
    public void testFindLinkedDomainObjectsWithInheritanceAndAccessChecks() throws LoginException {
        LoginContext lc = login(PERSON_2_LOGIN, ADMIN);
        lc.login();
        Id domainObjectId = new RdbmsId(1, 1);
        // Проверка на выполнимость SQL запросов для случая иерархии наследования и подключения проверки прав
        crudService.findLinkedDomainObjects(domainObjectId, "test_DO_5", "ref_DO_1");
        crudService.findLinkedDomainObjectsIds(domainObjectId, "test_DO_5", "ref_DO_1");
        lc.logout();
    }
    
    @Test
    public void testFindLinkedDomainObjectsWithInheritance() {
        DomainObject soOrgSystem = crudService.createDomainObject("SO_OrgSystem");
        soOrgSystem.setBoolean("IsDeleted", false);
        soOrgSystem.setString("ShortName", "ShortName1");
        soOrgSystem.setString("FullName", "FullName1");
        soOrgSystem.setString("NoticesFormulaIDs", "NoticesFormulaIDs1");
        soOrgSystem = crudService.save(soOrgSystem);

        DomainObject soParentSU = crudService.createDomainObject("SO_Parent_SU");
        soParentSU.setReference("Owner", soOrgSystem);
        soParentSU = crudService.save(soParentSU);

        DomainObject soDepartmentExt = crudService.createDomainObject("SO_DepartmentExt");
        soDepartmentExt.setString("TelexExt", "TelexExt");
        soDepartmentExt.setString("ShortName", "ShortName2");
        soDepartmentExt.setString("FullName", "FullName2");
        soDepartmentExt.setString("NoticesFormulaIDs", "NoticesFormulaIDs2");
        soDepartmentExt.setReference("HierRoot", soOrgSystem.getId());
        soDepartmentExt.setReference("HierParent", soParentSU.getId());
        soDepartmentExt.setString("Type", "Type2");
        soDepartmentExt.setBoolean("IsIndependent", false);
        soDepartmentExt.setBoolean("IsIsolated", false);
        soDepartmentExt = crudService.save(soDepartmentExt);

        // Test findLinkedDomainObjectsIds with exactType == true
        List<Id> linkedIds = crudService.findLinkedDomainObjectsIds(soOrgSystem.getId(), "SO_Department", "HierRoot", true);
        assertNotNull(linkedIds);
        for (Id id : linkedIds) {
            if (((RdbmsId)id).getTypeId() != domainObjectTypeIdCache.getId("SO_Department")) {
                fail("findLinkedDomainObjectsIds with exactType==true returned inherited type id");
            }
        }

        // Test findLinkedDomainObjectsIds with exactType == false
        linkedIds = crudService.findLinkedDomainObjectsIds(soOrgSystem.getId(), "SO_Department", "HierRoot");
        assertNotNull(linkedIds);
        assertTrue(linkedIds.size() > 0);

        // Test findLinkedDomainObjects with exactType == true
        List<DomainObject> linkedObjects = crudService.findLinkedDomainObjects(soOrgSystem.getId(), "SO_Department", "HierRoot", true);
        assertNotNull(linkedObjects);
        for (DomainObject linkedObject : linkedObjects) {
            if (!linkedObject.getTypeName().equalsIgnoreCase("SO_Department")) {
                fail("findLinkedDomainObjects with exactType==true returned inherited type");
            }
        }

        // Test findLinkedDomainObjects with exactType == false
        linkedObjects = crudService.findLinkedDomainObjects(soOrgSystem.getId(), "SO_Department", "HierRoot");
        assertNotNull(linkedObjects);
        assertTrue(linkedObjects.size() > 0);

        for (DomainObject linkedObject : linkedObjects) {
            if (linkedObject.getString("TelexExt") != null && linkedObject.getString("TelexExt").equals("TelexExt") &&
                    linkedObject.getString("NoticesFormulaIDs") != null &&
                    linkedObject.getString("NoticesFormulaIDs").equals("NoticesFormulaIDs2")) {
                return;
            }
        }

        fail("Linked object wasn't found");
    }

    @Test
    public void testFindAllWithInheritance() {
        DomainObject soOrgSystem = crudService.createDomainObject("SO_OrgSystem");
        soOrgSystem.setBoolean("IsDeleted", false);
        soOrgSystem.setString("ShortName", "ShortName1");
        soOrgSystem.setString("FullName", "FullName1");
        soOrgSystem.setString("NoticesFormulaIDs", "NoticesFormulaIDs1");
        soOrgSystem = crudService.save(soOrgSystem);

        DomainObject soParentSU = crudService.createDomainObject("SO_Parent_SU");
        soParentSU.setReference("Owner", soOrgSystem);
        soParentSU = crudService.save(soParentSU);

        DomainObject soDepartmentExt = crudService.createDomainObject("SO_DepartmentExt");
        soDepartmentExt.setString("TelexExt", "TelexExt");
        soDepartmentExt.setString("ShortName", "ShortName2");
        soDepartmentExt.setString("FullName", "FullName2");
        soDepartmentExt.setString("NoticesFormulaIDs", "NoticesFormulaIDs2");
        soDepartmentExt.setReference("HierRoot", soOrgSystem.getId());
        soDepartmentExt.setReference("HierParent", soParentSU.getId());
        soDepartmentExt.setString("Type", "Type2");
        soDepartmentExt.setBoolean("IsIndependent", false);
        soDepartmentExt.setBoolean("IsIsolated", false);
        soDepartmentExt = crudService.save(soDepartmentExt);

        // Test findAll with exactType == true
        List<DomainObject> objects = crudService.findAll("SO_Department", true);
        assertNotNull(objects);
        for (DomainObject object : objects) {
            if (!object.getTypeName().equalsIgnoreCase("SO_Department")) {
                fail("findAll with exactType==true returned inherited type");
            }
        }

        // Test findAll with exactType == false
        objects = crudService.findAll("SO_Department");
        assertNotNull(objects);
        assertTrue(objects.size() > 0);

        for (DomainObject object : objects) {
            if (object.getString("TelexExt") != null && object.getString("TelexExt").equals("TelexExt") &&
                    object.getString("NoticesFormulaIDs") != null &&
                    object.getString("NoticesFormulaIDs").equals("NoticesFormulaIDs2")) {
                return;
            }
        }

        fail("Objects wasn't found by findAll()");
    }

    @Test
    public void testFindAuditLogs() throws LoginException {
        DomainObject organization = createOrganizationDomainObject();
        DomainObject savedOrganization = crudService.save(organization);
        DomainObject department = createDepartmentDomainObject(savedOrganization);
        DomainObject savedDepartment = crudService.save(department);
        GlobalSettingsConfig globalSettings = configurationExplorer.getGlobalSettings();
        Boolean isAuditLogEnabled = false;
        if (globalSettings != null && globalSettings.getAuditLog() != null) {
            isAuditLogEnabled = globalSettings.getAuditLog().isEnable();
        }

        // Test by ADMIN
        LoginContext lc = login(ADMIN, ADMIN);
        lc.login();

        List<DomainObject> linkedAuditObjects =
                crudService.findLinkedDomainObjects(savedDepartment.getId(), "Department_al", "domain_object_id");

        assertNotNull(linkedAuditObjects);
        if (isAuditLogEnabled) {
            assertTrue(linkedAuditObjects.size() >= 1);
            assertEquals(linkedAuditObjects.get(0).getReference("domain_object_id"), savedDepartment.getId());
        }

        DomainObject foundAuditLog = crudService.find(linkedAuditObjects.get(0).getId());
        assertNotNull(foundAuditLog);
        assertNotNull(foundAuditLog.getId());
        assertEquals(foundAuditLog.getReference("domain_object_id"), savedDepartment.getId());
        lc.logout();

        // Test by PERSON2 (not ADMIN)
        lc = login(PERSON_2_LOGIN, ADMIN);
        lc.login();

        linkedAuditObjects =
                crudService.findLinkedDomainObjects(savedDepartment.getId(), "Department_al", "domain_object_id");
        assertNotNull(linkedAuditObjects);
        if (isAuditLogEnabled) {
            assertTrue(linkedAuditObjects.size() >= 1);
            assertEquals(linkedAuditObjects.get(0).getReference("domain_object_id"), savedDepartment.getId());
        }

        foundAuditLog = crudService.find(linkedAuditObjects.get(0).getId());
        assertNotNull(foundAuditLog);
        assertNotNull(foundAuditLog.getId());
        lc.logout();

        // Test by PERSON3 (not Admin) not having permissions to read department objects.
        lc = login(PERSON_3_LOGIN, ADMIN);
        lc.login();

        try {
            linkedAuditObjects =
                    crudService.findLinkedDomainObjects(savedDepartment.getId(), "Department_al", "domain_object_id");
        } catch (ObjectNotFoundException ex) {

        }
        assertNotNull(linkedAuditObjects);
        if (isAuditLogEnabled) {
            assertTrue(linkedAuditObjects.size() == 0);
        }
        lc.logout();

    }

    @Test
    public void testFindAuditLogsAsLinkedObjects() throws LoginException {
        DomainObject organization = createOrganizationDomainObject();
        DomainObject savedOrganization = crudService.save(organization);
        DomainObject department = createDepartmentDomainObject(savedOrganization);
        DomainObject savedDepartment = crudService.save(department);
        GlobalSettingsConfig globalSettings = configurationExplorer.getGlobalSettings();
        Boolean isAuditLogEnabled = false;
        if (globalSettings != null && globalSettings.getAuditLog() != null) {
            isAuditLogEnabled = globalSettings.getAuditLog().isEnable();
        }
        
        LoginContext lc = login(PERSON_2_LOGIN, ADMIN);
        lc.login();
     
        List<DomainObject> linkedAuditObjects =
                crudService.findLinkedDomainObjects(savedDepartment.getId(), "Department_al", "domain_object_id");
        assertNotNull(linkedAuditObjects);
        if(isAuditLogEnabled){
            assertTrue(linkedAuditObjects.size() >= 1);
            assertEquals(linkedAuditObjects.get(0).getReference("domain_object_id"), savedDepartment.getId());            
        }
                
        lc.logout();

        lc = login(PERSON_3_LOGIN, ADMIN);
        lc.login();

        linkedAuditObjects =
                crudService.findLinkedDomainObjects(savedDepartment.getId(), "Department_al", "domain_object_id");
        assertNotNull(linkedAuditObjects);
        assertNotNull(linkedAuditObjects);
        if (isAuditLogEnabled) {
            assertTrue(linkedAuditObjects.size() == 0);
        }

        lc.logout();

    }
    
    @Test
    public void testGetDomainObjectType() {
        DomainObject organization = createOrganizationDomainObject();
        DomainObject savedOrganization = crudService.save(organization);
        String type = crudService.getDomainObjectType(savedOrganization.getId());
        assertEquals(type, "Organization");
    }

    private DomainObject createPersonDomainObject() {
        DomainObject personDomainObject = crudService.createDomainObject("Person");
        personDomainObject.setString("Login", "login " + System.currentTimeMillis());
        return personDomainObject;
    }

    private DomainObject createPersonTestDomainObject() {
        DomainObject personDomainObject = crudService.createDomainObject("person_test_empty_fields");
        personDomainObject.setString("Login", "login test" + System.currentTimeMillis());
        return personDomainObject;
    }
    
    private DomainObject createEmployeeDomainObject(DomainObject departmentObject) {
        DomainObject personDomainObject = crudService.createDomainObject("Employee");
        
        personDomainObject.setString("Name", "Name " + System.currentTimeMillis());
        personDomainObject.setString("Position", "Position " + System.currentTimeMillis());
        personDomainObject.setString("Phone", "" + System.currentTimeMillis());        
        personDomainObject.setReference("Department", departmentObject.getId());
        
        return personDomainObject;
    }

    private DomainObject createOrganizationDomainObject() {
        DomainObject organizationDomainObject = crudService.createDomainObject("Organization");
        organizationDomainObject.setString("Name", "Organization" + new Date());
        return organizationDomainObject;
    }

    private DomainObject createDepartmentDomainObject(DomainObject savedOrganizationObject) {
        return createDepartmentDomainObject("department1", savedOrganizationObject);
    }

    private DomainObject createDepartmentDomainObject(String depName, DomainObject savedOrganizationObject) {
        DomainObject departmentDomainObject = crudService.createDomainObject("Department");
        departmentDomainObject.setString("Name", depName != null ? "department1" : depName);
        departmentDomainObject.setReference("Organization", savedOrganizationObject.getId());
        return departmentDomainObject;
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

    private DomainObject createDepartmentTest2DomainObject(DomainObject organizationDomainObject) {
        
        DomainObject departmentDomainObject = crudService.createDomainObject("department_test2");
        departmentDomainObject.setString("Name", "Departmment");
        departmentDomainObject.setLong("Number1", new Long(1));
        departmentDomainObject.setLong("Number2", new Long(2));
        
        departmentDomainObject.setTimestamp("Date1", new Date());
        departmentDomainObject.setTimestamp("Date2", new Date());

        departmentDomainObject.setReference("Organization", organizationDomainObject.getId());
        return departmentDomainObject;
    }

    private DomainObject createEmployeeTestDomainObject(DomainObject departmentObject) {
        DomainObject employeeDomainObject = crudService.createDomainObject("employee_test_ref_matrix");
        
        employeeDomainObject.setString("Name", "Name " + System.currentTimeMillis());
        employeeDomainObject.setString("Position", "Position " + System.currentTimeMillis());
        employeeDomainObject.setString("Phone", "" + System.currentTimeMillis()); 
        employeeDomainObject.setString("Login", "Login" + System.currentTimeMillis()); 
        employeeDomainObject.setString("EMail", "Email" + System.currentTimeMillis()); 
        
        employeeDomainObject.setReference("Department", departmentObject.getId());
        
        return employeeDomainObject;
    }
    private DomainObject createOrganizationTestDomainObject() {
        DomainObject organizationDomainObject = crudService.createDomainObject("organization_test");
        organizationDomainObject.setString("Name", "Organization");
        return organizationDomainObject;
    }

    @Test
    public void testFindByUniqueKey() throws Exception{

        DomainObject employeeTestUniqueKey = createEmployeeTestUniqueKey();
        Id organizationId = employeeTestUniqueKey.getReference("referenceField");

        Map<String, Value> paramsSimpleKey = new HashMap<>();
        paramsSimpleKey.put("newField", new StringValue(employeeTestUniqueKey.getString("newField")));

        DomainObject do1 = crudService.findByUniqueKey("EmployeeTestUniqueKey", paramsSimpleKey);
        assertEquals(employeeTestUniqueKey.getId(), do1.getId());

        paramsSimpleKey.put("newField", new StringValue("key2err"));
        try {
            DomainObject result = crudService.findByUniqueKey("EmployeeTestUniqueKey", paramsSimpleKey);
            assertNull(result);
        } catch (ObjectNotFoundException e) {
            assertTrue(false);
        }

        Map<String, Value> paramsComplexKey = new HashMap<>();
        paramsComplexKey.put("booleanField", new BooleanValue(true));
        paramsComplexKey.put("stringField", new StringValue("str"));
        paramsComplexKey.put("dateTimeField", new DateTimeValue(new SimpleDateFormat("yyyy-MM-dd").parse("2014-07-08")));
        paramsComplexKey.put("dateTimeWithTimeZoneField", new DateTimeWithTimeZoneValue(new DateTimeWithTimeZone(2, 2014, 3, 3)));
        paramsComplexKey.put("longField", new LongValue(1004L));
        paramsComplexKey.put("referenceField", new ReferenceValue(organizationId));
        paramsComplexKey.put("timelessDateField", new TimelessDateValue(new TimelessDate(2014, 3, 3)));
        paramsComplexKey.put("decimalField", new DecimalValue(new BigDecimal("1.2")));
        paramsComplexKey.put("textField", new StringValue("txt"));

        DomainObject do2 = crudService.findByUniqueKey("EmployeeTestUniqueKey", paramsComplexKey);
        assertEquals(employeeTestUniqueKey.getId(), do2.getId());

        paramsComplexKey.put("stringField", new StringValue("key2err"));
        try {
            DomainObject result = crudService.findByUniqueKey("EmployeeTestUniqueKey", paramsComplexKey);
            assertNull(result);
        } catch (ObjectNotFoundException e) {
            assertTrue(false);
        }

        paramsComplexKey.put("stringField", new StringValue("str"));
        DomainObject do3 = crudService.findAndLockByUniqueKey("EmployeeTestUniqueKey", paramsComplexKey);
        assertEquals(employeeTestUniqueKey.getId(), do3.getId());

        paramsComplexKey.put("extraField", new StringValue("txt"));
        try {
            crudService.findByUniqueKey("EmployeeTestUniqueKey", paramsComplexKey);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void testFindByUniqueKeyCaseInsensitive() throws Exception{
        DomainObject country = createCountryDomainObject();
        String countryName = country.getString("Name");
        
        DomainObject savedCountry = crudService.save(country);

        Map<String, Value> uniqueKeyValues = new HashMap<>();
        uniqueKeyValues.put("name", new StringValue(countryName));
        DomainObject countryObject = crudService.findByUniqueKey("country_test", uniqueKeyValues);
        assertNotNull(countryObject);
        assertNotNull(countryObject.getId());
        
        uniqueKeyValues = new HashMap<>();
        uniqueKeyValues.put("nAme", new StringValue(countryName));
        countryObject = crudService.findByUniqueKey("country_test", uniqueKeyValues);
        assertNotNull(countryObject);
        assertNotNull(countryObject.getId());

    }

    @Test
    public void testFindByUniqueKeyByNonSuperUser() throws Exception {
        DomainObject testObject = crudService.createDomainObject("nunid2punid_map");
        String value = "value" + System.currentTimeMillis();
        testObject.setString("nunid", value);
        DomainObject savedTestObject = crudService.save(testObject);

        LoginContext lc = login("person2", "admin");
        lc.login();
        Map<String, Value> uniqueKeyValues = new HashMap<>();
        uniqueKeyValues.put("nunid", new StringValue(value));
        DomainObject result = crudService.findByUniqueKey("nunid2punid_map", uniqueKeyValues);
        assertNotNull(result);
        assertNotNull(result.getId());

        lc.logout();
    }
    
    private DomainObject createCountryDomainObject() {
        DomainObject organizationDomainObject = crudService.createDomainObject("country_test");
        organizationDomainObject.setString("Name", "Country" + System.currentTimeMillis());
        return organizationDomainObject;
    }

    private DomainObject createEmployeeTestUniqueKey() throws ParseException {

        DomainObject organization = createOrganizationDomainObject();
        organization = crudService.save(organization);

        DomainObject employee = crudService.createDomainObject("EmployeeTestUniqueKey");
        employee.setString("Name", "name4" + System.currentTimeMillis());
        employee.setString("Position", "admin4");
        employee.setString("Login", "login4" + System.currentTimeMillis());
        employee.setString("Email", "e-mail4" + System.currentTimeMillis());
        employee.setString("Phone", "+222" + System.currentTimeMillis());

        employee.setString("newField", "key2" + System.currentTimeMillis());

        employee.setBoolean("booleanField", true);
        employee.setString("stringField", "str");
        employee.setTimestamp("dateTimeField", new SimpleDateFormat("yyyy-MM-dd").parse("2014-07-08"));
        employee.setDateTimeWithTimeZone("dateTimeWithTimeZoneField", new DateTimeWithTimeZone(3, 2014, 3, 3));
        employee.setDecimal("decimalField", new BigDecimal("1.2"));
        employee.setLong("longField", 1004L);
        employee.setReference("referenceField", organization.getId());
        employee.setString("textField", "txt");
        employee.setTimelessDate("timelessDateField", new TimelessDate(2014, 3, 3));

        return crudService.save(employee);
    }

    @Test
    public void testFindByUniqueKeyInheritor() {

        Map<String, Value> paramsRefKey1 = new HashMap<>();
        paramsRefKey1.put("name", new StringValue("a_type_1"));

        Map<String, Value> paramsRefKey3 = new HashMap<>();
        paramsRefKey3.put("name", new StringValue("c_type_3"));

        try {
            DomainObject refDo1 = crudService.findByUniqueKey("atst_types", paramsRefKey1);
            assertNotNull(refDo1);

            DomainObject refDo3 = crudService.findByUniqueKey("atst_types", paramsRefKey3);
            assertNotNull(refDo3);

            Map<String, Value> paramsKey = new HashMap<>();
            paramsKey.put("base_id", new StringValue("1"));
            paramsKey.put("atst_type", new ReferenceValue(refDo1.getId()));

            DomainObject do1 = crudService.findByUniqueKey("atst_base_object", paramsKey);
            assertNotNull(do1);
            assertTrue("atst_base_object".equalsIgnoreCase(do1.getTypeName()));

            do1 = crudService.findByUniqueKey("atst_base_object", paramsKey);
            assertNotNull(do1);
            assertTrue("atst_base_object".equalsIgnoreCase(do1.getTypeName()));

            paramsKey.clear();
            paramsKey.put("base_id", new StringValue("3"));
            paramsKey.put("atst_type", new ReferenceValue(refDo3.getId()));

            DomainObject do3 = crudService.findByUniqueKey("atst_base_object", paramsKey);
            assertNotNull(do3);
            assertTrue("atst_object".equalsIgnoreCase(do3.getTypeName()));

            do3 = crudService.findByUniqueKey("atst_base_object", paramsKey);
            assertNotNull(do3);
            assertTrue("atst_object".equalsIgnoreCase(do3.getTypeName()));

            DomainObject do4 = crudService.find(do3.getId());
            assertNotNull(do4);

        } catch (ObjectNotFoundException e) {
            assertTrue(false);
        }
    }
}
