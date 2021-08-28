package ru.intertrust.cm.core.business.api.dto.notification;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.NotificationSettings;
import ru.intertrust.cm.core.config.NotificationTypeConfig;

/**
 * Класс контекста сообщения
 * @author larin
 * 
 */
public class NotificationContext implements Dto{
    
    private Map<String, Dto> contextObjects = new Hashtable<String, Dto>();
    private NotificationSettings notificationSettings;
    private int addresseeIdx = -1;
    
    /**
     * Добавление контекстного объекта
     * @param name
     * @param object
     */
    public void addContextObject(String name, Dto object) {
        contextObjects.put(name, object);
    }

    /**
     * Получение контекстного объекта
     * @param name
     * @return
     */
    public Dto getContextObject(String name) {
        return contextObjects.get(name);
    }
    
    /**
     * Получение6 имен всех контекстных объектов
     * @return
     */
    public Set<String> getContextNames() {
        return contextObjects.keySet();
    }

    @Override
    public String toString() {
        return "NotificationContext [contextObjects=" + contextObjects + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contextObjects == null) ? 0 : contextObjects.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NotificationContext other = (NotificationContext) obj;
        if (contextObjects == null) {
            if (other.contextObjects != null)
                return false;
        } else if (!contextObjects.equals(other.contextObjects))
            return false;
        return true;
    }

    public NotificationSettings getNotificationSettings() {
        return notificationSettings;
    }

    public void setNotificationSettings(NotificationSettings notificationSettings) {
        this.notificationSettings = notificationSettings;
    }

    public int getAddresseeIdx () {
        return this.addresseeIdx;
    }
    
    public void setAddresseeIdx (final int value) {
        this.addresseeIdx = value;
    }
    
}
