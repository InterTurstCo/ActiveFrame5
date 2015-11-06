package ru.intertrust.cm.core.business.impl.notification;

import org.springframework.beans.factory.annotation.Value;

import ru.intertrust.cm.core.business.api.notification.NotificationServiceController;

/**
 * Бин управления сервисом уведомлений
 * @author larin
 *
 */
public class NotificationServiceControllerImpl implements NotificationServiceController{
    
    @Value("${notification.service.enableOnStart:true}")
    private boolean enable;
    
    @Override
    public boolean isEnable() {
        return enable;
    }
    
    @Override
    public void setEnable(boolean enable) {
        this.enable = enable;
    }    
}
