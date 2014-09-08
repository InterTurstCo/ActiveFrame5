package ru.intertrust.cm.core.business.impl.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.EventTrigger;
import ru.intertrust.cm.core.business.api.NotificationService;
import ru.intertrust.cm.core.business.api.ScriptService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeContextRole;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeDynamicGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseePerson;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.notification.NotificationContextObjectProducer;
import ru.intertrust.cm.core.business.impl.EventTriggerImpl.EventType;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FindNotificationContextObjectsConfig;
import ru.intertrust.cm.core.config.FindNotificationContextObjectsDoelConfig;
import ru.intertrust.cm.core.config.FindNotificationContextObjectsJavaClassConfig;
import ru.intertrust.cm.core.config.FindNotificationContextObjectsJavaScriptConfig;
import ru.intertrust.cm.core.config.FindNotificationContextObjectsQueryConfig;
import ru.intertrust.cm.core.config.FindNotificationContextObjectsSpringBeanConfig;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.config.NotificationConfig;
import ru.intertrust.cm.core.config.NotificationContextConfig;
import ru.intertrust.cm.core.config.NotificationContextObject;
import ru.intertrust.cm.core.config.TriggerConfig;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DoelEvaluator;
import ru.intertrust.cm.core.dao.api.DomainObjectFinderService;
import ru.intertrust.cm.core.dao.api.NotificationSenderEvaluator;
import ru.intertrust.cm.core.model.SearchException;
import ru.intertrust.cm.core.tools.BaseScriptContext;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;
import ru.intertrust.cm.core.util.SpringApplicationContext;

/**
 * 
 * @author atsvetkov
 *
 */
public abstract class NotificationSenderExtensionPointBase {
    
    final static org.slf4j.Logger logger = LoggerFactory.getLogger(NotificationSenderExtensionPointBase.class);

    @Autowired
    protected ConfigurationExplorer configurationExplorer;
    
    @Autowired    
    protected EventTrigger eventTrigger;

    @EJB
    protected NotificationService notificationService;
    
    @Autowired    
    protected CurrentUserAccessor currentUserAccessor;
    
    @Autowired        
    protected DomainObjectFinderService domainObjectFinderService;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private DoelEvaluator doelEvaluator;

    @Autowired
    private AccessControlService accessService;

    @Autowired
    private ScriptService scriptService;
    
    @Autowired   
    private NotificationSenderEvaluator NotificationSenderEvaluator;
    
    public ConfigurationExplorer getConfigurationExplorer() {        
        return configurationExplorer;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }
    
    public EventTrigger getEventTrigger() {
        return eventTrigger;
    }

    public void setEventTrigger(EventTrigger eventTrigger) {
        this.eventTrigger = eventTrigger;
    }
    
    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    public CurrentUserAccessor getCurrentUserAccessor() {
        return currentUserAccessor;
    }

    public void setCurrentUserAccessor(CurrentUserAccessor currentUserAccessor) {
        this.currentUserAccessor = currentUserAccessor;
    }
    
    public DomainObjectFinderService getDomainObjectFinderService() {
        return domainObjectFinderService;
    }

    public void setDomainObjectFinderService(DomainObjectFinderService domainObjectFinderService) {
        this.domainObjectFinderService = domainObjectFinderService;
    }

    protected void sendNotifications(DomainObject domainObject, List<FieldModification> changedFields) {
        Collection<NotificationConfig> notifications = configurationExplorer.getConfigs(NotificationConfig.class);
        for (NotificationConfig notificationConfig : notifications) {
            List<TriggerConfig> notificationTriggers =
                    notificationConfig.getNotificationTypeConfig().getNotificationTriggersConfig().getTriggers();
            for (TriggerConfig triggerConfig : notificationTriggers) {
                boolean isTriggered = false;
                if (triggerConfig.getRefName() != null) {
                    isTriggered = eventTrigger.isTriggered(triggerConfig.getRefName(), getEventType().toString(),
                            domainObject, changedFields);
                } else {
                    isTriggered = eventTrigger.isTriggered(triggerConfig, getEventType().toString(),
                            domainObject, changedFields);
                }

                if (isTriggered) {
                    sendNotification(domainObject, notificationConfig);
                    break;
                }
            }

        }
    }

    abstract protected EventType getEventType();

