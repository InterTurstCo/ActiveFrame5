package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

/**
 * Интеграционный тест работы с коллекциями.
 * @author atsvetkov
 *
 */
@RunWith(Arquillian.class)
public class CollectionsIT extends IntegrationTestBase {

    private static final String DEPARTMENT_2 = "Подразделение 2";

    private static final String DEPARTMENT_1 = "Подразделение 1";

    private static final String EMPLOYEE_1_NAME = "Сотрудник 1";

    @EJB
    private CollectionsService.Remote collectionService;

    @Inject
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    private boolean isDataLoaded = false;
    @Deployment
    public static Archive<EnterpriseArchive> createDeployment() {
        return createDeployment(new Class[] {CollectionsIT.class }, new String[] {"test-data/import-department.csv",
                "test-data/import-organization.csv",
                "test-data/import-employee.csv" });
    }

    @Test
    public void testArquillianInjection() {
        Assert.assertNotNull(collectionService);
    }

    @Before
    public void init() throws IOException, LoginException {
        LoginContext lc = login("admin", "admin");
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
    }
    
    @Test
    public void testFindCollectionByQuery() {
        String query = "select t.id from Employee t where t.Name='" + EMPLOYEE_1_NAME + "'";
        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);
        assertNotNull(collection);
        assertTrue(collection.size() >= 1);
    }

    @Test
    public void testFindCollectionByQueryWithParams() {
        String query = "select * from Employee e where e.department = {0} and name = {1}";
        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue(new RdbmsId(5013, 1).toStringRepresentation()));
        params.add(new StringValue(EMPLOYEE_1_NAME));

/*        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query, params);

        assertNotNull(collection);
        assertTrue(collection.size() >= 1);
*/
    }
    
    @Test
    public void testFindCollectionWithFilters() {
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("id", Order.ASCENDING));

        List<Filter> filterValues = new ArrayList<Filter>();
        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new LongValue(1));

        filterValues.add(filter);
        IdentifiableObjectCollection employeesCollection =
                collectionService.findCollection("Employees", sortOrder, filterValues, 0, 0);

        assertNotNull(employeesCollection);
        assertTrue(employeesCollection.size() >= 1);

        testFindCollectionWithFilterMultiCriterion(sortOrder);

    }

    private void testFindCollectionWithFilterMultiCriterion(SortOrder sortOrder) {
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
}
