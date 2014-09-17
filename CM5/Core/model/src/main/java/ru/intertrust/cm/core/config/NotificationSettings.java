package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;

/**
 * Интерфейс конфигурации уведомлений
 * @author larin
 *
 */
public interface NotificationSettings extends Dto {
    
    /**
     * Получение типа уведомления
     * @return
     */
    String getName();

    /**
     * Получение приоритета уведомлений
     * @return
     */
    NotificationPriority getPriority();

    /**
     * Получение адресатов
     * @return
     */
    NotificationAddresseConfig getNotificationAddresseConfig();
    
    /**
     * Получение дополнительных объектов контекста
     * @return
     */
    NotificationContextConfig getNotificationContextConfig();
    
    /**
     * Получение отправителей
     * @return
     */
    FindObjectsConfig getSenderConfig();
}
