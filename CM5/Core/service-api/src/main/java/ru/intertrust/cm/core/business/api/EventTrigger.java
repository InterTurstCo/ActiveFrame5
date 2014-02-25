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
     * @return
     */
    boolean isTriggered(String eventType, DomainObject domainObject, List<FieldModification> changedFields);
}
