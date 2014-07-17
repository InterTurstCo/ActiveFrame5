package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.model.ObjectNotFoundException;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

/**
 * Иитеграционный тест для {@link CrudService}
 * @author atsvetkov
 */
@RunWith(Arquillian.class)
public class CrudServiceIT extends IntegrationTestBase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
    public void testGetDomainObjectType() {
        DomainObject organization = createOrganizationDomainObject();
        DomainObject savedOrganization = crudService.save(organization);
        String type = crudService.getDomainObjectType(savedOrganization.getId());
        assertEquals(type, "Organization");
    }

    private DomainObject createPersonDomainObject() {
        DomainObject personDomainObject = crudService.createDomainObject("Person");
        personDomainObject.setString("Login", "login " + new Date());
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
        paramsSimpleKey.put("newField", new StringValue("key2"));

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


    }

    private DomainObject createEmployeeTestUniqueKey() throws ParseException {

        DomainObject organization = createOrganizationDomainObject();
        organization = crudService.save(organization);

        DomainObject employee = crudService.createDomainObject("EmployeeTestUniqueKey");
        employee.setString("Name", "name4");
        employee.setString("Position", "admin4");
        employee.setString("Login", "login4");
        employee.setString("Email", "e-mail4");
        employee.setString("Phone", "+1132514");

        employee.setString("newField", "key2");

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
