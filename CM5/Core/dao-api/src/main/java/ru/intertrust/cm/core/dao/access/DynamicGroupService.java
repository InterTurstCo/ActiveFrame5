package ru.intertrust.cm.core.dao.access;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис по работе с динамическими группами пользователей. Выполняет пересчет/удаление динамических групп.
 * @author atsvetkov
 */
public interface DynamicGroupService {

    /**
     * Пересчет динамических групп при удалении доменного объекта.
     * @param objectId
     *            доменный объект
     */
    void notifyDomainObjectDeleted(DomainObject domainObject, List<Id> beforeSaveInvalicContexts);

    /**
     * Пересчет динамических групп при изменении доменного объекта. Выполняет пересчет дмнамических групп, на состав которых влияет доменный объект. (Доменный
     * объект входит в список отслеживаемых объектов <track-domain-objects> и измененные поля входят в Doel выражение внутри <track-domain-objects>)
     * @param objectId
     *            изменяемый доменный объект
     * @param modifiedFieldNames
     *            список измененных полей доменного объекта.
     */
    void notifyDomainObjectChanged(DomainObject domainObject, List<FieldModification> modifiedFieldNames, List<Id> beforeSaveInvalicContexts);

    /**
     * Пересчет динамических групп при создании отслеживаемого объекта. Выполняет пересчет дмнамических групп, на состав которых влияет отслеживаемый объект.
     * (Доменный объект входит в список отслеживаемых объектов)
     * @param objectId
     *            создаваемый доменный объект
     */
    void notifyDomainObjectCreated(DomainObject domainObject);

    /**
     * Метод получения невалидных контекстов для динамических групп. Вызывается перед изменением данных из базы. Необходим для учета
     * состояния данных как до изменения так и после
     * @param domainObject
     * @return
     */
    List<Id> getInvalidGroupsBeforeChange(DomainObject domainObject, List<FieldModification> modifiedFieldNames);

    /**
     * Метод получения невалидных контекстов для динамических групп. Вызывается перед удалением данных из базы. Необходим для учета
     * состояния данных как до изменения так и после
     * @param domainObject
     * @param modifiedFieldNames
     * @return
     */
    List<Id> getInvalidGroupsBeforeDelete(DomainObject domainObject);
    
}