    protected void sendNotification(DomainObject domainObject, NotificationConfig notificationConfig) {
        String notificationType = notificationConfig.getNotificationTypeConfig().getName();
        NotificationContext notificationContext = new NotificationContext();
        notificationContext.setNotificationTypeConfig(notificationConfig.getNotificationTypeConfig());
        notificationContext.addContextObject("document", new DomainObjectAccessor(domainObject));
        fillAdditionalContextObjects(notificationContext, notificationConfig, domainObject);
        NotificationPriority priority = NotificationPriority
                .valueOf(notificationConfig.getNotificationTypeConfig().getPriority());
        Id senderId = getSender(domainObject, notificationConfig);

        List<NotificationAddressee> addresseeList = getAddresseeList(domainObject, notificationConfig);
        logger.info("Sending notification: " + notificationType + " on event: " + getEventType() +  " for Domain Object: " + domainObject);
        notificationService.sendOnTransactionSuccess(notificationType, senderId,
                addresseeList, priority, notificationContext);
    }

    
    protected Id getSender(DomainObject domainObject,
            NotificationConfig notificationConfig) {

        FindObjectsConfig findPersonConfig = notificationConfig.getNotificationTypeConfig().getSenderConfig();
        if (findPersonConfig != null) {
            Id sender = NotificationSenderEvaluator.findSender(findPersonConfig, domainObject.getId());

            if (sender != null) {
                return sender;
            }

        }
        return currentUserAccessor.getCurrentUserId();
    }
    
    protected List<NotificationAddressee> getAddresseeList(DomainObject domainObject,
            NotificationConfig notificationConfig) {
        List<NotificationAddressee> addresseeList = new  ArrayList<NotificationAddressee>();
        FindObjectsConfig findPerson = notificationConfig.getNotificationTypeConfig().getNotificationAddresseConfig().getFindPerson();
        
        if(findPerson != null) {
            List<Id> personIds =
                    domainObjectFinderService.findObjects(findPerson, domainObject.getId());
            if (personIds != null) {
                for (Id personId : personIds) {
                    addresseeList.add(new NotificationAddresseePerson(personId));
                }
            }
    
        } else if (notificationConfig.getNotificationTypeConfig().getNotificationAddresseConfig()
                .getContextRole() != null) {
            String contextRoleName =
                    notificationConfig.getNotificationTypeConfig().getNotificationAddresseConfig()
                            .getContextRole().getData();
            NotificationAddresseeContextRole addresseeContextRole =
                    new NotificationAddresseeContextRole(contextRoleName, domainObject.getId());
            addresseeList.add(addresseeContextRole);
        } else if (notificationConfig.getNotificationTypeConfig().getNotificationAddresseConfig()
                .getDynamicGroup() != null) {
            String dynamicGroupName =
                    notificationConfig.getNotificationTypeConfig().getNotificationAddresseConfig()
                            .getDynamicGroup().getData();
            NotificationAddresseeDynamicGroup notificationAddresseeDynamicGroup =
                    new NotificationAddresseeDynamicGroup(dynamicGroupName, domainObject.getId());
            addresseeList.add(notificationAddresseeDynamicGroup);
        }
        return addresseeList;
    }


    private void fillAdditionalContextObjects(NotificationContext context, NotificationConfig notificationConfig, DomainObject domainObject) {

        NotificationContextConfig notificationContextConfig = notificationConfig.getNotificationTypeConfig().getNotificationContextConfig();
        if (notificationContextConfig == null || notificationContextConfig.getContextObjects() == null) return;

        for (NotificationContextObject notificationContextObject : notificationContextConfig.getContextObjects()){
            String objectName = notificationContextObject.getName();
            FindNotificationContextObjectsConfig findNotificationContextObjectsConfig = notificationContextObject.getFindNotificationContextObjectsConfig();

            Object object = findNotificationContextObject(context, findNotificationContextObjectsConfig, domainObject);
            if (object != null) {
                if (object instanceof Id) {
                    context.addContextObject(objectName, new DomainObjectAccessor((Id)object));
                } else if (object instanceof DomainObject) {
                    context.addContextObject(objectName, new DomainObjectAccessor((DomainObject)object));
                } else if (object instanceof Dto) {
                    context.addContextObject(objectName, (Dto)object);
                }
            }
        }
    }

