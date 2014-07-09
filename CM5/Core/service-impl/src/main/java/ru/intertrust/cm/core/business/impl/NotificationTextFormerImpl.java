package ru.intertrust.cm.core.business.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.NotificationTextFormer;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationText;
import ru.intertrust.cm.core.model.NotificationException;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;
import ru.intertrust.cm.core.tools.Session;

import java.util.*;

public class NotificationTextFormerImpl implements NotificationTextFormer{
    private static final Logger logger = LoggerFactory.getLogger(NotificationTextFormerImpl.class);

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public String format(String notificationType, String notificationPart, Id addressee, Id locale, String channel, NotificationContext context) {
        IdentifiableObjectCollection collection = findNotificationTextCollection(notificationType, locale, channel);
        String notificationTextTemplate = null;
        for (int i = 0; i < collection.size(); i++){
            IdentifiableObject notificationDo = collection.get(i);
            String part = notificationDo.getString("notification_part");
            if (part.equals(notificationPart)){
                notificationTextTemplate = notificationDo.getString("notification_text");
                break;
            }
        }
        if (notificationTextTemplate == null){
            throw new NotificationException("Notification text not found for (notificationType="
                    + notificationType + "; channel=" + channel + "; locale=" + locale +
                    "; notificationPart=" + notificationPart +")" );
        }

        return formatTemplate(notificationTextTemplate, addressee, context);
    }

    @Override
    public List<NotificationText> format(String notificationType, Id addressee, Id locale, String channel, NotificationContext context) {

        IdentifiableObjectCollection collection = findNotificationTextCollection(notificationType, locale, channel);

        List<NotificationText> ret = new ArrayList<>();

        for (int i = 0; i < collection.size(); i++){
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

    private IdentifiableObjectCollection findNotificationTextCollection(String notificationType, Id locale, String channel) {
        Filter filter = new Filter();
        filter.setFilter("byParams");
        filter.addStringCriterion(0, notificationType);
        filter.addStringCriterion(1, channel);
        filter.addReferenceCriterion(2, locale);

        IdentifiableObjectCollection collection = collectionsService.findCollection("NotificationText", new SortOrder(), Collections.singletonList(filter));
        if (collection.size() == 0) {
            throw new NotificationException("Notification text not found for (notificationType="
                    + notificationType + "; channel=" + channel + "; locale=" + locale + ")");
        }
        return collection;
    }

    private String formatTemplate(String notificationTextTemplate, Id addressee, NotificationContext context) {

        Set<String> contextNames = context.getContextNames();
        if (contextNames == null) return null;

        Map<String, Object> model = new HashMap<>();
        model.put("session", new Session());
        injectBeans(model);

        for (String contextName : contextNames) {
            Dto contextObject = context.getContextObject(contextName);
            if (contextObject instanceof DomainObject){
                model.put(contextName, new DomainObjectAccessor((DomainObject)contextObject));
            } else if (contextObject instanceof Id){
                model.put(contextName, new DomainObjectAccessor((Id)contextObject));
            } else {
                model.put(contextName, contextObject);
            }
        }

        //todo добавить преобразование даты и времени в часовой пояс пользователя addressee

        return FreeMarkerHelper.format(notificationTextTemplate, model);

    }

    private void injectBeans(Map<String, Object> model){
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        /* Вместо явных исключениий добавляем проверку на прототип
        List<String> exceptionBeans = new ArrayList<>();
        exceptionBeans.add("formSaver");
        */
        
        for (String beanDefinitionName : beanDefinitionNames) {
            /* Вместо явных исключениий добавляем проверку на прототип
            if (exceptionBeans.contains(beanDefinitionName)) {
                continue;
            }*/
            if (!((AbstractApplicationContext)applicationContext).isPrototype(beanDefinitionName)){
                Object bean = applicationContext.getBean(beanDefinitionName);
                model.put(beanDefinitionName, bean);
            }
        }
    }

    @Override
    public boolean contains(String notificationType, String notificationPart, Id locale, String channel) {
        IdentifiableObjectCollection collection = findNotificationTextCollection(notificationType, locale, channel);
        boolean result = false;
        for (int i = 0; i < collection.size(); i++){
            IdentifiableObject notificationDo = collection.get(i);
            String part = notificationDo.getString("notification_part");
            if (part.equals(notificationPart)){
                result = true;
                break;
            }
        }        
        return result;
    }

}
