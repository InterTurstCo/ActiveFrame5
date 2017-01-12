package ru.intertrust.cm.core.business.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.Environment;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.NotificationTextFormer;
import ru.intertrust.cm.core.business.api.ThreadContext;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationText;
import ru.intertrust.cm.core.model.NotificationException;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;
import ru.intertrust.cm.core.tools.Session;

public class NotificationTextFormerImpl implements NotificationTextFormer {
    protected static final Logger logger = LoggerFactory.getLogger(NotificationTextFormerImpl.class);

    @Autowired
    protected CollectionsService collectionsService;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected Environment environment;
    
    @Autowired
    protected ThreadContext threadContext;  

    @Override
    public String format(String notificationType, String notificationPart, Id addressee, Id locale, String channel, NotificationContext context) {
        setThreadContext(notificationType, notificationPart, addressee, locale, channel, context);
        IdentifiableObjectCollection collection = findNotificationTextCollection(notificationType, locale, channel);
        String notificationTextTemplate = null;
        for (int i = 0; i < collection.size(); i++) {
            IdentifiableObject notificationDo = collection.get(i);
            String part = notificationDo.getString("notification_part");
            if (part.equals(notificationPart)) {
                notificationTextTemplate = notificationDo.getString("notification_text");
                break;
            }
        }
        String result = null;
        if (notificationTextTemplate == null) {
            result = "MISSING.NOTIFICATION.TEXT." + channel + "." + notificationType + "." + notificationPart;
            logger.warn("Missing notification text " + channel + "." + notificationType + "." + notificationPart);
        }else{
            result = formatTemplate(notificationTextTemplate, addressee, context);
        }

        return result;
    }

    @Override
    public List<NotificationText> format(String notificationType, Id addressee, Id locale, String channel, NotificationContext context) {
        setThreadContext(notificationType, null, addressee, locale, channel, context);

        IdentifiableObjectCollection collection = findNotificationTextCollection(notificationType, locale, channel);

        List<NotificationText> ret = new ArrayList<>();

        for (int i = 0; i < collection.size(); i++) {
            IdentifiableObject notificationDo = collection.get(i);
            String notificationPart = notificationDo.getString("notification_part");
            String notificationTextTemplate = notificationDo.getString("notification_text");

            NotificationText notificationText = new NotificationText();
            notificationText.setPartName(notificationPart);
            notificationText.setText(formatTemplate(notificationTextTemplate, addressee, context));
            ret.add(notificationText);
        }

        return ret;
    }

    protected void setThreadContext(String notificationType, String notificationPart, Id addressee, Id locale, String channel, NotificationContext context){
        threadContext.set(PARAM_NOTIFICATION_TYPE, notificationType);
        threadContext.set(PARAM_NOTIFICATION_PART, notificationPart);
        threadContext.set(PARAM_NOTIFICATION_ADDRESSEE, addressee);
        threadContext.set(PARAM_NOTIFICATION_LOCALE, locale);
        threadContext.set(PARAM_NOTIFICATION_CHANNEL, channel);
        threadContext.set(PARAM_NOTIFICATION_CONTEXT, context);
    }
    
    protected IdentifiableObjectCollection findNotificationTextCollection(String notificationType, Id locale, String channel) {
        Filter filter = new Filter();
        filter.setFilter("byParams");
        filter.addStringCriterion(0, notificationType);
        filter.addStringCriterion(1, channel);
        filter.addReferenceCriterion(2, locale);

        IdentifiableObjectCollection collection = collectionsService.findCollection("NotificationText", new SortOrder(), Collections.singletonList(filter));
        return collection;
    }

    protected String formatTemplate(String notificationTextTemplate, Id addressee, NotificationContext context) {

        Set<String> contextNames = context.getContextNames();
        if (contextNames == null)
            return null;

        Map<String, Object> model = new HashMap<>();
        model.put("session", new Session());
        injectBeans(model);

        for (String contextName : contextNames) {
            Dto contextObject = context.getContextObject(contextName);
            if (contextObject instanceof DomainObject) {
                model.put(contextName, new DomainObjectAccessor((DomainObject) contextObject));
            } else if (contextObject instanceof Id) {
                model.put(contextName, new DomainObjectAccessor((Id) contextObject));
            } else {
                model.put(contextName, contextObject);
            }
        }

        //todo добавить преобразование даты и времени в часовой пояс пользователя addressee

        return FreeMarkerHelper.format(notificationTextTemplate, model);

    }

    protected void injectBeans(Map<String, Object> model) {
        ApplicationContext context = getInjectedApplicationContext();
        String[] beanDefinitionNames = context.getBeanDefinitionNames();

        for (String beanDefinitionName : beanDefinitionNames) {
            if (context.isSingleton(beanDefinitionName) && 
                    !((AbstractApplicationContext)context).getBeanFactory().getBeanDefinition(beanDefinitionName).isAbstract() ) {
                Object bean = context.getBean(beanDefinitionName);
                model.put(beanDefinitionName, bean);
            }
        }

        //Добавляем переменные из server.properties
        model.put("environment", environment);
    }
    
    protected ApplicationContext getInjectedApplicationContext(){
        return applicationContext;
    }

    @Override
    public boolean contains(String notificationType, String notificationPart, Id locale, String channel) {
        IdentifiableObjectCollection collection = findNotificationTextCollection(notificationType, locale, channel);
        boolean result = false;
        for (int i = 0; i < collection.size(); i++) {
            IdentifiableObject notificationDo = collection.get(i);
            String part = notificationDo.getString("notification_part");
            if (part.equals(notificationPart)) {
                result = true;
                break;
            }
        }
        return result;
    }

}
