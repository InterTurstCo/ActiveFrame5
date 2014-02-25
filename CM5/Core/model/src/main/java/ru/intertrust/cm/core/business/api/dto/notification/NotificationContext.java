package ru.intertrust.cm.core.business.api.dto.notification;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Класс контекста сообщения
 * @author larin
 * 
 */
public class NotificationContext implements Dto{
    private Map<String, Dto> contextObjects = new Hashtable<String, Dto>();
    
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
    
}
