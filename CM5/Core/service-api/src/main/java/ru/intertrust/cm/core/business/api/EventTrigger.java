package ru.intertrust.cm.core.business.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;

/**
 * Интерфейс класса , определяющего факт возникновения события
 * @author larin
 * 
 */
public interface EventTrigger {

    /**
     * Метод определяющий факт возникновения события
     * @param event
     *            тип события CREATE, CHANGE, CHANGE_STATUS, DELETE
     * @param domainObject
     *            длменный объект по которому произошло событие
     * @param changedFields
     *            измененные поля
     * @return Возвращается флаг сработал триггер или нет
     */
    boolean isTriggered(String eventType, DomainObject domainObject, List<FieldModification> changedFields);

    /**
     * Получение списка имен триггеров сработавших на изменение доменного объекта.
     * @param domainObject
     *            длменный объект по которому произошло событие
     * @param changedFields
     *            измененные поля
     * @return Возвращается список сработавших триггеров, если не сработал не один триггер возвращается пустой список
     */
    List<String> getTriggeredEvents(DomainObject domainObject, List<FieldModification> changedFields);
}
