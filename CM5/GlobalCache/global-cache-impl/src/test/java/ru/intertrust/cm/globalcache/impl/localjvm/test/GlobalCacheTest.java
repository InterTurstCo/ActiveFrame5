package ru.intertrust.cm.globalcache.impl.localjvm.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectsModification;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.SystemField;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.Subject;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.DomainEntitiesCloner;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DomainEntitiesClonerImpl;
import ru.intertrust.cm.globalcache.api.GlobalCache;
import ru.intertrust.cm.globalcache.api.PersonAccessChanges;
import ru.intertrust.cm.globalcache.impl.localjvm.GlobalCacheImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { GlobalCacheTest.Config.class })
public class GlobalCacheTest {
    public static final String CONFIGURATION_SCHEMA_PATH = "config/configuration.xsd";

    @Autowired
    private GlobalCache globalCache;
    
    @Autowired
    private DomainObjectTypeIdCache typeIdCache;

    private int lastObjectId = 0;

    @Configuration
    public static class Config {
        @Bean
        public GlobalCache globalCache() {
            GlobalCache result = new GlobalCacheImpl();
            return result;
        }

        @Bean
        public ConfigurationExplorer configurationExplorer() throws Exception {
            ConfigurationSerializer configurationSerializer = createConfigurationSerializer("global-cache-test.xml");
            ru.intertrust.cm.core.config.base.Configuration config = configurationSerializer.deserializeConfiguration();
            return new ConfigurationExplorerImpl(config);
        }

        @Bean
        public DomainEntitiesCloner domainEntitiesCloner() {
            DomainEntitiesCloner result = new DomainEntitiesClonerImpl();
            return result;
        }

        @Bean
        public DomainObjectTypeIdCache domainObjectTypeIdCache(ConfigurationExplorer configurationExplorer) {
            DomainObjectTypeIdCache result = new DomainObjectTypeIdCache() {
                private Map<String, Integer> typeIds = new HashMap<String, Integer>();
                private Map<Integer, String> IdTypes = new HashMap<Integer, String>();
                private int lastTypeId = 0;

                @Override
                public void build() {
                    typeIds.clear();
                    IdTypes.clear();

                    for (DomainObjectTypeConfig typeConfig : configurationExplorer.getConfigs(DomainObjectTypeConfig.class)) {
                        int typeId = ++lastTypeId;
                        typeIds.put(typeConfig.getName().toLowerCase(), typeId);
                        IdTypes.put(typeId, typeConfig.getName().toLowerCase());
                    }
                }

                @Override
                public Integer getId(String name) {
                    return typeIds.get(name.toLowerCase());
                }

                @Override
                public String getName(Integer id) {
                    return IdTypes.get(id);
                }

                @Override
                public String getName(Id id) {
                    return IdTypes.get(((RdbmsId) id).getTypeId());
                }
            };
            return result;
        }

        private ConfigurationSerializer createConfigurationSerializer(String confPath) throws Exception {
            ConfigurationClassesCache.getInstance().build(); // Инициализируем кэш конфигурации тэг-класс

            ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
            configurationSerializer.setModuleService(createModuleService(confPath));

            return configurationSerializer;
        }

        private ModuleService createModuleService(String confPath) throws MalformedURLException {
            URL moduleUrl = getClass().getClassLoader().getResource(".");

            ModuleService result = new ModuleService();
            ModuleConfiguration confCore = new ModuleConfiguration();
            confCore.setName("core");
            result.getModuleList().add(confCore);
            confCore.setConfigurationPaths(new ArrayList<String>());
            confCore.getConfigurationPaths().add(confPath);
            confCore.setConfigurationSchemaPath(CONFIGURATION_SCHEMA_PATH);
            confCore.setModuleUrl(moduleUrl);

            return result;
        }

    }

    public class TestAccessToken implements AccessToken {
        private int userId;

        public TestAccessToken(int userId) {
            this.userId = userId;
        }

        @Override
        public Subject getSubject() {
            return new UserSubject(userId);
        }

        @Override
        public boolean isDeferred() {
            return false;
        }

        @Override
        public AccessLimitationType getAccessLimitationType() {
            return AccessToken.AccessLimitationType.LIMITED;
        }

    }

    @Before
    public void init() {
        typeIdCache.build();
        globalCache.activate();
        globalCache.clear();
    }

    @After
    public void deinit() {
        globalCache.clear();
        globalCache.deactivate();
    }

