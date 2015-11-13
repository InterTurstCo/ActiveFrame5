package ru.intertrust.cm.core.business.api.notification;

/**
 * Бин управления сервисом уведомлений
 * @author larin
 *
 */
public interface NotificationServiceController {

    /**
     * Проверка флага активности сервиса уведомлений
     * @return
     */
    public boolean isEnable();
    
    /**
     * Установка флага активности сервиса уведомлений
     * @param enable
     */
    public void setEnable(boolean enable);
}
