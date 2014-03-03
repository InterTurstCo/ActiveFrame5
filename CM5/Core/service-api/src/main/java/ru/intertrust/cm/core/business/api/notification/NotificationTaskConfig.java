package ru.intertrust.cm.core.business.api.notification;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationTaskMode;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.config.FindObjectsConfig;

/**
 * Параметр периодического задания отправки уведомлений
 * @author larin
 *
 */
public class NotificationTaskConfig implements ScheduleTaskParameters {

    private static final long serialVersionUID = 2618754657538579112L;

    /**
     * Описание способа получения доменных объектов
     */
    @Attribute
    private FindObjectsConfig findDomainObjects;
    
    /**
     * Описание способа получения персон
     */
    @Attribute
    private FindObjectsConfig findPersons;
    
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
    
    public String getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
    
    public NotificationPriority getNotificationPriority() {
        return notificationPriority;
    }
    
    public void setNotificationPriority(NotificationPriority notificationPriority) {
        this.notificationPriority = notificationPriority;
    }
    
    public FindObjectsConfig getFindDomainObjects() {
        return findDomainObjects;
    }
    
    public void setFindDomainObjects(FindObjectsConfig findDomainObjects) {
        this.findDomainObjects = findDomainObjects;
    }
    
    public FindObjectsConfig getFindPersons() {
        return findPersons;
    }
    
    public void setFindPersons(FindObjectsConfig findPersons) {
        this.findPersons = findPersons;
    }
    
    public NotificationTaskMode getTaskMode() {
        return taskMode;
    }
    
    public void setTaskMode(NotificationTaskMode taskMode) {
        this.taskMode = taskMode;
    }
}
