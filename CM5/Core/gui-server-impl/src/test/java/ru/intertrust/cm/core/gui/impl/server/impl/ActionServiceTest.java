package ru.intertrust.cm.core.gui.impl.server.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import ru.intertrust.cm.core.business.api.*;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.form.PlainFormBuilder;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.impl.ModuleServiceImpl;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.api.server.DomainObjectMapping;
import ru.intertrust.cm.core.gui.impl.server.ActionServiceImpl;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ActionServiceTest {
    static private Map<Id, DomainObject> domainObjects = new Hashtable<Id, DomainObject>();
    
    @Autowired
    private ActionService actionService;
    
    @Autowired
    private CrudService crudService;

    @Test
    public void testActionContext(){

        GenericDomainObject domainObjectNotCorrect = new GenericDomainObject();
        domainObjectNotCorrect.setId(new RdbmsId(1, 1));
        domainObjectNotCorrect.setTypeName("type-not-correct");
        crudService.save(domainObjectNotCorrect);

        List<ActionContext> actions = actionService.getActions(domainObjectNotCorrect.getId());
        assertTrue(actions.size() == 1);

        
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setId(new RdbmsId(2, 1));
        domainObject.setTypeName("type-1");
        crudService.save(domainObject);

        actions = actionService.getActions(domainObject.getId());
        assertTrue(actions.size() == 2);
        
        domainObject.setStatus(new RdbmsId(3, 1));
        actions = actionService.getActions(domainObject.getId());
        assertTrue(actions.size() == 3);

        domainObject.setString("attr-1", "value-1");
        actions = actionService.getActions(domainObject.getId());
        assertTrue(actions.size() == 4);

        domainObject.setLong("attr-2", 10L);
        actions = actionService.getActions(domainObject.getId());
        assertTrue(actions.size() == 5);
        
        domainObject.setBoolean("attr-3", true);
        actions = actionService.getActions(domainObject.getId());
        assertTrue(actions.size() == 6);
        
        domainObject.setString("attr-4", "attr-4-value");
        actions = actionService.getActions(domainObject.getId());
        assertTrue(actions.size() == 8);

        domainObject.setString("attr-1", "value-7");
        actions = actionService.getActions(domainObject.getId());
        assertTrue(actions.size() == 4);

        domainObject.setString("attr-1", "value-8");
        actions = actionService.getActions(domainObject.getId());
        assertTrue(actions.size() == 4);
    }
    
    @org.springframework.context.annotation.Configuration
    static class ContextConfiguration {

        @Bean
        public ActionService actionService(){
            return new ActionServiceImpl();
        }
        
        @Bean
        public CrudService crudService(){
            CrudService crudService =  Mockito.mock(CrudService.class);
            
            when(crudService.find(any(Id.class))).thenAnswer(new Answer<DomainObject>() {

                @Override
                public DomainObject answer(InvocationOnMock invocation) throws Throwable {
                    Id id = (Id)invocation.getArguments()[0];
                    return domainObjects.get(id);
                }
            });
            
            when(crudService.save(any(DomainObject.class))).thenAnswer(new Answer<DomainObject>() {

                @Override
                public DomainObject answer(InvocationOnMock invocation) throws Throwable {
                    DomainObject savedDo = (DomainObject)invocation.getArguments()[0];
                    domainObjects.put(savedDo.getId(), savedDo);
                    return savedDo;
                }
            });
            return crudService;
        }

        @Bean
        public CollectionsService collectionsService(){
            return Mockito.mock(CollectionsService.class);
        }

        @Bean
        public ProcessService processService(){
            return Mockito.mock(ProcessService.class);
        }

        @Bean
        public PermissionService permissionService(){
            return Mockito.mock(PermissionService.class);
        }
        
        @Bean
        public FormLogicalValidator formLogicalValidator(){
            return Mockito.mock(FormLogicalValidator.class);
        }

        @Bean
        public NavigationPanelLogicalValidator navigationPanelLogicalValidator(){
            return Mockito.mock(NavigationPanelLogicalValidator.class);
        }

        @Bean
        public PlainFormBuilder plainFormBuilder(){
            return Mockito.mock(PlainFormBuilder.class);
        }

        @Bean
        public WidgetConfigurationLogicalValidator widgetConfigurationLogicalValidator(){
            return Mockito.mock(WidgetConfigurationLogicalValidator.class);
        }

        @Bean
        public UserGroupGlobalCache userGroupGlobalCache(){
            UserGroupGlobalCache userGroupGlobalCache = Mockito.mock(UserGroupGlobalCache.class);
            when(userGroupGlobalCache.isPersonSuperUser(any(Id.class))).thenReturn(true);
            return userGroupGlobalCache;
        }

        @Bean
        public CurrentUserAccessor currentUserAccessor(){
            return Mockito.mock(CurrentUserAccessor.class);
        }

        @Bean
        public ProfileService profileService(){
            return Mockito.mock(ProfileService.class);
        }

        @Bean
        public StatusDao statusDao(){
            StatusDao statusDao = Mockito.mock(StatusDao.class);            
            when(statusDao.getStatusNameById(new RdbmsId(3, 1))).thenReturn("status-1");            
            return statusDao;
        }
        
        @Bean
        public ConfigurationExplorer configurationExplorer() throws Exception {
            ActionConfig.class.toString();
                        
            ConfigurationClassesCache.getInstance().setSearchClassPackages(Arrays.asList("ru"));
            ConfigurationClassesCache.getInstance().build();
            ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();

            ModuleServiceImpl moduleSerevice = new ModuleServiceImpl();
            ModuleConfiguration conf = new ModuleConfiguration();
            moduleSerevice.getModuleList().add(conf);
            conf.setConfigurationPaths(new ArrayList<String>());
            conf.getConfigurationPaths().add("ru/intertrust/cm/core/gui/impl/server/action.xml");
            final URL moduleUrl = getClass().getClassLoader().getResource(".");
            conf.setModuleUrl(moduleUrl);
            conf.setConfigurationSchemaPath("config/configuration.xsd");

            configurationSerializer.setModuleService(moduleSerevice);
            Configuration configuration = configurationSerializer.deserializeConfiguration();
            return new ConfigurationExplorerImpl(configuration);
        }

        @Bean
        public DomainObjectMapping domainObjectMapping(){
            DomainObjectMapping result = Mockito.mock(DomainObjectMapping.class);
            return result;
        }
    }
    
}
