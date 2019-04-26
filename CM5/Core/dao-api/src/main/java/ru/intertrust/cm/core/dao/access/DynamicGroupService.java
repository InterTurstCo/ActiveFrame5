package ru.intertrust.cm.core.dao.access;

import java.util.List;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.DynamicGroupConfig;

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
    void notifyDomainObjectDeleted(DomainObject domainObject, Set<Id> beforeSaveInvalicContexts);

    /**
     * Пересчет динамических групп при изменении доменного объекта. Выполняет пересчет дмнамических групп, на состав которых влияет доменный объект. (Доменный
     * объект входит в список отслеживаемых объектов <track-domain-objects> и измененные поля входят в Doel выражение внутри <track-domain-objects>)
     * @param objectId
     *            изменяемый доменный объект
     * @param modifiedFieldNames
     *            список измененных полей доменного объекта.
     */
    void notifyDomainObjectChanged(DomainObject domainObject, List<FieldModification> modifiedFieldNames, Set<Id> beforeSaveInvalicContexts);

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
    Set<Id> getInvalidGroupsBeforeChange(DomainObject domainObject, List<FieldModification> modifiedFieldNames);

    /**
     * Метод получения невалидных контекстов для динамических групп. Вызывается перед удалением данных из базы. Необходим для учета
     * состояния данных как до изменения так и после
     * @param domainObject
     * @param modifiedFieldNames
     * @return
     */
    Set<Id> getInvalidGroupsBeforeDelete(DomainObject domainObject);
    
    /**
     * Поиск группы (динамической или статической) по имени.
     * @param groupName имя группы
     * @return идентификатор группы
     */
    Id getUserGroupByGroupName(String groupName);
 
    /**
     * Получение состава динамической группы с учетом вхождения группы в группу
     * @param contextId
     * @return
     */
    List<Id> getPersons(Id contextId, String groupName);

    /**
     * Пересчет состава динамической группы по ее идентификатору
     * @param groupId
     */
    void recalcGroup(Id groupId);

    /**
     * Пересчет состава динамической группы по имени и идентификатору контекста
     * @param groupName Имя группы
     * @param contextId Идентификатор контекста группы
     */
    void recalcGroup(String groupName, Id contextId);
    
    
    /**
     * Получение конфигураций всех динамических групп для типа
     * @param typeName Имя типа, для которого получаем конфигурации групп
     * @return
     */
    List<DynamicGroupConfig> getTypeDynamicGroupConfigs(String typeName);
}
