package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;

/**
 * Интерфейс канала доставки
 * @author larin
 * 
 */
public interface NotificationChannel {
    /**
     * Получение имени канала
     * @return
     */
    String getName();

    /**
     * Описание канала. Может использоваться в GUI для отображения информации о канале в профиле пользователя или
     * системы.
     * @return
     */
    String getDescription();

    /**
     * Отправка сообщения с помощью канала
     * @param notificationType
     *            тип сообщения
     * @param senderId
     *            идентификатор персоны отправителя. Может быть null в случае если отправитель система
     * @param addresseeId
     *            идентификатор персоны адресата
     * @param priority
     *            приоритет
     * @param context
     *            контекст сообщения
     */
    void send(String notificationType, Id senderId, Id addresseeId, NotificationPriority priority, NotificationContext context);
}
