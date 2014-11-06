package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.model.ObjectNotFoundException;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

/**
 * Иитеграционный тест для {@link CrudService}
 * @author atsvetkov
 */
@RunWith(Arquillian.class)
public class CrudServiceIT extends IntegrationTestBase {

    private static final String ADMIN = "admin";

    private static final String PERSON_2_LOGIN = "person2";

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
    }

    @Test
    public void testSaveFindExists() {
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

    }

    @Test
    public void testFindAuditLogs() throws LoginException {
        Integer countryAlTypeid = domainObjectTypeIdCache.getId("country_al");
        Id countryAuditId = new RdbmsId(countryAlTypeid, 33);

        LoginContext lc = login(PERSON_2_LOGIN, ADMIN);
        lc.login();

        DomainObject foundCountryAudit = crudService.find(countryAuditId);
        assertNotNull(foundCountryAudit);
        assertNotNull(foundCountryAudit.getId());
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

        DomainObject person1 = createPersonDomainObject();
        DomainObject organization1 = createOrganizationDomainObject();

        DomainObject savedPerson1 = crudService.save(person1);

        DomainObject savedOrganization1 = crudService.save(organization1);

        Id[] objects = new Id[] {savedPerson1.getId(), savedOrganization1.getId() };
        List<Id> objectIds = Arrays.asList(objects);

        List<DomainObject> foundObjects = crudService.find(objectIds);

        List<Id> foundIds = getIdList(foundObjects);
        assertNotNull(foundObjects);
        assertTrue(foundObjects.size() == 2);

        assertTrue(foundIds.contains(savedPerson1.getId()));
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
    public void testFindLinkedDoaminObjects() {
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
    }

    @Test
    public void testFindLinkedDoaminObjectsForAuditLog() throws LoginException {
        DomainObject organization = createOrganizationDomainObject();
        DomainObject savedOrganization = crudService.save(organization);
        LoginContext lc = login("person2", "admin");
        lc.login();
        List<DomainObject> linkedObjects =
                crudService.findLinkedDomainObjects(savedOrganization.getId(), "Organization_al", "domain_object_id");
        assertNotNull(linkedObjects);
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
        DomainObject departmentDomainObject = crudService.createDomainObject("Department");
        departmentDomainObject.setString("Name", "department1");
        departmentDomainObject.setReference("Organization", savedOrganizationObject.getId());
        return departmentDomainObject;
    }

    @Test
    public void testFindByUniqueKey() throws Exception{

        DomainObject employeeTestUniqueKey = createEmployeeTestUniqueKey();
        Id organizationId = employeeTestUniqueKey.getReference("referenceField");

        Map<String, Value> paramsSimpleKey = new HashMap<>();
        paramsSimpleKey.put("newField", new StringValue(employeeTestUniqueKey.getString("key2")));

        DomainObject do1 = crudService.findByUniqueKey("EmployeeTestUniqueKey", paramsSimpleKey);
        assertEquals(employeeTestUniqueKey.getId(), do1.getId());

        paramsSimpleKey.put("newField", new StringValue("key2err"));
        try {
            crudService.findByUniqueKey("EmployeeTestUniqueKey", paramsSimpleKey);
            assertTrue(false);
        } catch (ObjectNotFoundException e) {
            assertTrue(true);
        }

        Map<String, Value> paramsComplexKey = new HashMap<>();
        paramsComplexKey.put("booleanField", new BooleanValue(true));
        paramsComplexKey.put("stringField", new StringValue("str"));
        paramsComplexKey.put("dateTimeField", new DateTimeValue(new SimpleDateFormat("yyyy-MM-dd").parse("2014-07-08")));
        paramsComplexKey.put("dateTimeWithTimeZoneField", new DateTimeWithTimeZoneValue(new DateTimeWithTimeZone(2, 2014, 3, 3)));
        paramsComplexKey.put("longField", new LongValue(1004L));
        paramsComplexKey.put("referenceField", new ReferenceValue(organizationId));
        paramsComplexKey.put("textField", new StringValue("txt"));
        paramsComplexKey.put("timelessDateField", new TimelessDateValue(new TimelessDate(2014, 3, 3)));
        paramsComplexKey.put("decimalField", new DecimalValue(new BigDecimal("1.2")));

        DomainObject do2 = crudService.findByUniqueKey("EmployeeTestUniqueKey", paramsComplexKey);
        assertEquals(employeeTestUniqueKey.getId(), do2.getId());

        paramsComplexKey.put("textField", new StringValue("key2err"));
        try {
            crudService.findByUniqueKey("EmployeeTestUniqueKey", paramsComplexKey);
            assertTrue(false);
        } catch (ObjectNotFoundException e) {
            assertTrue(true);
        }

        paramsComplexKey.put("textField", new StringValue("txt"));
        DomainObject do3 = crudService.findAndLockByUniqueKey("EmployeeTestUniqueKey", paramsComplexKey);
        assertEquals(employeeTestUniqueKey.getId(), do3.getId());

        paramsComplexKey.put("extraField", new StringValue("txt"));
        try {
            crudService.findByUniqueKey("EmployeeTestUniqueKey", paramsComplexKey);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }

        crudService.delete(employeeTestUniqueKey.getId());
        crudService.delete(organizationId);

        DomainObject country = createCountryDomainObject();
        String countryName = country.getString("Name");
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
}
