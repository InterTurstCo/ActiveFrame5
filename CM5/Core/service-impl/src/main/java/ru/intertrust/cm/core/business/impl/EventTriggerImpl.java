package ru.intertrust.cm.core.business.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ru.intertrust.cm.core.business.api.EventTrigger;
import ru.intertrust.cm.core.business.api.ScriptService;
import ru.intertrust.cm.core.business.api.TriggerService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.NamedTriggerConfig;
import ru.intertrust.cm.core.config.TriggerConfigConfig;
import ru.intertrust.cm.core.config.TriggerFieldConfig;
import ru.intertrust.cm.core.config.TriggerStatusConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.model.EventTriggerException;
import ru.intertrust.cm.core.tools.ScriptDomainObjectAccessor;

/**
 * 
 * @author atsvetkov
 *
 */
public class EventTriggerImpl implements EventTrigger, ApplicationContextAware {

    /**
     * Типы событий, поддерживаемых системой
     * @author atsvetkov
     *
     */
    public static enum EventType {
        CREATE,
        CHANGE,
        CHANGE_STATUS,
        DELETE
    }

    @Autowired
    private ConfigurationExplorer configurationExplorer;
    
    @Autowired    
    private DomainObjectTypeIdCache domainObjectTypeIdCache;
    
    @Autowired
    private StatusDao statusDao;
    
    @Autowired
    private  ScriptService scriptService;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    
    /* (non-Javadoc)
     * @see ru.intertrust.cm.core.business.api.EventTrigger#isTriggered(java.lang.String, java.lang.String, ru.intertrust.cm.core.business.api.dto.DomainObject, java.util.List)
     */
    @Override
    public boolean isTriggered(String triggerName, String eventType, DomainObject domainObject,
            List<FieldModification> changedFields) {

        Collection<NamedTriggerConfig> namedTriggerConfigs = configurationExplorer.getConfigs(NamedTriggerConfig.class);
        String domainObjectType = domainObjectTypeIdCache.getName(domainObject.getId());
        for(NamedTriggerConfig namedTriggerConfig : namedTriggerConfigs) {
            if(namedTriggerConfig.getName().equals(triggerName)) {
                if (namedTriggerConfig.getTrigger().getEvent().equals(eventType)
                        && namedTriggerConfig.getTrigger().getDomainObjectType().equals(domainObjectType)) {
                    
                    
                    TriggerConfigConfig triggerConfig = namedTriggerConfig.getTrigger().getTriggerConfig();
                    if(triggerConfig != null ) {
                        if (EventType.CHANGE_STATUS.toString().equals(eventType)) {
                            // если статусы не указаны в конфигурации, событие срабатывает на все статусы
                            if (triggerConfig.getTriggerStatusesConfig() == null) {
                                return true;
                            }
                            for (TriggerStatusConfig triggerStatusConfig : triggerConfig.getTriggerStatusesConfig()
                                    .getStatuses()) {
                                String domainObjectStatusName = statusDao.getStatusNameById(domainObject.getStatus());
                                if (triggerStatusConfig.getData().equalsIgnoreCase(domainObjectStatusName)) {
                                    return true;
                                }
                            }

                        } else if (EventType.CHANGE.toString().equals(eventType)) {
                            if (triggerConfig.getTriggerFieldsConfig() == null) {
                                return true;
                            }

                            // если список измененных полей пустой, изменение любого поля вызывает это событие
                            if (changedFields == null || changedFields.size() == 0) {
                                return true;
                            }

                            for (TriggerFieldConfig triggerFieldConfig : namedTriggerConfig.getTrigger()
                                    .getTriggerConfig().getTriggerFieldsConfig().getFields()) {
                                for (FieldModification fieldModification : changedFields) {
                                    if (fieldModification.getName().equalsIgnoreCase(triggerFieldConfig.getData())) {
                                        return true;
                                    }
                                }
                            }
                        } else if (EventType.CREATE.toString().equals(eventType) || EventType.DELETE.toString().equals(eventType)) {
                            return true;
                        }
                        
                    } else if (namedTriggerConfig.getTrigger().getTriggerClassNameConfig() != null
                            && namedTriggerConfig.getTrigger().getTriggerClassNameConfig().getData() != null) {                        
                        
                        return executeJavaClass(eventType, domainObject, changedFields, namedTriggerConfig);
                        
                    } else if (namedTriggerConfig.getTrigger().getTriggerConditionsScriptConfig() != null) {
                        return executeScript(eventType, domainObject, changedFields, namedTriggerConfig);
                    }
                }
                
            }
        }
        return false;
    }

    private boolean executeJavaClass(String eventType, DomainObject domainObject,
            List<FieldModification> changedFields, NamedTriggerConfig namedTriggerConfig) {
        Class<?> triggerClassNameClass = null;
        try {
            triggerClassNameClass =
                    Class.forName(namedTriggerConfig.getTrigger().getTriggerClassNameConfig().getData());
        } catch (ClassNotFoundException e) {
            throw new EventTriggerException("Error on initializing class "
                    + namedTriggerConfig.getTrigger().getTriggerClassNameConfig().getData()
                    + "Class not found");
        }

        TriggerService triggerService = (TriggerService) applicationContext
                .getAutowireCapableBeanFactory().createBean(triggerClassNameClass,
                        AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
                        false);
        return triggerService.isTriggered(eventType, domainObject, changedFields);
    }

    private boolean executeScript(String eventType, DomainObject domainObject, List<FieldModification> changedFields,
            NamedTriggerConfig namedTriggerConfig) {
        String script = namedTriggerConfig.getTrigger().getTriggerConditionsScriptConfig().getData();
        ScriptDomainObjectAccessor context = new ScriptDomainObjectAccessor(domainObject, eventType, changedFields);
        return (Boolean) scriptService.eval(script, context);
    }

    @Override
    public List<String> getTriggeredEvents(String eventType, DomainObject domainObject,
            List<FieldModification> changedFields) {
        List<String> matchedTriggers = new ArrayList<String>();
        Collection<NamedTriggerConfig> namedTriggerConfigs = configurationExplorer.getConfigs(NamedTriggerConfig.class);
        for (NamedTriggerConfig namedTriggerConfig : namedTriggerConfigs) {
            if (isTriggered(namedTriggerConfig.getName(), eventType, domainObject, changedFields)) {
                matchedTriggers.add(namedTriggerConfig.getName());
            }
        }
        return matchedTriggers;
    }

}
