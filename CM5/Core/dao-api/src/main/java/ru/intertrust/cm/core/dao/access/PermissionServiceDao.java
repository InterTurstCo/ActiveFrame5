package ru.intertrust.cm.core.dao.access;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.AccessMatrixStatusConfig;
import ru.intertrust.cm.core.config.BaseOperationPermitConfig;

/**
 * Сервис обновления списков доступа.
 * @author atsvetkov
 */
public interface PermissionServiceDao {

    /**
     * Пересчет динамических групп при удалении доменного объекта.
     * @param domainObject
     *            доменный объект
     */
    void notifyDomainObjectDeleted(DomainObject domainObject);

    /**
     * Пересчет динамических групп при изменении доменного объекта. Выполняет
     * пересчет дмнамических групп, на состав которых влияет доменный объект.
     * (Доменный объект входит в список отслеживаемых объектов
     * <track-domain-objects> и измененные поля входят в Doel выражение внутри
     * <track-domain-objects>)
     * @param domainObject
     *            изменяемый доменный объект
     * @param modifiedFieldNames
     *            список измененных полей доменного объекта.
     */
    void notifyDomainObjectChanged(DomainObject domainObject, List<FieldModification> modifiedFieldNames);

    /**
     * Пересчет динамических групп при создании отслеживаемого объекта.
     * Выполняет пересчет дмнамических групп, на состав которых влияет
     * отслеживаемый объект. (Доменный объект входит в список отслеживаемых
     * объектов)
     * @param domainObject
     *            создаваемый доменный объект
     */
    void notifyDomainObjectCreated(DomainObject domainObject);

    /**
     * Удаляет списки доступа (таблицы Object_ACL, Object_READ) для доменного
     * объекта.
     * @param objectId
     *            идентификатор доменного объекта, для которого удаляются списки
     *            доступа
     */
    void cleanAclFor(Id objectId);

    /**
     * Получение прав переданного пользователя для переданного доменного объекта
     * @param domainObjectId
     * @param userId
     * @return
     */
    DomainObjectPermission getObjectPermission(Id domainObjectId, Id userId);

    /**
     * Получение прав всех пользователей на переданный доменный объект
     * @param domainObjectId
     * @return
     */
    List<DomainObjectPermission> getObjectPermissions(Id domainObjectId);
    
    /**
     * Получение состава контекстной роли как список пользователей
     * @param contextId
     * @return
     */
    List<Id> getPersons(Id contextId, String roleName);

    /**
     * Получение состава контекстной роли как список групп
     * @param contextId
     * @return
     */
    List<Id> getGroups(Id contextId, String roleName);

    /**
     * Дать временные права на вновь созданный доменный объект. 
     * Необходимо выполнить сразу после создания не дожидаясь окончания транзакции, 
     * чтобы можно юыло уже получать объект запросом в течение текущей транзакции
     * @param domainObject
     */
    void grantNewObjectPermissions(Id domainObject);

    /**
     * Дать временные права на вновь созданные доменные объекты. Необходимо выполнить сразу после создания не дожидаясь
     * окончания транзакции, чтобы можно юыло уже получать объект запросом в течение текущей транзакции
     * @param domainObjectIds
     */
    void grantNewObjectPermissions(List<Id> domainObjectIds);

    /**
     * Производит пересчет всех ACL, которые должны пересчитатся в конце транзакции, не дожидаясь окончания транзакции
     */
    void refreshAcls();
    
    /**
     * Пересчет ACL для доменного обьекта с переданным идентификатором 
     * @param domainObjectId
     */
    void refreshAclFor(Id domainObjectId);

    /**
     * Отправкауведомления сервису о смене статуса доменного объекта
     * @param domainObject
     */
    void notifyDomainObjectChangeStatus(DomainObject domainObject);

    /**
     * Обновление прав доступа к доменным объектам не дожидаясь конца транзакции.
     * Пересчет произведется только если ДО с переданными идентификаторами помечены для пересчета в конце транзакции.
     * Если в качестве параметра передан null то пересчитаются все права у ДО, которые помечены для пересчета в текущей транзакции
     * @param invalidContextIds
     */
    void refreshAclIfMarked(Set<Id> invalidContextIds);

    /**
     * Проверка, ограничен ли доступ к вложению на операцию чтения содержимого
     *
     * @param attachId id вложения - по нему будем искать ограничения в таблице с правами
     * @return true, если доступ ограничен
     */
    boolean checkIfContentReadRestricted(Id attachId);

    /**
     * Вычисление вложений, к которым доступ не ограничен. Не проверяет соответствие типа вложений соответствующему параметру.
     *
     * @param objectIds список id проверяемых объектов
     * @param objType тип вложений
     * @return множество id объектов, доступ к которым не был ограничен
     */
    @Nonnull
    Set<Id> checkIfContentReadAllowedForAll(List<Id> objectIds, String objType);

    /**
     * Вычисление вложений, доступ к которым ограничен. Не проверяет соответствие типа вложений соответствующему параметру.
     *
     * @param objectIds список id проверяемых объектов
     * @param objType тип вложений
     * @return множество id объектов, доступ к которым был ограничен
     */
    @Nonnull
    Set<Id> checkIfContentReadRestricted(List<Id> objectIds, String objType);

    /**
     * Проверка на то что конфигурация прав не зависит от контекста
     * @param matrixStatusConfig
     * @param accessType
     * @return
     */
    boolean isWithoutContextPermissionConfig(AccessMatrixStatusConfig matrixStatusConfig, AccessType accessType);

    /**
     * Проверка соответствия настройки прав в матрице проверяемому типу доступа
     * @param permitConfig
     * @param accessType
     * @return
     */
    boolean checkAccessType(BaseOperationPermitConfig permitConfig, AccessType accessType);

    /**
     * Проверка входит ли пользователь в группу
     * @param userId
     * @param groupName
     * @return
     */
    boolean checkUserGroup(Id userId, String groupName);

    /**
     * Проверка входит ли пользователь хотя бы в одну группу из набора
     * @param userId
     * @param groupNames
     * @return
     */
    boolean checkUserGroups(Id userId, Set<String> groupNames);

    /**
     * Проверка имеет ли пользователь права на тип
     * @param personId
     * @param accessType
     * @param matrixStatusConfig
     * @return
     */
    boolean hasUserTypePermission(Id personId, AccessType accessType, AccessMatrixStatusConfig matrixStatusConfig);

    /**
     * Получение имени типа, у которго заимствует права конкретный доменный объект
     * (очень не хватает access_object_id_type для этого :(((, было бы гораздо проще)
     * Путем анализа матриц невозможно получить однозначнго имя типа, у которого мы заимствуем права, так как
     * матрицы могут быть и у дочерних доменных объектов.
     * @param refTypeName имя типа, у которого заимствуем права полученное путем анализа матриц доступа.
     * @param objectId ид доменного объекта
     * @return
     */
    String getRealMatrixReferenceTypeName(String refTypeName, Id objectId);
}
