package ru.intertrust.cm.core.business.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.NotificationTextFormer;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationText;
import ru.intertrust.cm.core.model.NotificationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationTextFormerImpl implements NotificationTextFormer{
    private static final Logger logger = LoggerFactory.getLogger(NotificationTextFormerImpl.class);

    @Autowired
    private CollectionsService collectionsService;

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
        return null;
    }
}
