package ru.intertrust.cm.core.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.transaction.TransactionSynchronizationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.Subject;
import ru.intertrust.cm.core.dao.api.DomainEntitiesCloner;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DomainObjectCacheServiceImplTest.Config.class })
@EnableSpringConfigured
public class DomainObjectCacheServiceImplTest {

    private static ThreadLocal<Map> transactionResources = new ThreadLocal<Map>();

    @Autowired
    private DomainObjectCacheService domainObjectCacheService;

    @Configuration
    public static class Config {

        @Bean
        public PropertyPlaceholderConfigurer propConfig() {
            PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
            Properties properties = new Properties();
            properties.setProperty("cache.domainObject.enabled", "true");
            ppc.setProperties(properties);
            return ppc;
        }

        @Bean
        public DomainObjectCacheService domainObjectCacheService() {
            return new DomainObjectCacheServiceImpl();
        }

        @Bean
        public DomainObjectTypeIdCache domainObjectTypeIdCache() {
            return mock(DomainObjectTypeIdCache.class);
        }

        @Bean
        public TransactionSynchronizationRegistry transactionSynchronizationRegistry() {
            TransactionSynchronizationRegistry result = mock(TransactionSynchronizationRegistry.class);

            // getTransactionKey
            when(result.getTransactionKey()).thenReturn("transaction-key");

            // putResource
            doAnswer(e -> {
                transactionResources.get().put(e.getArgumentAt(0, Object.class), e.getArgumentAt(1, Object.class));
                return null;
            }).when(result).putResource(any(), any());

            // getResource
            when(result.getResource(any())).then(e -> {
                return transactionResources.get().get(e.getArgumentAt(0, Object.class));
            });
            return result;
        }

        @Bean
        public ConfigurationExplorer configurationExplorer() {
            ConfigurationExplorer result = mock(ConfigurationExplorer.class);
            when(result.getDomainObjectTypeConfig(anyString())).then(p -> {
                DomainObjectTypeConfig config = new DomainObjectTypeConfig();
                return config;
            });

            when(result.getReferenceFieldConfigs(anyString())).then(p -> {
                Set<ReferenceFieldConfig> configs = new HashSet<ReferenceFieldConfig>();
                if (p.getArgumentAt(0, String.class).equalsIgnoreCase("type2") 
                        || p.getArgumentAt(0, String.class).equalsIgnoreCase("type3")) {
                    ReferenceFieldConfig config = new ReferenceFieldConfig();
                    config.setName("type1");
                    config.setType("type1");
                    configs.add(config);
                }
                return configs;
            });

            return result;
        }

        @Bean
        public DomainEntitiesCloner domainEntitiesCloner() {
            return new DomainEntitiesClonerImpl();
        }

    }

    /**
     * Эмуляция новой транзакции
     */
    private void beginTransaction() {
        transactionResources.get().clear();
    }

    @Before
    public void init() {
        transactionResources.set(new HashMap<Object, Object>());
    }

