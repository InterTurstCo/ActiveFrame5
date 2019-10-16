package ru.intertrust.cm.core.dao.impl.access;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;

/**
 * Тест для CollectionsDao.
 * @author atsvetkov
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectionsDaoTest extends BaseDaoTest {

    @Test
    public void testFindCollection() {
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("id", Order.ASCENDING));
        List<Filter> filterValues = new ArrayList<Filter>();
        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new StringValue("department1"));

        filterValues.add(filter);
        IdentifiableObjectCollection objectCollection =
                collectionsDao.findCollection("Employees", filterValues, sortOrder, 0, 0, accessToken);
        assertNotNull(objectCollection);
        assertTrue(objectCollection.size() >= 1);
    }

    @Test
    public void testFindCollectionWithMultiCriterion() {
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("id", Order.ASCENDING));
        List<Filter> filterValues = new ArrayList<Filter>();
        Filter filter = new Filter();
        filter.setFilter("byDepartmentNames");
        List<Value> departmentNames = new ArrayList<Value>();
        departmentNames.add(new StringValue("department1"));
        departmentNames.add(new StringValue("department2"));

        filter.addMultiCriterion(0, departmentNames);
        filterValues.add(filter);

        IdentifiableObjectCollection objectCollection =
                collectionsDao.findCollection("Employees", filterValues, sortOrder, 0, 0, accessToken);
        
        System.out.print(objectCollection);
        assertNotNull(objectCollection);
        assertTrue(objectCollection.size() >= 1);

    }
    
    @Test
    public void testFindCollectionByQuery() {

        String query = "select ai.id, ai.created_date, ai.status, user_uid, password from authentication_info ai where user_uid='admin'";

        IdentifiableObjectCollection identifiableObjectCollection = collectionsDao.findCollectionByQuery(query, 0, 0, accessToken);
        assertNotNull(identifiableObjectCollection);
        
        System.out.print(identifiableObjectCollection);

    }    

    
}
