package ru.intertrust.cm.core.business.api.notification;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationTaskMode;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.config.NotificationAddresseConfig;
import ru.intertrust.cm.core.config.NotificationContextConfig;
import ru.intertrust.cm.core.config.NotificationSettings;

/**
 * Параметр периодического задания отправки уведомлений
 * @author larin
 *
 */
@Root
public class NotificationTaskConfig implements ScheduleTaskParameters, NotificationSettings {

    private static final long serialVersionUID = 2618754657538579112L;

    /**
     * Описание способа получения доменных объектов
     */
    @Element
    private FindObjectsConfig findDomainObjects;
    
    @Element(name = "addressee", required = false)
    private NotificationAddresseConfig notificationAddresseConfig;
    
    @Element(name = "sender", required = false)
    private FindObjectsConfig notificationSenderConfig;
    
    @Element(name = "context-config", required = false)
    private NotificationContextConfig notificationContextConfig;
    
    /**
     * Тип сообщения
     */
    @Attribute
    private String notificationType;
    
    /**
     * Приоритет сообщения
     */
    @Attribute
    private NotificationPriority notificationPriority;
    
    /**
     * Флаг типа формирования сообщений, относительно доменного объекта или отностительно персоны 
     */
    @Attribute
    private NotificationTaskMode taskMode;
    
    public void setName(String notificationType) {
        this.notificationType = notificationType;
    }
    
    public void setPriority(NotificationPriority notificationPriority) {
        this.notificationPriority = notificationPriority;
    }
    
    public FindObjectsConfig getFindDomainObjects() {
        return findDomainObjects;
    }
    
    public void setFindDomainObjects(FindObjectsConfig findDomainObjects) {
        this.findDomainObjects = findDomainObjects;
    }
    
    public NotificationTaskMode getTaskMode() {
        return taskMode;
    }
    
    public void setTaskMode(NotificationTaskMode taskMode) {
        this.taskMode = taskMode;
    }

    @Override
    public NotificationContextConfig getNotificationContextConfig() {
        return notificationContextConfig;
    }

    public void setNotificationContextConfig(NotificationContextConfig notificationContextConfig) {
        this.notificationContextConfig = notificationContextConfig;
    }

    @Override
    public NotificationAddresseConfig getNotificationAddresseConfig() {
        return notificationAddresseConfig;
    }

    public void setNotificationAddresseConfig(NotificationAddresseConfig notificationAddresseConfig) {
        this.notificationAddresseConfig = notificationAddresseConfig;
    }

    public void setSenderConfig(FindObjectsConfig notificationSenderConfig) {
        this.notificationSenderConfig = notificationSenderConfig;
    }

    @Override
    public String getName() {
        return notificationType;
    }

    @Override
    public FindObjectsConfig getSenderConfig() {
        return notificationSenderConfig;
    }

    @Override
    public NotificationPriority getPriority() {
        return notificationPriority;
    }
    
}
