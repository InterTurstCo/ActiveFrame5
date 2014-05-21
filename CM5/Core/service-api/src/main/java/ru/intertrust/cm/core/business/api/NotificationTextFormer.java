package ru.intertrust.cm.core.business.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationText;

/**
 * Сервис формирования текста сообщения
 * @author larin
 * 
 */
public interface NotificationTextFormer {

    /**
     * Метод формирует текст сообщения по типу сообщения и каналу
     * @param notificationType
     *            тип сообщения
     * @param notificationPart
     *            имя фрагмента сообщения. Используется если сообщение состоит
     *            из нескольких частей, например почтовое сообщение состоит из
     *            заголовка и тела сообщения
     * @param addressee
     *            адресат сообщения
     * @param locale
     *            Идентификатор локали
     * @param channel
     *            имя канала
     * @param context
     *            контекст сообщения
     * @return
     */
    String format(String notificationType, String notificationPart, Id addressee, Id locale, String channel,
            NotificationContext context);

    /**
     * Формирование всех частей текста уведомления по его типу
     * @param notificationType
     *            тип сообщения
     * @param addressee
     *            идентификатор персоны адресата
     * @param locale
     *            Идентификатор локали
     * @param channel
     *            имя канала
     * @param context
     *            контекст сообщения
     * @return
     */
    List<NotificationText> format(String notificationType, Id addressee, Id locale, String channel,
            NotificationContext context);

    /**
     * Проверка наличия конфигурации для типа сообщения и фрагмента сообщения
     * @param notificationType
     *            тип сообщения
     * @param notificationPart
     *            имя фрагмента сообщения
     * @param locale
     *            Локаль сообщения
     * @param channel
     *            канал доставки
     * @return
     */
    boolean contains(String notificationType, String notificationPart, Id locale, String channel);
}
