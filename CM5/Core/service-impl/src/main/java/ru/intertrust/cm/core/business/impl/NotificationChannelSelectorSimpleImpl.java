package ru.intertrust.cm.core.business.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelLoader;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelSelector;

/**
 * Реализация простого варианта получения списка каналов исходя из типа сообщения и пользователя. Данная реализация возвращает все доступные каналы
 * для отправки всех уведомлений
 * @author larin
 *
 */
public class NotificationChannelSelectorSimpleImpl implements NotificationChannelSelector {

    @Autowired
    private NotificationChannelLoader notificationChannelLoader;
    
    /**
     * В данной имплементации просто отдаем все найденные каналы
     */
    @Override
    public List<String> getNotificationChannels(String notificationType, Id addressee, NotificationPriority priority) {
        return notificationChannelLoader.getNotificationChannelNames();
    }
}
