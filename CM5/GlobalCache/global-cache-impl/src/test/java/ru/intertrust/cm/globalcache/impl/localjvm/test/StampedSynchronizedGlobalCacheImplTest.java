package ru.intertrust.cm.globalcache.impl.localjvm.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainEntitiesCloner;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.globalcache.api.AccessChanges;
import ru.intertrust.cm.globalcache.api.GlobalCache;
import ru.intertrust.cm.globalcache.api.PersonAccessChanges;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { StampedSynchronizedGlobalCacheImplTest.Config.class })
@EnableSpringConfigured
public class StampedSynchronizedGlobalCacheImplTest {

    @Autowired
    private GlobalCache globalCache;

    @Configuration
    public static class Config {
        @Bean
        public StampedSynchronizedGlobalCacheTestImpl stampedSynchronizedGlobalCacheTestImpl() {
            StampedSynchronizedGlobalCacheTestImpl service = new StampedSynchronizedGlobalCacheTestImpl();
            return service;
        }

        @Bean
        public ConfigurationExplorer configurationExplorer() {
            ConfigurationExplorer result = mock(ConfigurationExplorer.class);
            when(result.getConfigs(any())).thenReturn(Collections.emptyList());
            return result;
        }

        @Bean
        public DomainEntitiesCloner domainEntitiesCloner() {
            DomainEntitiesCloner result = new DomainEntitiesCloner() {

                @Override
                public DomainObject fastCloneDomainObject(DomainObject domainObject) {
                    return domainObject;
                }

                @Override
                public List<DomainObject> fastCloneDomainObjectList(List<DomainObject> domainObjects) {
                    return domainObjects;
                }

                @Override
                public IdentifiableObjectCollection fastCloneCollection(IdentifiableObjectCollection collection) {
                    return collection;
                }

                @Override
                public Filter fastCloneFilter(Filter filter) {
                    return filter;
                }

                @Override
                public SortOrder fastCloneSortOrder(SortOrder sortOrder) {
                    return sortOrder;
                }

                @Override
                public List<Value> fastCloneValueList(List<? extends Value> val) {
                    return (List<Value>)val;
                }

                @Override
                public Id fastCloneId(Id id) {
                    return id;
                }
                
            };
            return result;
        }

        @Bean
        public DomainObjectTypeIdCache domainObjectTypeIdCache() {
            return mock(DomainObjectTypeIdCache.class);
        }
    }

    @Before
    public void init() {
        globalCache.activate();
    }

    private Object[] getParams(Executable executable, Class[] paramTypes) throws Exception {
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i].isPrimitive()) {
                if (paramTypes[i].equals(boolean.class)) {
                    params[i] = false;
                } else if (paramTypes[i].equals(int.class)) {
                    params[i] = 0;
                } else if (paramTypes[i].equals(long.class)) {
                    params[i] = 0l;
                } else {
                    throw new Exception("not support primitive " + paramTypes[i]);
                }
            } else if (paramTypes[i].equals(String.class)) {
                params[i] = "";
                
            } else if (List.class.isAssignableFrom(paramTypes[i])) {
                params[i] = new SortOrder();
            } else if (IdentifiableObjectCollection.class.isAssignableFrom(paramTypes[i])) {
                params[i] = new GenericIdentifiableObjectCollection();
            } else if (List.class.isAssignableFrom(paramTypes[i])) {
                params[i] = Collections.emptyList();
            } else if (paramTypes[i].equals(HashSet.class)) {
                params[i] = new HashSet<>();
            } else if (Set.class.isAssignableFrom(paramTypes[i])) {
                params[i] = Collections.emptySet();
            } else if (Collection.class.isAssignableFrom(paramTypes[i])) {
                params[i] = Collections.emptySet();
            } else if (Map.class.isAssignableFrom(paramTypes[i])) {
                if (executable.getName().equals("notifyReadByUniqueKey") || executable.getName().equals("getDomainObject")) {
                    params[i] = Collections.singletonMap("field1", new StringValue("value1"));
                }else {
                    params[i] = Collections.emptyMap();
                }
            } else if (paramTypes[i].equals(DomainObject.class)) {                
                params[i] = new GenericDomainObject();
                ((GenericDomainObject)params[i]).setTypeName("type1");
                ((GenericDomainObject)params[i]).setId(new RdbmsId(1, 1));
                ((GenericDomainObject)params[i]).setReference("access_object_id", ((GenericDomainObject)params[i]).getId());
            } else if (paramTypes[i].equals(AccessChanges.class)) {
                params[i] = new PersonAccessChanges();
            } else if (paramTypes[i].isInterface()) {
                params[i] = mock(paramTypes[i]);
            } else {
                for (Constructor constructor : paramTypes[i].getConstructors()) {
                    params[i] = constructor.newInstance(getParams(constructor, constructor.getParameterTypes()));
                    break;
                }
            }
        }
        return params;
    }

    @Test
    public void detectDeadLock() throws Exception {
        for (Method method : GlobalCache.class.getMethods()) {
            if (method.getName().equals("deactivate")) {
                continue;
            }
            method.invoke(globalCache, getParams(method, method.getParameterTypes()));
        }
    }
}
