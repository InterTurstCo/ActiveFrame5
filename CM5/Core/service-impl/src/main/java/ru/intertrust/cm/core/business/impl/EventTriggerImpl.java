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
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.NamedTriggerConfig;
import ru.intertrust.cm.core.config.TriggerConfig;
import ru.intertrust.cm.core.config.TriggerConfigConfig;
import ru.intertrust.cm.core.config.TriggerFieldConfig;
import ru.intertrust.cm.core.config.TriggerStatusConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.model.EventTriggerException;
import ru.intertrust.cm.core.tools.EventTriggerScriptContext;

/**
 * 
 * @author atsvetkov
 *
 */

public class EventTriggerImpl implements EventTrigger, ApplicationContextAware {

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
        //TODO Optimize. Cache trigger config by name in Map
        for(NamedTriggerConfig namedTriggerConfig : namedTriggerConfigs) {
            if(namedTriggerConfig.getName().equals(triggerName)) {
                if (isTriggered(namedTriggerConfig.getTrigger(), eventType, domainObject, changedFields)) {
                    return true;
                }                
            }
        }
        return false;
    }

    @Override
    public boolean isTriggered(TriggerConfig triggerConfig, String eventType, DomainObject domainObject,
            List<FieldModification> changedFields) {
        if (domainObject.getId() == null) {
            return false;
        }

        if (triggerConfig.getEvent().equals(eventType)
                && isTriggerForDomainObjectType(triggerConfig, domainObject)) {
            
            
            TriggerConfigConfig triggerConfigConfig = triggerConfig.getTriggerConfig();
            if(triggerConfigConfig != null ) {
                if (EventType.CHANGE_STATUS.toString().equals(eventType)) {
                    // если статусы не указаны в конфигурации, событие срабатывает на все статусы
                    if (triggerConfigConfig.getTriggerStatusesConfig() == null) {
                        return true;
                    }
                    for (TriggerStatusConfig triggerStatusConfig : triggerConfigConfig.getTriggerStatusesConfig()
                            .getStatuses()) {
                        String domainObjectStatusName = statusDao.getStatusNameById(domainObject.getStatus());
                        if (triggerStatusConfig.getData().equalsIgnoreCase(domainObjectStatusName)) {
                            return true;
                        }
                    }

                } else if (EventType.CHANGE.toString().equals(eventType)) {
                    if (triggerConfigConfig.getTriggerFieldsConfig() == null) {
                        return true;
                    }

                    // если список измененных полей пустой, то событие не инициируется
                    if (changedFields == null || changedFields.size() == 0) {
                        return false;
                    }

                    for (TriggerFieldConfig triggerFieldConfig : triggerConfigConfig.
                            getTriggerFieldsConfig().getFields()) {
                        for (FieldModification fieldModification : changedFields) {
                            if (fieldModification.getName().equalsIgnoreCase(triggerFieldConfig.getData())) {
                                return true;
                            }
                        }
                    }
                } else if (EventType.CREATE.toString().equals(eventType) || EventType.DELETE.toString().equals(eventType)) {
                    return true;
                }
                
            } else if (triggerConfig.getTriggerClassNameConfig() != null
                    && triggerConfig.getTriggerClassNameConfig().getData() != null) {                        
                
                return executeJavaClass(eventType, domainObject, changedFields, triggerConfig);
                
            } else if (triggerConfig.getTriggerConditionsScriptConfig() != null) {
                return executeScript(eventType, domainObject, changedFields, triggerConfig);
            }
        }
        return false;
    }
    
    /**
     * Проверка на соответствие типа доменного объекта тому что указан в конфигурации триггера. Проверка осуществляется с учетом наследования
     * @param triggerConfig
     * @param domainObject
     * @return
     */
    private boolean isTriggerForDomainObjectType(TriggerConfig triggerConfig, DomainObject domainObject){
        //Проверка непосредственно совпадение типа
        boolean result = triggerConfig.getDomainObjectType().equalsIgnoreCase(domainObject.getTypeName());
        // Если не совпадает то проверка является ли тип доменного объекта дочерним по отношению к типу доменного объекта объявленного в триггере
        if (!result){
            Collection<DomainObjectTypeConfig> chilTypeConfigs = configurationExplorer.findChildDomainObjectTypes(triggerConfig.getDomainObjectType(), true);
            for (DomainObjectTypeConfig chilTypeConfig : chilTypeConfigs) {
                if (chilTypeConfig.getName().equalsIgnoreCase(domainObject.getTypeName())){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    
    private boolean executeJavaClass(String eventType, DomainObject domainObject,
            List<FieldModification> changedFields, TriggerConfig triggerConfig) {
        Class<?> triggerClassNameClass = null;
        try {
            triggerClassNameClass =
                    Class.forName(triggerConfig.getTriggerClassNameConfig().getData());
        } catch (ClassNotFoundException e) {
            throw new EventTriggerException("Error on initializing class "
                    + triggerConfig.getTriggerClassNameConfig().getData()
                    + "Class not found");
        }

        TriggerService triggerService = (TriggerService) applicationContext
                .getAutowireCapableBeanFactory().createBean(triggerClassNameClass,
                        AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
                        false);
        return triggerService.isTriggered(eventType, domainObject, changedFields);
    }

    private boolean executeScript(String eventType, DomainObject domainObject, List<FieldModification> changedFields,
            TriggerConfig triggerConfig) {
        String script = triggerConfig.getTriggerConditionsScriptConfig().getData();
        EventTriggerScriptContext context = new EventTriggerScriptContext(domainObject, eventType, changedFields);
        return (Boolean) scriptService.eval(script, context);
    }
    
    /* (non-Javadoc)
     * @see ru.intertrust.cm.core.business.api.EventTrigger#getTriggeredEvents(java.lang.String, ru.intertrust.cm.core.business.api.dto.DomainObject, java.util.List)
     */
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
