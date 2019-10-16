package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.auth.login.LoginContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.dao.api.CollectionQueryCache;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

@RunWith(Arquillian.class)
public class DomainObjectCacheServiceIT  extends IntegrationTestBase {

    @EJB
    private PersonService.Remote personService;
    
    @Inject
    private UserTransaction utx;

    private PersonManagementServiceDao personManagementServiceDao;
    
    private CollectionQueryCache collectionQueryCache;
    
    @EJB
    private CollectionsService.Remote collectionService;
    
    @Before
    public void init() {
        initializeSpringBeans();        
    }
    
    private void initializeSpringBeans() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        personManagementServiceDao = applicationContext.getBean(PersonManagementServiceDao.class);
        domainObjectTypeIdCache = applicationContext.getBean(DomainObjectTypeIdCache.class);
    }
    
    @Test
    public void testGetObjectCollection() throws Exception{
        LoginContext lc = login("admin", "admin");
        lc.login();
        
        Id personId = personService.findPersonByLogin("person2").getId();        
        utx.begin();
        
        List<DomainObject> objects = personManagementServiceDao.getPersonGroups(personId);
        List<DomainObject> cachedObjects = personManagementServiceDao.getPersonGroups(personId);

        assertEquals(objects.size(), cachedObjects.size());
        
        int userGroupType = domainObjectTypeIdCache.getId("user_group");

        Id userGroupId = new RdbmsId(userGroupType, 1);
        objects = personManagementServiceDao.getPersonsInGroup(userGroupId);
        cachedObjects = personManagementServiceDao.getPersonsInGroup(userGroupId);        
        assertEquals(objects.size(), cachedObjects.size());
        
        objects = personManagementServiceDao.getAllChildGroups(userGroupId);
        cachedObjects = personManagementServiceDao.getAllChildGroups(userGroupId);
        assertEquals(objects.size(), cachedObjects.size());
        
        objects = personManagementServiceDao.getAllParentGroup(userGroupId);
        cachedObjects = personManagementServiceDao.getAllParentGroup(userGroupId);
        assertEquals(objects.size(), cachedObjects.size());
        
        objects = personManagementServiceDao.getAllPersonsInGroup(userGroupId);
        cachedObjects = personManagementServiceDao.getAllPersonsInGroup(userGroupId);
        assertEquals(objects.size(), cachedObjects.size());
        
        utx.commit();
        
        lc.logout();
    }
    @Test
    public void testCollectionQueryCache() throws Exception {
        LoginContext lc = login("admin", "admin");
        lc.login();
        
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("id", Order.ASCENDING));

        List<Filter> filterValues = new ArrayList<Filter>();
        Filter filter = new Filter();
        filter.setFilter("byNullDepartment");
        filterValues.add(filter);

        utx.begin();
        long start = System.currentTimeMillis();
        
        IdentifiableObjectCollection testCollection = collectionService.findCollection("Employees_Test", sortOrder, filterValues, 0, 0);
        assertNotNull(testCollection);
        long end = System.currentTimeMillis();
        System.out.println("Time to fetch collection Employees_Test : " + (end - start) + " ms");

        start = System.currentTimeMillis();
        testCollection = collectionService.findCollection("Employees_Test", sortOrder, filterValues, 0, 0);
        end = System.currentTimeMillis();
        System.out.println("Time to fetch collection Employees_Test using cached query: " + (end - start) + " ms");
        
        testCollection = collectionService.findCollection("Employees_Test", sortOrder, filterValues, 0, 0);

        utx.commit();
    }
}
