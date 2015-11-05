package ru.intertrust.cm.core.business.impl.notification;

import org.springframework.beans.factory.annotation.Value;

/**
 * Бин управления сервисом уведомлений
 * @author larin
 *
 */
public class NotificationServiceController {
    
    @Value("${notification.service.enableOnStart:true}")
    private boolean enable;
        
    public boolean isEnable() {
        return enable;
    }
    
    public void setEnable(boolean enable) {
        this.enable = enable;
    }
    
}
