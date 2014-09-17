package ru.intertrust.cm.core.business.impl.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.ScriptService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeContextRole;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeDynamicGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseePerson;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.notification.NotificationContextObjectProducer;
import ru.intertrust.cm.core.config.FindNotificationContextObjectsConfig;
import ru.intertrust.cm.core.config.FindNotificationContextObjectsDoelConfig;
import ru.intertrust.cm.core.config.FindNotificationContextObjectsJavaClassConfig;
import ru.intertrust.cm.core.config.FindNotificationContextObjectsJavaScriptConfig;
import ru.intertrust.cm.core.config.FindNotificationContextObjectsQueryConfig;
import ru.intertrust.cm.core.config.FindNotificationContextObjectsSpringBeanConfig;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.config.NotificationContextConfig;
import ru.intertrust.cm.core.config.NotificationContextObject;
import ru.intertrust.cm.core.config.NotificationSettings;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DoelEvaluator;
import ru.intertrust.cm.core.dao.api.DomainObjectFinderService;
import ru.intertrust.cm.core.model.SearchException;
import ru.intertrust.cm.core.tools.BaseScriptContext;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;
import ru.intertrust.cm.core.util.SpringApplicationContext;

public abstract class NotificationSenderBase {
    @Autowired
    protected ScriptService scriptService;

    @Autowired
    protected DoelEvaluator doelEvaluator;

    @Autowired
    protected AccessControlService accessService;

    @Autowired
    protected CollectionsDao collectionsDao;

    @Autowired
    protected CurrentUserAccessor currentUserAccessor;

    @Autowired
    protected DomainObjectFinderService domainObjectFinderService;

    protected void fillAdditionalContextObjects(NotificationContext context, NotificationContextConfig notificationContextConfig, DomainObject domainObject) {

        if (notificationContextConfig == null || notificationContextConfig.getContextObjects() == null)
            return;

        for (NotificationContextObject notificationContextObject : notificationContextConfig.getContextObjects()) {
            String objectName = notificationContextObject.getName();
            FindNotificationContextObjectsConfig findNotificationContextObjectsConfig = notificationContextObject.getFindNotificationContextObjectsConfig();

            Object object = findNotificationContextObject(context, findNotificationContextObjectsConfig, domainObject);
            if (object != null) {
                if (object instanceof Id) {
                    context.addContextObject(objectName, new DomainObjectAccessor((Id) object));
                } else if (object instanceof DomainObject) {
                    context.addContextObject(objectName, new DomainObjectAccessor((DomainObject) object));
                } else if (object instanceof Dto) {
                    context.addContextObject(objectName, (Dto) object);
                }
            }
        }
    }

    /**
     * Метод получения объектов для контекста уведомлений по конфигурации,
     * описанной notification-type/notification-config
     * 
     * @param context
     *            контекст уведомлений
     * @param findConfig
     *            конфигурация, описывающая получение дополнительных объектов
     * @param domainObject
     * @return объект для добавления в контекст
     */
    protected Object findNotificationContextObject(NotificationContext context, FindNotificationContextObjectsConfig findConfig, DomainObject domainObject) {
        try {
            if (findConfig instanceof FindNotificationContextObjectsJavaClassConfig) {
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
            } else if (findConfig instanceof FindNotificationContextObjectsJavaScriptConfig) {
                // поиск с помощью JavaScript
                FindNotificationContextObjectsJavaScriptConfig config = (FindNotificationContextObjectsJavaScriptConfig) findConfig;
                String script = config.getData();
                return scriptService.eval(script, new BaseScriptContext(domainObject));
            } else if (findConfig instanceof FindNotificationContextObjectsDoelConfig) {
                // поиск с помощью DOEL выражения
                FindNotificationContextObjectsDoelConfig config = (FindNotificationContextObjectsDoelConfig) findConfig;
                return findDoelNotificationContextObject(context, domainObject, config);

            } else if (findConfig instanceof FindNotificationContextObjectsQueryConfig) {
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

    protected Object findDoelNotificationContextObject(NotificationContext context, DomainObject domainObject, FindNotificationContextObjectsDoelConfig config) {

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

    protected Object
            findQueryNotificationContextObject(NotificationContext context, DomainObject domainObject, FindNotificationContextObjectsQueryConfig config) {
        List<Value> params = new ArrayList<Value>();
        String query = replaceSqlParameters(params, config.getData(), context);

        AccessToken accessToken = accessService.createCollectionAccessToken(currentUserAccessor.getCurrentUser());
        IdentifiableObjectCollection collection =
                collectionsDao.findCollectionByQuery(query, params, 0, 1, accessToken);
        if (collection != null && collection.size() > 0) {
            return collection.get(0).getId();
        }

        return null;
    }

    protected String replaceSqlParameters(List<Value> params, String query, NotificationContext context) {
        int parameterIndex = 0;
        Set<String> contextNames = context.getContextNames();
        for (String contextName : contextNames) {
            String parameter = "{" + contextName + "}";
            if (query.contains(parameter)) {
                query = query.replace(parameter, "{" + (parameterIndex++) + "}");
                Value value;
                Dto contextObject = context.getContextObject(contextName);
                if (contextObject instanceof Id) {
                    value = new ReferenceValue((Id) contextObject);
                } else if (contextObject instanceof DomainObjectAccessor) {
                    value = new ReferenceValue(((DomainObjectAccessor) contextObject).getId());
                } else {
                    throw new SearchException("Context object " + contextName + " has wrong type");
                }
                params.add(value);
            }
        }
        return query;
    }

    protected List<NotificationAddressee> getAddresseeList(Id domainObject,
            NotificationSettings notificationSettings) {
        List<NotificationAddressee> addresseeList = new ArrayList<NotificationAddressee>();
        FindObjectsConfig findPerson = notificationSettings.getNotificationAddresseConfig().getFindPerson();

        if (findPerson != null) {
            List<Id> personIds =
                    domainObjectFinderService.findObjects(findPerson, domainObject, notificationSettings);
            if (personIds != null) {
                for (Id personId : personIds) {
                    addresseeList.add(new NotificationAddresseePerson(personId));
                }
            }

        } else if (notificationSettings.getNotificationAddresseConfig()
                .getContextRole() != null) {
            String contextRoleName =
                    notificationSettings.getNotificationAddresseConfig()
                            .getContextRole().getData();
            NotificationAddresseeContextRole addresseeContextRole = new NotificationAddresseeContextRole(contextRoleName, domainObject);
            addresseeList.add(addresseeContextRole);
        } else if (notificationSettings.getNotificationAddresseConfig()
                .getDynamicGroup() != null) {
            String dynamicGroupName = notificationSettings.getNotificationAddresseConfig().getDynamicGroup().getData();
            NotificationAddresseeDynamicGroup notificationAddresseeDynamicGroup =
                    new NotificationAddresseeDynamicGroup(dynamicGroupName, domainObject);
            addresseeList.add(notificationAddresseeDynamicGroup);
        }
        return addresseeList;
    }

}