    @Test
    public void testFindLinkedDomainObjects() throws Exception {
        assertNotNull(domainObjectCacheService);

        beginTransaction();

        // Создаем корневой DomainObject
        GenericDomainObject rdo1 = new GenericDomainObject();
        rdo1.setTypeName("type1");
        rdo1.setId(new RdbmsId(1, 1));

        // Кладем в кэш
        domainObjectCacheService.putOnUpdate(rdo1, getSystemAccessToken());

        // Получаем его из кэша
        DomainObject rdo1FromCache = domainObjectCacheService.get(rdo1.getId(), getSystemAccessToken());
        assertNotNull(rdo1FromCache);

        // Создаем связанный доменный объект
        GenericDomainObject chdo1 = new GenericDomainObject();
        chdo1.setTypeName("type2");
        chdo1.setId(new RdbmsId(2, 1));
        chdo1.setReference("type1", rdo1.getId());

        // Кладем в кэш
        domainObjectCacheService.putOnUpdate(chdo1, getSystemAccessToken());

        // Получаем связанные из кэша до вызова чтения, должны получить null
        String[] cacheKey = new String[] { "type2","type1",String.valueOf(false) };
        List<DomainObject> result = domainObjectCacheService.getAll(rdo1.getId(), getSystemAccessToken(), cacheKey);
        assertNull(result);

        // Имитируем вызов findLinkedDomainObjects
        domainObjectCacheService.putAllOnRead(rdo1.getId(), Arrays.asList(chdo1), getSystemAccessToken(), cacheKey);

        // Получаем связанные из кэша
        result = domainObjectCacheService.getAll(rdo1.getId(), getSystemAccessToken(), cacheKey);
        assertEquals(result.size(), 1);

        // Создаем еще один связанный
        GenericDomainObject chdo2 = new GenericDomainObject();
        chdo2.setTypeName("type2");
        chdo2.setId(new RdbmsId(2, 2));
        chdo2.setReference("type1", rdo1.getId());

        // Кладем в кэш, должен инвалидироваться кэш связанных 
        domainObjectCacheService.putOnUpdate(chdo2, getSystemAccessToken());

        // Получаем связанные из кэша, должны получить null
        result = domainObjectCacheService.getAll(rdo1.getId(), getSystemAccessToken(), cacheKey);
        assertNull(result);

        // Имитируем чтение из базы
        domainObjectCacheService.putAllOnRead(rdo1.getId(), Arrays.asList(chdo1, chdo2), getSystemAccessToken(), cacheKey);        

        // Читаем из кэша, должны получить два
        result = domainObjectCacheService.getAll(rdo1.getId(), getSystemAccessToken(), cacheKey);
        assertEquals(result.size(), 2);        
        
        // Создаем еще один связанный, меняем регистр reference полей
        GenericDomainObject chdo3 = new GenericDomainObject();
        chdo3.setTypeName("TYPE2");
        chdo3.setId(new RdbmsId(2, 3));
        chdo3.setReference("TYPE1", rdo1.getId());

        // Кладем в кэш, кэш должен инвалидироватся 
        domainObjectCacheService.putOnUpdate(chdo3, getSystemAccessToken());

        // Получаем связанные из кэша, должны получить null
        result = domainObjectCacheService.getAll(rdo1.getId(), getSystemAccessToken(), cacheKey);
        assertNull(result);

        // Имитируем чтение из базы
        domainObjectCacheService.putAllOnRead(rdo1.getId(), Arrays.asList(chdo1, chdo2, chdo3), getSystemAccessToken(), cacheKey);        

        // Читаем из кэша, должны получить три
        result = domainObjectCacheService.getAll(rdo1.getId(), getSystemAccessToken(), cacheKey);
        assertEquals(result.size(), 3);          
        
        // Создаем еще один связанный, экземпляр наследника type2
        GenericDomainObject chdo4 = new GenericDomainObject();
        chdo4.setTypeName("type3");
        chdo4.setId(new RdbmsId(3, 4));
        chdo4.setReference("type1", rdo1.getId());

        // Кладем в кэш, должен обнулится 
        domainObjectCacheService.putOnUpdate(chdo4, getSystemAccessToken());

        // Получаем связанные из кэша, должны получить null
        result = domainObjectCacheService.getAll(rdo1.getId(), getSystemAccessToken(), cacheKey);
        assertNull(result);
        
        // Имитируем чтение из базы не строго определенного типа
        domainObjectCacheService.putAllOnRead(rdo1.getId(), Arrays.asList(chdo1, chdo2, chdo3, chdo4), getSystemAccessToken(), cacheKey);        

        // Имитируем чтение из базы строго определенного типа
        String[] exactTypeCacheKey = new String[] { "type2","type1",String.valueOf(true) };
        domainObjectCacheService.putAllOnRead(rdo1.getId(), Arrays.asList(chdo1, chdo2, chdo3), getSystemAccessToken(), exactTypeCacheKey);        

        // Читаем из кэша не строго, должны получить четыре
        result = domainObjectCacheService.getAll(rdo1.getId(), getSystemAccessToken(), cacheKey);
        assertEquals(result.size(), 4);          
        
        // Читаем из кэша строго, должны получить три
        result = domainObjectCacheService.getAll(rdo1.getId(), getSystemAccessToken(), exactTypeCacheKey);
        assertEquals(result.size(), 3);          
        
        // Проверяем изменение ссылки на родителя
        // Создаем нового родителя
        // Создаем корневой DomainObject
        GenericDomainObject rdo2 = new GenericDomainObject();
        rdo2.setTypeName("type1");
        rdo2.setId(new RdbmsId(1, 2));
        
        // Кладем в кэш
        domainObjectCacheService.putOnUpdate(rdo2, getSystemAccessToken());
        
        // У третьего связанного меняем ссылку на родителя
        chdo3.setReference("type1", rdo2.getId());

        // Кладем в кэш
        domainObjectCacheService.putOnUpdate(chdo4, getSystemAccessToken());
        
        // Кэш должен обнулиться как для первого так и для второго родителя для строгого и не строгого режимов
        result = domainObjectCacheService.getAll(rdo1.getId(), getSystemAccessToken(), cacheKey);
        assertNull(result);
        result = domainObjectCacheService.getAll(rdo1.getId(), getSystemAccessToken(), exactTypeCacheKey);
        assertNull(result);
        result = domainObjectCacheService.getAll(rdo2.getId(), getSystemAccessToken(), cacheKey);
        assertNull(result);
        result = domainObjectCacheService.getAll(rdo2.getId(), getSystemAccessToken(), exactTypeCacheKey);
        assertNull(result);
        
        // Имитируем чтение из базы для двух рутовых доменных объектов не строго
        domainObjectCacheService.putAllOnRead(rdo1.getId(), Arrays.asList(chdo1, chdo2, chdo4), getSystemAccessToken(), cacheKey);        
        domainObjectCacheService.putAllOnRead(rdo2.getId(), Arrays.asList(chdo3), getSystemAccessToken(), cacheKey);        
        
        // Проверяем обнуления ссылки на родителя у второго связанного
        chdo2.setReference("type1", (Id)null);
        domainObjectCacheService.putOnUpdate(chdo2, getSystemAccessToken());
        
        // Кэш для первого должен обнулится
        result = domainObjectCacheService.getAll(rdo1.getId(), getSystemAccessToken(), cacheKey);
        assertNull(result);
        
        // Для второго не должен обнулятся и вернуть 1 связанный
        result = domainObjectCacheService.getAll(rdo2.getId(), getSystemAccessToken(), cacheKey);
        assertEquals(result.size(), 1);
    }

    private AccessToken getSystemAccessToken() {
        AccessToken token = new AccessToken() {

            @Override
            public Subject getSubject() {
                return new Subject() {

                    @Override
                    public String getName() {
                        return "test-cache";
                    }

                };
            }

            @Override
            public boolean isDeferred() {
                return false;
            }

            @Override
            public AccessLimitationType getAccessLimitationType() {
                return AccessLimitationType.UNLIMITED;
            }
        };

        return token;
    }

}
