package ru.intertrust.cm.core.business.api.notification;

/**
 * Фабрика по получению сервиса выбора каналов отправки уведомлений. необходима
 * для возможности реализации кастомного сервиса в проектах использующих
 * платформу, в случае если текущая пеализация не приемлема
 * @author larin
 * 
 */
public interface NotificationChannelSelectorFactory {
    
    /**
     * Получение сервиса NotificationChannelSelector 
     * @return
     */
    NotificationChannelSelector getService();
}
