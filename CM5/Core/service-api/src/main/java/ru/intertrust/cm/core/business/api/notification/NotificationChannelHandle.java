package ru.intertrust.cm.core.business.api.notification;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;

/**
 * Интерфейс канала доставки
 * @author larin
 * 
 */
public interface NotificationChannelHandle {

    public static final String INBOX_NOTIFICATION_CHANNEL = "InboxNotificationChannel";

    public static final String MAIL_NOTIFICATION_CHANNEL = "MailNotificationChannel";

    /**
     * Отправка сообщения с помощью канала
     * @param notificationType
     *            тип сообщения
     * @param senderId
     *            идентификатор персоны отправителя. Может быть null в случае
     *            если отправитель система
     * @param addresseeId
     *            идентификатор персоны адресата
     * @param priority
     *            приоритет
     * @param context
     *            контекст сообщения
     */
    void send(String notificationType, Id senderId, Id addresseeId, NotificationPriority priority, NotificationContext context);

    /**
     * Отправка сообщения с помощью канала
     * @param notificationType
     *            тип сообщения
     * @param senderName
     *            отображаемое имя отправителя. Может быть null в случае если
     *            отправитель система
     * @param addresseeId
     *            идентификатор персоны адресата
     * @param priority
     *            приоритет
     * @param context
     *            контекст сообщения
     */
    void send(String notificationType, String senderName, Id addresseeId, NotificationPriority priority, NotificationContext context);
}
