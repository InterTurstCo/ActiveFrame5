package ru.intertrust.cm.core.dao.impl.access;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.FileUtils;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.impl.*;
import ru.intertrust.cm.core.dao.impl.doel.DoelResolver;

import javax.transaction.TransactionSynchronizationRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.mockito.Mockito.when;

/**
 * Базовый класс для всех DAO тестов. Загружает тестовую конфигурацию, инициализирует базовые DAO сервисы (DomainObjectDao, CollectionsDao), наполняет
 * базу тестовыми данными.
 * @author atsvetkov
 */
public abstract class BaseDaoTest {

    private static String CONFIGURATION_SCHEMA_PATH = "config/configuration.xsd";
    private static String DOMAIN_OBJECTS_CONFIG_PATH = "config/domain-objects-test.xml";
    private static String COLLECTIONS_CONFIG_PATH = "config/collections-test.xml";
    private static String ACCESS_CONFIG_PATH = "config/access-test.xml";
    private static String COLLECTIONS_VIEW_PATH = "config/collection-view-test.xml";

    private static String SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH = "config/system-domain-objects-test.xml";

    private static String MODULES_CONFIG_FOLDER = "modules-configuration";
    private static String MODULES_CONFIG_PATH = "/modules-configuration-test.xml";
    private static String MODULES_CONFIG_SCHEMA_PATH = "config/modules-configuration-test.xsd";
    private static final String GLOBAL_XML_PATH = "config/global-test.xml";

    protected static PermissionServiceDaoImpl permissionService;
    protected static DynamicGroupServiceImpl dynamicGroupService;
    protected static NamedParameterJdbcOperations namedParameterJdbcOperations;
    protected static JdbcOperations jdbcOperations;
    protected static DriverManagerDataSource dataSource;
    protected static DomainObjectTypeIdCacheImpl domainObjectTypeIdCache;
    protected static AccessControlService  accessControlService;
    protected static ConfigurationExplorer configurationExplorer;

    protected static DomainObjectDaoImpl domainObjectDao;
    protected static CollectionsDaoImpl collectionsDao;
    protected static AccessToken accessToken;

    private static DomainObject savedOrganizationObject;

    private static DomainObject savedDepartmentObject;

    private static DomainObject savedPersonObject;

    private static DomainObject savedEmployeeObject;

    private static DomainObject savedEmployeeDelegateObject;

    private static DomainObject savedDelegationObject;

    private static DomainObject savedActiveStatus;

    private static DomainObject savedDraftStatus;

    @Mock
    private static DomainObjectCacheServiceImpl cacheServiceImpl = Mockito.mock(DomainObjectCacheServiceImpl.class);

    @Mock
    private static CurrentUserAccessor currentUserAccessor = Mockito.mock(CurrentUserAccessor.class);

    @Mock
    private static TransactionSynchronizationRegistry transactionSynchronizationRegistry = Mockito.mock(TransactionSynchronizationRegistry.class);

    protected static ConfigurationSerializer createConfigurationSerializer(String configPath) throws Exception {
        ConfigurationClassesCache.getInstance().build();
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        Set<String> configPaths =
                new HashSet<>(Arrays.asList(configPath, COLLECTIONS_CONFIG_PATH, SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH, ACCESS_CONFIG_PATH,
                        COLLECTIONS_VIEW_PATH, GLOBAL_XML_PATH));

        /*configurationSerializer.setCoreConfigurationFilePaths(configPaths);
        configurationSerializer.setCoreConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationSerializer.setModulesConfigurationFolder(MODULES_CONFIG_FOLDER);
        configurationSerializer.setModulesConfigurationPath(MODULES_CONFIG_PATH);
        configurationSerializer.setModulesConfigurationSchemaPath(MODULES_CONFIG_SCHEMA_PATH);*/
        ModuleService moduleService = Mockito.mock(ModuleService.class);
        ReflectionTestUtils.setField(configurationSerializer, "moduleService", moduleService);
        return configurationSerializer;
    }

