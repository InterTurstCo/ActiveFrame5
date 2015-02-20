package ru.intertrust.cm.core.service.it;

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

import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class DomainObjectCacheServiceIT  extends IntegrationTestBase {

    @EJB
    private PersonService.Remote personService;
    
    @Inject
    private UserTransaction utx;

    private PersonManagementServiceDao personManagementServiceDao;
    
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
}
