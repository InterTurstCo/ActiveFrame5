package ru.intertrust.cm.core.business.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;

/**
 * Сервис проверки условия возникновения события
 * @author larin
 * 
 */
public interface TriggerService {

    /**
     * Метод проверки возникновения события
     * @param event
     *            тип события
     * @param domainObject
     *            доменный объект
     * @param changedFields
     *            измененные поля
     * @return
     */
    boolean isTriggered(String eventType, DomainObject domainObject, List<FieldModification> changedFields);

}
