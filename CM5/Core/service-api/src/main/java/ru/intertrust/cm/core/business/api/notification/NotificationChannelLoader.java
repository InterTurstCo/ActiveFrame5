package ru.intertrust.cm.core.business.api.notification;

import java.util.List;

/**
 * Сервис поиска всех доступных каналов доставки уведомлений и формирования их реестра
 * @author larin
 *
 */
public interface NotificationChannelLoader {
    
    /**
     * Получение списка имен всех каналов отправки уведомлений
     * @return
     */
    List<String> getNotificationChannelNames();
    
    /**
     * Получение информации о канале доставки
     * @param channelName
     * @return
     */
    NotificationChannelInfo getNotificationChannelInfo(String channelName);
    
    /**
     * Получение обработчика канала отправки уведомления по его имени
     * @param channelName
     * @return
     */
    NotificationChannelHandle getNotificationChannel(String channelName);

}