    /**
     * Метод получения объектов для контекста уведомлений по конфигурации, описанной notification-type/notification-config
     *
     * @param context контекст уведомлений
     * @param findConfig конфигурация, описывающая получение дополнительных объектов
     * @param domainObject
     * @return объект для добавления в контекст
     */
    private Object findNotificationContextObject(NotificationContext context, FindNotificationContextObjectsConfig findConfig, DomainObject domainObject) {
        try {
            if (findConfig instanceof FindNotificationContextObjectsJavaClassConfig){
                //Поиск с помощью класса
                FindNotificationContextObjectsJavaClassConfig config = (FindNotificationContextObjectsJavaClassConfig) findConfig;
                Class<NotificationContextObjectProducer> producerClass = (Class<NotificationContextObjectProducer>) Class.forName(config.getData());
                NotificationContextObjectProducer producer = producerClass.newInstance();
                return producer.getContextObject(context);
            } else if (findConfig instanceof FindNotificationContextObjectsSpringBeanConfig) {
                // поиск с помощью Spring бина
                FindNotificationContextObjectsSpringBeanConfig config = (FindNotificationContextObjectsSpringBeanConfig) findConfig;
                String beanName = config.getData();
                NotificationContextObjectProducer producer = SpringApplicationContext.getContext().getBean(beanName, NotificationContextObjectProducer.class);
                return producer.getContextObject(context);
            } else if (findConfig instanceof FindNotificationContextObjectsJavaScriptConfig){
                // поиск с помощью JavaScript
                FindNotificationContextObjectsJavaScriptConfig config = (FindNotificationContextObjectsJavaScriptConfig) findConfig;
                String script = config.getData();
                return scriptService.eval(script, new BaseScriptContext(domainObject));
            } else if (findConfig instanceof FindNotificationContextObjectsDoelConfig){
                // поиск с помощью DOEL выражения
                FindNotificationContextObjectsDoelConfig config = (FindNotificationContextObjectsDoelConfig) findConfig;
                return findDoelNotificationContextObject(context, domainObject, config);

            } else if (findConfig instanceof FindNotificationContextObjectsQueryConfig){
                // Поиск с помощью запроса
                FindNotificationContextObjectsQueryConfig config = (FindNotificationContextObjectsQueryConfig) findConfig;
                return findQueryNotificationContextObject(context, domainObject, config);
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SearchException("Error find notification context objects", ex);
        }
    }

    private Object findDoelNotificationContextObject(NotificationContext context, DomainObject domainObject, FindNotificationContextObjectsDoelConfig config) {

        Id sourceObjectId = domainObject.getId();

        String sourceObject = config.getSourceObject();
        if (sourceObject != null && sourceObject.length() > 0) {
            Dto srcContextObject = context.getContextObject(sourceObject);
            if (srcContextObject instanceof Id) {
                sourceObjectId = (Id) srcContextObject;
            } else if (srcContextObject instanceof DomainObjectAccessor) {
                sourceObjectId = ((DomainObjectAccessor) srcContextObject).getId();
            }
        }

        List<Value> values = doelEvaluator.evaluate(
                DoelExpression.parse(config.getData()), sourceObjectId,
                accessService.createSystemAccessToken(getClass().getName()));

        if (values != null) {
            for (Value value : values) {
                if (value instanceof ReferenceValue)
                    return ((ReferenceValue) value).get();
            }
        }

        return null;
    }

    private Object findQueryNotificationContextObject(NotificationContext context, DomainObject domainObject, FindNotificationContextObjectsQueryConfig config) {
        List<Value> params = new ArrayList<Value>();
        String query = replaceSqlParameters(params, config.getData(), context);

        AccessToken accessToken = accessService.createCollectionAccessToken(currentUserAccessor.getCurrentUser());
        IdentifiableObjectCollection collection =
                collectionsDao.findCollectionByQuery(query, params, 0, 1, accessToken);
        if (collection != null) {
            return collection.get(0).getId();
        }

        return null;
    }

    private String replaceSqlParameters(List<Value> params, String query, NotificationContext context) {
        int parameterIndex = 0;
        Set<String> contextNames = context.getContextNames();
        for (String contextName : contextNames) {
            String parameter = "{" + contextName + "}";
            if (query.contains(parameter)){
                query = query.replace(parameter, "{" + (parameterIndex++) + "}");
                Value value;
                Dto contextObject = context.getContextObject(contextName);
                if (contextObject instanceof Id) {
                    value = new ReferenceValue((Id)contextObject);
                } else if (contextObject instanceof DomainObjectAccessor) {
                    value = new ReferenceValue(((DomainObjectAccessor)contextObject).getId());
                } else {
                    throw new SearchException("Context object " + contextName + " has wrong type");
                }
                params.add(value);
            }
        }
        return query;
    }

}
