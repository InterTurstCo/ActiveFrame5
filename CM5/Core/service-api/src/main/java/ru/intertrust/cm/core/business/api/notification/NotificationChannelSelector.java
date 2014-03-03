package ru.intertrust.cm.core.business.api.notification;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;

/**
 * Сервис получения списка каналов уведомления
 * @author larin
 * 
 */
public interface NotificationChannelSelector {

    /**
     * Получение списка каналов для сообщения исходя из типа сообщения, адресата и приоритета
     * @param notificationType
     *            тип сообщения
     * @param addressee
     *            идентификатор персоны адресата
     * @param priority
     *            приоритет сообщения
     * @return
     */
    List<String> getNotificationChannels(
            String notificationType,
            Id addressee,
            NotificationPriority priority);
}
