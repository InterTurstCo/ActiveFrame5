package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Profile;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelLoader;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelSelector;
import ru.intertrust.cm.core.model.ProfileException;

import java.util.ArrayList;
import java.util.List;

/**
 * Реализация получения списка каналов с использованием профиля пользователя
 */
public class NotificationChannelSelectorImpl implements NotificationChannelSelector {

    @Autowired
    private NotificationChannelLoader notificationChannelLoader;

    @Autowired
    private ProfileService profileService;

    private final static String NOTIFICATION = "NOTIFICATION";
    private final static String KEY_SEP = ".";
    private final static String KEY_ANY = "*";

    private final static String DISABLE = "DISABLE";
    private final static String HIGH = "HIGH";
    private final static String NORMAL = "NORMAL";
    private final static String LOW = "LOW";

    /**
     * Получение списка каналов для сообщения исходя из типа сообщения, адресата и приоритета
     */
    @Override
    public List<String> getNotificationChannels(String notificationType, Id addressee, NotificationPriority priority) {
        List<String> channelNames = notificationChannelLoader.getNotificationChannelNames();
        if (channelNames == null || channelNames.isEmpty()) return channelNames;

        Profile personProfile = profileService.getPersonProfileByPersonId(addressee);

        List<String> resultChannelNames = new ArrayList<>(channelNames.size());

        for (String channelName : channelNames) {
            if (isChannelEnabled(channelName, notificationType, priority, personProfile )){
                resultChannelNames.add(channelName);
            }
        }

        return resultChannelNames;
    }

    protected boolean isChannelEnabled(String channelName, String notificationType, NotificationPriority priority, Profile personProfile) {
        List<String> keys = personProfile.getFields();
        if (keys == null || keys.isEmpty()) return false;

        // находим полное соответствие
        String key = NOTIFICATION + KEY_SEP + channelName + KEY_SEP + notificationType;
        if (keys.contains(key)) return priorityCheck(personProfile.getString(key), priority);

        // ищем ключ для всех типов сообещний
        String key2 = NOTIFICATION + KEY_SEP + channelName + KEY_SEP + KEY_ANY;
        if (keys.contains(key2)) return priorityCheck(personProfile.getString(key2), priority);

        // ищем ключ для всех типов сообещний и каналов
        String key3 = NOTIFICATION + KEY_SEP + KEY_ANY;
        if (keys.contains(key3)) return priorityCheck(personProfile.getString(key3), priority);

        return false;
    }

    private boolean priorityCheck(String prioritySetting, NotificationPriority priority) {
        if (DISABLE.equalsIgnoreCase(prioritySetting)){
            return false;
        } else if (HIGH.equalsIgnoreCase(prioritySetting)){
            return priority == NotificationPriority.HIGH;
        } else if (NORMAL.equalsIgnoreCase(prioritySetting)){
            return priority == NotificationPriority.HIGH || priority == NotificationPriority.NORMAL;
        } else if (LOW.equalsIgnoreCase(prioritySetting)){
            return true;
        } else {
            throw new ProfileException("Unsupported value=" + prioritySetting +
                    " for priority in user profile");
        }
    }
}
