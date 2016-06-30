package ru.intertrust.cm.core.dao.access;

import java.util.List;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис обновления списков доступа.
 * @author atsvetkov
 */
public interface PermissionServiceDao {

    /**
     * Пересчет динамических групп при удалении доменного объекта.
     * @param objectId
     *            доменный объект
     */
    void notifyDomainObjectDeleted(DomainObject domainObject);

    /**
     * Пересчет динамических групп при изменении доменного объекта. Выполняет
     * пересчет дмнамических групп, на состав которых влияет доменный объект.
     * (Доменный объект входит в список отслеживаемых объектов
     * <track-domain-objects> и измененные поля входят в Doel выражение внутри
     * <track-domain-objects>)
     * @param objectId
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
     * @param objectId
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
     * @param domainObject
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
}
