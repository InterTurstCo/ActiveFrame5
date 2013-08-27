package ru.intertrust.cm.core.dao.access;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис обновления списков доступа.
 * @author atsvetkov
 */
public interface PermissionService {

    /**
     * Пересчитывает списки доступа (таблицы Object_ACL, Object_READ) для доменного объекта. Сначала удаляет все списки
     * доступа для доменного объекта, потом персчитывает заново.
     * @param objectId идентификатор доменного объекта, для которого персчитываются списки доступа
     */
    void refreshAclFor(Id objectId);

    /**
     * Удаляет списки доступа (таблицы Object_ACL, Object_READ) для доменного объекта.
     * @param objectId идентификатор доменного объекта, для которого удаляются списки доступа
     */
    void deleteAclFor(Id objectId);
}
