package ru.intertrust.cm.core.service.it;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.transaction.*;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author atsvetkov
 */
@RunWith(Arquillian.class)
public class GlobalCacheIT extends IntegrationTestBase {
    private static final Logger logger = LoggerFactory.getLogger(GlobalCacheIT.class);

    private static final String CITY_NAME_BY_THREAD1 = "CityModifiedByThread1";

    private static final String CITY_NAME_INITIAL_PREFIX = "CityInitial";

    private static final String CITY_NAME_BY_PERSON2_PREFIX = "CityModifiedByPerson2";

    private static final String CITY_NAME_BY_PERSON1_PREFIX = "CityModifiedByPerson1";

    private static final String CITY_NAME_BY_SYSTEM_PREFIX = "CityModifiedBySystem";

    private static final String PERSON1 = "person1";

    private static final String PERSON2 = "person2";

    private static final String ADMIN = "admin";

    @EJB
    private CrudService.Remote crudService;

    private AccessControlService accessControlService;
    private DomainObjectDao domainObjectDao;

    private DomainObjectCacheService domainObjectCacheService;

    @Inject
    private UserTransaction utx;

    private void initializeSpringBeans() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        domainObjectTypeIdCache = applicationContext.getBean(DomainObjectTypeIdCache.class);
        domainObjectDao = applicationContext.getBean(DomainObjectDao.class);
        accessControlService = applicationContext.getBean(AccessControlService.class);
        domainObjectCacheService = (DomainObjectCacheService) applicationContext.getBean("domainObjectCacheService");
    }

    public DomainObjectDao getDomainObjectDao() {
        return domainObjectDao;
    }

    public UserTransaction getUtx() {
        return utx;
    }

    @Before
    public void init() throws IOException, LoginException {
        initializeSpringBeans();
    }

    @Test
    public void testFindWithCacheEnabled() throws LoginException, Exception {
        LoginContext lc = login(ADMIN, "admin");
        lc.login();

        DomainObject country = createCountryDomainObject();
        DomainObject savedCountry = crudService.save(country);

        DomainObject city = createCityTestDomainObject(savedCountry);
        DomainObject savedCity = crudService.save(city);
        final Id cityId = savedCity.getId();

        city = createCityTestDomainObject(savedCountry);
        savedCity = crudService.save(city);
        final Id cityId2 = savedCity.getId();

        DomainObject friendCity = createFriendCityTestDomainObject(savedCity);
        DomainObject savedFriendCity = crudService.save(city);
        final Id friendCityId = savedFriendCity.getId();

        friendCity = createFriendCityTestDomainObject(savedCity);
        savedFriendCity = crudService.save(city);
        final Id friendCityId2 = savedFriendCity.getId();

        final AccessToken systemAccessToken = accessControlService.createSystemAccessToken("GlobalCacheIT");
        final AccessToken accessTokenCityReadForPerson1 = accessControlService.createAccessToken(PERSON1, cityId, DomainObjectAccessType.READ);
        final AccessToken accessTokenCityWriteForPerson1 = accessControlService.createAccessToken(PERSON1, cityId, DomainObjectAccessType.WRITE);
        final AccessToken accessTokenCityReadForPerson2 = accessControlService.createAccessToken(PERSON2, cityId, DomainObjectAccessType.READ);

        final AccessToken accessTokenCity2ReadForPerson1 = accessControlService.createAccessToken(PERSON1, cityId2, DomainObjectAccessType.READ);
        final AccessToken accessTokenCity2WriteForPerson1 = accessControlService.createAccessToken(PERSON1, cityId2, DomainObjectAccessType.WRITE);

        final AccessToken accessTokenFriendCityReadForPerson1 = accessControlService.createAccessToken(PERSON1, friendCityId, DomainObjectAccessType.READ);
        final AccessToken accessTokenFriendCityWriteForPerson1 = accessControlService.createAccessToken(PERSON1, friendCityId, DomainObjectAccessType.WRITE);
        final AccessToken accessTokenFriendCityReadForPerson2 = accessControlService.createAccessToken(PERSON2, friendCityId, DomainObjectAccessType.READ);

        final AccessToken accessTokenFriendCity2ReadForPerson1 = accessControlService.createAccessToken(PERSON1, friendCityId2, DomainObjectAccessType.READ);
        final AccessToken accessTokenFriendCity2WriteForPerson1 = accessControlService.createAccessToken(PERSON1, friendCityId2, DomainObjectAccessType.WRITE);

        // 1. Test sequential read in 2 transactions.
        testSequentialReadInTransactionsBySystem(cityId, systemAccessToken);
        testSequentialReadInTransactionsBySystem(friendCityId, systemAccessToken);

        testSequentialReadInTransactionsByPerson1(cityId, accessTokenCityReadForPerson1);
        testSequentialReadInTransactionsByPerson1(friendCityId, accessTokenFriendCityReadForPerson1);

        testSequentialReadInTransactionsByPerson2(cityId, accessTokenCityReadForPerson2);
        testSequentialReadInTransactionsByPerson2(friendCityId, accessTokenFriendCityReadForPerson2);

        // 2. Test read, update, commit and read in new transaction
        testReadUpdateCommitReadCycleBySystem(cityId, systemAccessToken);

        testReadUpdateCommitReadCycleByPerson1(cityId, accessTokenCityReadForPerson1, accessTokenCityWriteForPerson1);

        testReadUpdateCommitReadCycleByPerson2(cityId, systemAccessToken, accessTokenCityReadForPerson1, accessTokenCityReadForPerson2);

        testReadUpdateCommitReadCycleBySystem(friendCityId, systemAccessToken);

        testReadUpdateCommitReadCycleByPerson1(friendCityId, accessTokenFriendCityReadForPerson1, accessTokenFriendCityWriteForPerson1);

        testReadUpdateCommitReadCycleByPerson2(friendCityId, systemAccessToken, accessTokenFriendCityReadForPerson1, accessTokenFriendCityReadForPerson2);

        // 3. Test parallel transaction scenarios
        testReadUpdateReadInMultileTransactions(cityId, systemAccessToken, systemAccessToken);
        testReadUpdateReadInMultileTransactions(cityId2, accessTokenCity2ReadForPerson1, accessTokenCity2WriteForPerson1);

        testReadUpdateReadInMultileTransactions(friendCityId, systemAccessToken, systemAccessToken);
        testReadUpdateReadInMultileTransactions(friendCityId2, accessTokenFriendCity2ReadForPerson1, accessTokenFriendCity2WriteForPerson1);

    }

    private void testReadUpdateReadInMultileTransactions(final Id cityId, final AccessToken readAccessToken, final AccessToken writeAccessToken)
            throws NotSupportedException, SystemException,
            RollbackException, HeuristicMixedException, HeuristicRollbackException, InterruptedException {
        utx.begin();
        DomainObject domainObject = domainObjectDao.find(cityId, readAccessToken);
        domainObject.setString("Name", CITY_NAME_INITIAL_PREFIX + System.currentTimeMillis());
        DomainObject modifiedDomainObject = domainObjectDao.save(domainObject, writeAccessToken);
        assertNotNull(modifiedDomainObject);
        utx.commit();

        Thread thread1 = new Thread() {
            public void run() {
                synchronized (cityId) {
                    try {
                        getUtx().begin();
                        // 1. read and update in 1-st transaction
                        DomainObject domainObject = getDomainObjectDao().find(cityId, readAccessToken);
                        assertTrue(domainObject.getString("Name").startsWith(CITY_NAME_INITIAL_PREFIX));

                        domainObject.setString("Name", CITY_NAME_BY_THREAD1 + System.currentTimeMillis());
                        DomainObject modifiedDomainObject = getDomainObjectDao().save(domainObject, writeAccessToken);
                        assertTrue(modifiedDomainObject.getString("Name").startsWith(CITY_NAME_BY_THREAD1));

                        logger.info("1-st transaction updated, not committed");

                        cityId.wait();
                        // 3. second transaction should read DomainObject and not see uncommitted changes
                        getUtx().commit();
                        cityId.notify();
                        logger.info("1-st transaction committed");

                        domainObject = getDomainObjectDao().find(cityId, readAccessToken);
                        assertTrue(domainObject.getString("Name").startsWith(CITY_NAME_BY_THREAD1));
                        logger.info("1-st transaction finished");
                    } catch (Exception e) {
                        logger.error("Error in test: " + e.getMessage());
                    }

                }
            }
        };

        Thread thread2 = new Thread() {
            public void run() {
                synchronized (cityId) {
                    try {
                        getUtx().begin();
                        // 2. Read uncommitted object from 2-d transaction
                        logger.info("2-st transaction read uncommitted changes");

                        DomainObject domainObject = getDomainObjectDao().find(cityId, readAccessToken);
                        assertTrue(domainObject.getString("Name").startsWith(CITY_NAME_INITIAL_PREFIX));
                        cityId.notify();
                        cityId.wait();
                        logger.info("2-st transaction read after commit");

                        logger.info("Name is: " + domainObject.getString("Name"));
                        // 4. 1-st transaction committed, check committed changes
                        domainObjectCacheService.evict(cityId);
                        domainObject = getDomainObjectDao().find(cityId, readAccessToken);
                        assertTrue(domainObject.getString("Name").startsWith(CITY_NAME_BY_THREAD1));

                        getUtx().commit();
                        logger.info("2-st transaction finished");
                    } catch (Exception e) {
                        logger.error("Error in test: " + e.getMessage());
                    }
                }
            }
        };

        thread1.start();
        Thread.sleep(1000);
        thread2.start();

        thread1.join();
        thread2.join();

    }

    private void testReadUpdateCommitReadCycleByPerson2(final Id cityId, final AccessToken systemAccessToken,
            final AccessToken simpleAccessTokenReadForPerson1, final AccessToken simpleAccessTokenReadForPerson2) throws Exception {
        DomainObject domainObject;
        DomainObject modifiedDomainObject;
        utx.begin();
        domainObject = domainObjectDao.find(cityId, simpleAccessTokenReadForPerson2);
        assertTrue(domainObject == null);
        domainObject = domainObjectDao.find(cityId, simpleAccessTokenReadForPerson1);
        assertNotNull(domainObject);
        assertNotNull(domainObject.getId());
        domainObject.setString("Name", CITY_NAME_BY_PERSON2_PREFIX + System.currentTimeMillis());
        modifiedDomainObject = domainObjectDao.save(domainObject, systemAccessToken);
        assertNotNull(modifiedDomainObject);
        utx.commit();

        utx.begin();
        domainObject = domainObjectDao.find(cityId, simpleAccessTokenReadForPerson2);
        assertTrue(domainObject == null);
        domainObject = domainObjectDao.find(cityId, simpleAccessTokenReadForPerson1);
        assertNotNull(domainObject);
        assertNotNull(domainObject.getId());
        utx.commit();
    }

    private void testReadUpdateCommitReadCycleByPerson1(final Id cityId, final AccessToken simpleAccessTokenReadForPerson1,
            final AccessToken simpleAccessTokenWriteForPerson1) throws Exception {
        DomainObject domainObject;
        DomainObject modifiedDomainObject;
        utx.begin();
        domainObject = domainObjectDao.find(cityId, simpleAccessTokenReadForPerson1);
        assertNotNull(domainObject);
        assertNotNull(domainObject.getId());
        domainObject.setString("Name", CITY_NAME_BY_PERSON1_PREFIX + System.currentTimeMillis());
        modifiedDomainObject = domainObjectDao.save(domainObject, simpleAccessTokenWriteForPerson1);
        assertNotNull(modifiedDomainObject);
        utx.commit();

        utx.begin();
        domainObject = domainObjectDao.find(cityId, simpleAccessTokenReadForPerson1);
        assertNotNull(domainObject);
        assertTrue(domainObject.getString("Name").startsWith(CITY_NAME_BY_PERSON1_PREFIX));
        utx.commit();
    }

    private void testReadUpdateCommitReadCycleBySystem(final Id cityId, final AccessToken systemAccessToken) throws Exception {
        DomainObject domainObject;
        utx.begin();
        domainObject = domainObjectDao.find(cityId, systemAccessToken);
        assertNotNull(domainObject);
        assertNotNull(domainObject.getId());
        domainObject.setString("Name", CITY_NAME_BY_SYSTEM_PREFIX + System.currentTimeMillis());
        DomainObject modifiedDomainObject = domainObjectDao.save(domainObject, systemAccessToken);
        assertNotNull(modifiedDomainObject);
        utx.commit();

        utx.begin();
        domainObject = domainObjectDao.find(cityId, systemAccessToken);
        assertNotNull(domainObject);
        assertTrue(domainObject.getString("Name").startsWith(CITY_NAME_BY_SYSTEM_PREFIX));
        utx.commit();
    }

    private void testSequentialReadInTransactionsBySystem(Id cityId, AccessToken systemAccessToken)
            throws NotSupportedException, SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        utx.begin();
        DomainObject domainObject = domainObjectDao.find(cityId, systemAccessToken);
        assertNotNull(domainObject);
        assertNotNull(domainObject.getId());
        utx.commit();

        utx.begin();
        domainObject = domainObjectDao.find(cityId, systemAccessToken);
        assertNotNull(domainObject);
        assertNotNull(domainObject.getId());
        utx.commit();

    }

    private void testSequentialReadInTransactionsByPerson1(Id cityId, AccessToken simpleAccessTokenForPerson1)
            throws NotSupportedException, SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        utx.begin();
        DomainObject domainObject = domainObjectDao.find(cityId, simpleAccessTokenForPerson1);
        assertNotNull(domainObject);
        assertNotNull(domainObject.getId());
        utx.commit();

        utx.begin();
        domainObject = domainObjectDao.find(cityId, simpleAccessTokenForPerson1);
        assertNotNull(domainObject);
        assertNotNull(domainObject.getId());
        utx.commit();

    }

    private void testSequentialReadInTransactionsByPerson2(Id cityId, AccessToken simpleAccessTokenForPerson2)
            throws NotSupportedException, SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        utx.begin();
        DomainObject domainObject = domainObjectDao.find(cityId, simpleAccessTokenForPerson2);
        assertTrue(domainObject == null);
        utx.commit();

        utx.begin();
        domainObject = domainObjectDao.find(cityId, simpleAccessTokenForPerson2);
        assertTrue(domainObject == null);
        utx.commit();

    }

    private DomainObject createCityTestDomainObject(DomainObject savedCountryObject) {
        DomainObject domainObject = crudService.createDomainObject("city_test");
        domainObject.setString("Name", CITY_NAME_INITIAL_PREFIX + new Date());
        if (savedCountryObject != null) {
            domainObject.setReference("country", savedCountryObject.getId());
        }
        return domainObject;
    }

    private DomainObject createFriendCityTestDomainObject(DomainObject savedCityObject) {
        DomainObject domainObject = crudService.createDomainObject("friend_city_test");
        domainObject.setString("Name", "FriendCityInitial" + new Date());
        if (savedCityObject != null) {
            domainObject.setReference("city", savedCityObject.getId());
        }
        return domainObject;
    }

    private DomainObject createCountryDomainObject() {
        DomainObject organizationDomainObject = crudService.createDomainObject("country_test");
        organizationDomainObject.setString("name", "Country" + System.currentTimeMillis());
        return organizationDomainObject;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Test");

        final Object cityId = new Object();
        final Object sequenceLock = new Object();

        testInMultipleThreads(cityId);
        testInMultipleThreads(cityId);

    }

    private static void testInMultipleThreads(final Object cityId) throws InterruptedException {
        Thread thread1 = new Thread() {
            public void run() {
                synchronized (cityId) {
                    /*
                     * synchronized (sequenceLock) { try { sequenceLock.notify(); } catch (Exception e) {
                     * e.printStackTrace(); }
                     * 
                     * }
                     */
                    try {
                        System.out.println("Begin 1-t thread");

                        cityId.wait();
                        System.out.println("1-t thread commits");
                        cityId.notify();
                        System.out.println("2-t thread could recheck");

                    } catch (SecurityException | IllegalStateException | InterruptedException e) {
                    }

                }
            }
        };

        Thread thread2 = new Thread() {
            public void run() {
                /*
                 * synchronized (sequenceLock) { try { sequenceLock.wait(); } catch (Exception e) { e.printStackTrace();
                 * } }
                 */synchronized (cityId) {
                    try {
                        System.out.println("Begin 2-t thread");

                        cityId.notify();
                        System.out.println("1-t thread should commit from 2-d thread");

                        cityId.wait();
                        System.out.println("2-t thread finished");

                    } catch (SecurityException | IllegalStateException | InterruptedException e) {
                    }
                }
            }
        };

        thread1.start();
        Thread.sleep(1000);
        thread2.start();

        thread1.join();
        thread2.join();
    }
}
