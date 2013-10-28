package ru.intertrust.cm.core.dao.access;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис обновления списков доступа.
 * @author atsvetkov
 */
public interface PermissionService {

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
}