    private static ConfigurationExplorer initConfigurationExplorer() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);

        Configuration configuration = configurationSerializer.deserializeConfiguration();
        ConfigurationExplorerImpl configurationExplorer = new ConfigurationExplorerImpl(configuration);
        configurationExplorer.init();
        return configurationExplorer;
    }

    @BeforeClass
    public static void setUp() throws Exception {

        when(transactionSynchronizationRegistry.getTransactionKey()).thenReturn(null);

        dataSource = createDataSource();
        jdbcOperations = new JdbcTemplate(dataSource);
        namedParameterJdbcOperations = new NamedParameterJdbcTemplate(dataSource);

        domainObjectTypeIdCache = createDomainObjectTypeIdCache(dataSource);

        accessControlService = new AccessControlServiceImpl();
        accessToken = accessControlService.createSystemAccessToken("1");

        configurationExplorer = initConfigurationExplorer();


        permissionService = new PermissionServiceDaoImpl();
        permissionService.setDoelResolver(new DoelResolver());
        permissionService.setConfigurationExplorer(configurationExplorer);
        permissionService.setMasterNamedParameterJdbcTemplate(namedParameterJdbcOperations);
        permissionService.setDomainObjectTypeIdCache(domainObjectTypeIdCache);

        domainObjectDao = new DomainObjectDaoImpl();
        domainObjectDao.setMasterJdbcTemplate(namedParameterJdbcOperations);

        BasicSequenceIdGenerator idGenerator = new PostgreSqlSequenceIdGenerator();
        idGenerator.setJdbcTemplate(jdbcOperations);

        domainObjectDao.setIdGenerator(idGenerator);
        domainObjectDao.setDomainObjectCacheService(cacheServiceImpl);
        domainObjectDao.setDomainObjectTypeIdCache(domainObjectTypeIdCache);
        domainObjectDao.setConfigurationExplorer(configurationExplorer);

        dynamicGroupService = new DynamicGroupServiceImpl() {
            protected DynamicGroupTrackDomainObjectCollector getDynamicGroupTrackDomainObjectCollector() {
                DynamicGroupTrackDomainObjectCollector trackDomainObjectCollector =
                        new DynamicGroupTrackDomainObjectCollector();
                trackDomainObjectCollector.setConfigurationExplorer(configurationExplorer);
                trackDomainObjectCollector.setAccessControlService(accessControlService);
                trackDomainObjectCollector.setDomainObjectDao(domainObjectDao);
                trackDomainObjectCollector.setDomainObjectTypeIdCache(domainObjectTypeIdCache);
                return trackDomainObjectCollector;
            }

            protected TransactionSynchronizationRegistry getTxReg() {
                return transactionSynchronizationRegistry;
            }

        };
        dynamicGroupService.setConfigurationExplorer(configurationExplorer);
        dynamicGroupService.setDomainObjectDao(domainObjectDao);
        dynamicGroupService.setDomainObjectTypeIdCache(domainObjectTypeIdCache);
        // dynamicGroupService.onLoad();

        domainObjectDao.setDynamicGroupService(dynamicGroupService);
        domainObjectDao.setPermissionService(permissionService);
        domainObjectDao.setAccessControlService(accessControlService);

        collectionsDao = new CollectionsDaoImpl();
        collectionsDao.setJdbcTemplate(namedParameterJdbcOperations);
        collectionsDao.setConfigurationExplorer(configurationExplorer);
        collectionsDao.setDomainObjectTypeIdCache(domainObjectTypeIdCache);
        collectionsDao.setCurrentUserAccessor(currentUserAccessor);

        when(currentUserAccessor.getCurrentUserId())
                .thenReturn(new RdbmsId(domainObjectTypeIdCache.getId("Person"), 1));

        prepareInitialData();

    }

    private static Properties getTestProperties() {
        Properties properties = new Properties();
        InputStream stream = FileUtils.class.getClassLoader().getResourceAsStream("config/test.properties");
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading test properties");
        }
        return properties;
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (domainObjectDao != null){
            domainObjectDao.delete(savedDelegationObject.getId(), accessToken);

            domainObjectDao.delete(savedEmployeeObject.getId(), accessToken);
            domainObjectDao.delete(savedEmployeeDelegateObject.getId(), accessToken);

            domainObjectDao.delete(savedPersonObject.getId(), accessToken);

            domainObjectDao.delete(savedDepartmentObject.getId(), accessToken);

            domainObjectDao.delete(savedOrganizationObject.getId(), accessToken);
            domainObjectDao.delete(savedActiveStatus.getId(), accessToken);
            domainObjectDao.delete(savedDraftStatus.getId(), accessToken);
        }
    }

    private static void prepareInitialData() {
        Id statusId = new RdbmsId(domainObjectTypeIdCache.getId("Status"), 1);

        GenericDomainObject personDomainObject = createPersonDomainObject(statusId);

        savedPersonObject = domainObjectDao.save(personDomainObject, accessToken);
//        System.out.println("Saved object : " + savedPersonObject);

        GenericDomainObject organizationDomainObject = createOrganizationDomainObject(statusId);

        savedOrganizationObject = domainObjectDao.save(organizationDomainObject, accessToken);
//        System.out.println("Saved object : " + savedOrganizationObject);

        GenericDomainObject departmentDomainObject = createDepartmentDomainObject(statusId, savedOrganizationObject);

        savedDepartmentObject = domainObjectDao.save(departmentDomainObject, accessToken);
//        System.out.println("Saved object : " + savedDepartmentObject);

        GenericDomainObject employeeDomainObject = createEmployeeDomainObject(statusId, savedDepartmentObject);

        savedEmployeeObject = domainObjectDao.save(employeeDomainObject, accessToken);
//        System.out.println("Saved object : " + savedEmployeeObject);

        GenericDomainObject employeeDelegateObject = createEmployeeDomainObject(statusId, savedDepartmentObject);

        savedEmployeeDelegateObject = domainObjectDao.save(employeeDelegateObject, accessToken);
//        System.out.println("Saved object : " + savedEmployeeDelegateObject);

        GenericDomainObject delegationDomainObject =
                createDelegateDomainObject(savedEmployeeObject, savedEmployeeDelegateObject, statusId);

        savedDelegationObject = domainObjectDao.save(delegationDomainObject, accessToken);

        GenericDomainObject statusActiveObject = createStatusDomainObject("Active");

        savedActiveStatus = domainObjectDao.save(statusActiveObject, accessToken);
//        System.out.println("Saved status : " + savedActiveStatus);

        GenericDomainObject statusDraftObject = createStatusDomainObject("Draft");

        savedDraftStatus = domainObjectDao.save(statusDraftObject, accessToken);

//        System.out.println("Saved status : " + savedDraftStatus);


    }

  private static GenericDomainObject createDelegateDomainObject(DomainObject savedEmployeeObject, DomainObject savedEmployeeDelegateObject, Id statusId) {
      GenericDomainObject delegationDomainObject = new GenericDomainObject();

      Id personId = new RdbmsId(domainObjectTypeIdCache.getId("Employee"), ((RdbmsId)savedEmployeeObject.getId()).getId());
      Id delegateId = new RdbmsId(domainObjectTypeIdCache.getId("Employee"), ((RdbmsId)savedEmployeeDelegateObject.getId()).getId());

      delegationDomainObject.setCreatedDate(new Date());
      delegationDomainObject.setModifiedDate(new Date());
      delegationDomainObject.setTypeName("Delegation");
      delegationDomainObject.setReference("person", personId);
      delegationDomainObject.setReference("delegate", delegateId);
      delegationDomainObject.setStatus(statusId);
      return delegationDomainObject;
  }

  private static GenericDomainObject createEmployeeDomainObject(Id statusId, DomainObject savedDepartmentDomainObject) {
      GenericDomainObject employeeDomainObject = new GenericDomainObject();
      employeeDomainObject.setCreatedDate(new Date());
      employeeDomainObject.setModifiedDate(new Date());
      employeeDomainObject.setTypeName("Employee");
      employeeDomainObject.setString("Name", "Employee " + new Date());
      employeeDomainObject.setString("Position", "Position");
      employeeDomainObject.setStatus(statusId);
      employeeDomainObject.setReference("Department", savedDepartmentDomainObject.getId());
      return employeeDomainObject;
  }

  private static GenericDomainObject createDepartmentDomainObject(Id statusId, DomainObject savedOrganizationObject) {
      GenericDomainObject departmentDomainObject = new GenericDomainObject();
      departmentDomainObject.setCreatedDate(new Date());
      departmentDomainObject.setModifiedDate(new Date());
      departmentDomainObject.setTypeName("Department");
      departmentDomainObject.setString("Name", "department1");
      departmentDomainObject.setStatus(statusId);
      departmentDomainObject.setReference("Organization", savedOrganizationObject.getId());
      return departmentDomainObject;
  }

  private static GenericDomainObject createPersonDomainObject(Id statusId) {
      GenericDomainObject personDomainObject = new GenericDomainObject();
      personDomainObject.setCreatedDate(new Date());
      personDomainObject.setModifiedDate(new Date());
      personDomainObject.setTypeName("Person");
      personDomainObject.setString("Login", "login " + new Date());
      personDomainObject.setStatus(statusId);
      return personDomainObject;
  }

  private static GenericDomainObject createStatusDomainObject(String statusName) {
      GenericDomainObject personDomainObject = new GenericDomainObject();
      personDomainObject.setCreatedDate(new Date());
      personDomainObject.setModifiedDate(new Date());
      personDomainObject.setTypeName("Status");
      personDomainObject.setString("Name", statusName);
      return personDomainObject;
  }

  private static GenericDomainObject createOrganizationDomainObject(Id statusId) {
      GenericDomainObject organizationDomainObject = new GenericDomainObject();
      organizationDomainObject.setCreatedDate(new Date());
      organizationDomainObject.setModifiedDate(new Date());
      organizationDomainObject.setTypeName("Organization");
      organizationDomainObject.setString("Name", "Organization" + new Date());
      organizationDomainObject.setStatus(statusId);
      return organizationDomainObject;
  }


  private static DriverManagerDataSource createDataSource() {

      Properties properties = getTestProperties();

      DriverManagerDataSource dataSource = new DriverManagerDataSource();
      dataSource.setDriverClassName(properties.getProperty("driver.name"));
      dataSource.setUrl(properties.getProperty("database.url"));
      dataSource.setUsername(properties.getProperty("database.user"));
      dataSource.setPassword(properties.getProperty("database.password"));
      return dataSource;
  }

  private static DomainObjectTypeIdCacheImpl createDomainObjectTypeIdCache(DriverManagerDataSource dataSource) {
      DataStructureDao dataStructureDao = Mockito.mock(DataStructureDao.class);

      DomainObjectTypeIdDaoImpl domainObjectTypeIdDao = new DomainObjectTypeIdDaoImpl();
      domainObjectTypeIdDao.setJdbcTemplate(jdbcOperations);
      ReflectionTestUtils.setField(domainObjectTypeIdDao, "dataStructureDao", dataStructureDao);

      DomainObjectTypeIdCacheImpl domainObjectTypeIdCache = new DomainObjectTypeIdCacheImpl();
      domainObjectTypeIdCache.setDomainObjectTypeIdDao(domainObjectTypeIdDao);
      domainObjectTypeIdCache.build();
      return domainObjectTypeIdCache;
  }


}