    @Test
    public void testDomainObjectCache() {
        // Получаем доменный объект который еще не читали из кэша
        String transactionId_1 = UUID.randomUUID().toString();
        DomainObject domainObject_1 = generateDomainObject("test_1");
        TestAccessToken accessToken_1 = new TestAccessToken(1);
        DomainObject domainObject = globalCache.getDomainObject(transactionId_1, domainObject_1.getId(), accessToken_1);
        assertNull(domainObject);

        // Имитируем чтение доменного объекта в той же транзакции и тем же пользователем
        globalCache.notifyRead(transactionId_1, domainObject_1.getId(), domainObject_1, accessToken_1);

        // Получаем из кэша
        domainObject = globalCache.getDomainObject(transactionId_1, domainObject_1.getId(), accessToken_1);
        assertNotNull(domainObject);

        // Получаем из кэша под вторым пользователем
        TestAccessToken accessToken_2 = new TestAccessToken(2);
        domainObject = globalCache.getDomainObject(transactionId_1, domainObject_1.getId(), accessToken_2);
        assertNull(domainObject);

        // Читаем под вторым пользователем и получаем из кэша
        globalCache.notifyRead(transactionId_1, domainObject_1.getId(), domainObject_1, accessToken_2);
        domainObject = globalCache.getDomainObject(transactionId_1, domainObject_1.getId(), accessToken_2);
        assertNotNull(domainObject);

        // Имитируем сохранение и чтение обновленного доменного объекта под первым пользователем
        domainObject_1 = updateDomainObject(domainObject_1);
        globalCache.notifyRead(transactionId_1, domainObject_1.getId(), domainObject_1, accessToken_1);

        // Проверяем что в кэше получаем обновленный доменный объект под обоими пользователями
        domainObject = globalCache.getDomainObject(transactionId_1, domainObject_1.getId(), accessToken_1);
        assertEquals(domainObject_1, domainObject);
        domainObject = globalCache.getDomainObject(transactionId_1, domainObject_1.getId(), accessToken_2);
        assertEquals(domainObject_1, domainObject);

        List<DomainObject> domainObjects = globalCache.getDomainObjects(transactionId_1, Arrays.asList(domainObject_1.getId()), accessToken_1);
        assertNotNull(domainObjects);
        assertNotNull(domainObjects.size() == 1);
        assertEquals(domainObject_1, domainObjects.get(0));

        // Иметируем удаление доменного объекта
        DomainObjectsModification domainObjectsModification = new DomainObjectsModification(transactionId_1);
        domainObjectsModification.addDeletedDomainObject(domainObject_1);
        globalCache.notifyCommit(domainObjectsModification, new PersonAccessChanges());

        // Ищем в кэше после удаления
        domainObject = globalCache.getDomainObject(transactionId_1, domainObject_1.getId(), accessToken_2);
        assertNull(domainObject);

    }

    @Test
    @Ignore
    public void testLinkedDomainObjectCache() {
        // Получаем связанные доменные объекты которые еще не читали из кэша
        String transactionId_1 = UUID.randomUUID().toString();
        DomainObject domainObject_1_1 = generateDomainObject("test_1");
        TestAccessToken accessToken_1 = new TestAccessToken(1);
        List<DomainObject> domainObjects = globalCache.getLinkedDomainObjects(transactionId_1, domainObject_1_1.getId(), "test_2", "test_1", false, accessToken_1);
        assertNull(domainObjects);
        
        // имитируем чтение связанных из базы
        DomainObject domainObject_2_1 = generateDomainObject("test_2");
        DomainObject domainObject_2_2 = generateDomainObject("test_2");
        DomainObject domainObject_2_3 = generateDomainObject("test_2");
        List<DomainObject> domainObjects_1 = Arrays.asList(domainObject_2_1, domainObject_2_2, domainObject_2_3);
        //globalCache.notifyLinkedObjectsRead(transactionId_1, domainObject_1.getId(), "test_2", "test_1", false, domainObjects_1, accessToken_1);
    }

    @Test
    @Ignore
    public void testStressNotifyRead() {
        testNotifyRead(10);
        testNotifyRead(100);
        testNotifyRead(1000);
        testNotifyRead(10000);
        testNotifyRead(20000);
        testNotifyRead(30000);
        testNotifyRead(40000);
        testNotifyRead(50000);
    }
    
    private void testNotifyRead(long count) {
        TestAccessToken accessToken_1 = new TestAccessToken(1);
        String transactionId_1 = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();
        long startSize = globalCache.getSizeBytes();
        for (int i = 0; i < count; i++) {
            DomainObject domainObject = generateDomainObject("test_1");
            globalCache.notifyRead(transactionId_1, domainObject.getId(), domainObject, accessToken_1);
        }
        long time = System.currentTimeMillis() - start;
        long size = globalCache.getSizeBytes() - startSize;
        System.out.println("Iterations:" + count + "; time:" + time + "; size:" + size + "; max-size:" + globalCache.getSizeLimitBytes());
    }

    private DomainObject updateDomainObject(DomainObject domainObject) {
        ((GenericDomainObject) domainObject).setModifiedDate(new Date());
        return domainObject;
    }

    private DomainObject generateDomainObject(String type) {
        GenericDomainObject result = new GenericDomainObject(type);
        Integer typeId = typeIdCache.getId(type);
        result.setId(new RdbmsId(typeId, ++lastObjectId));
        result.setReference(SystemField.access_object_id.name(), result.getId());
        result.setModifiedDate(new Date());
        return result;
    }

}
