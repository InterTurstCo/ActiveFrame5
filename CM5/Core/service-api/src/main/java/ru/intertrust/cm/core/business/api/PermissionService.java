package ru.intertrust.cm.core.business.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис получения прав доступа для доменного объекта
 * @author larin
 *
 */
public interface PermissionService {

    public interface Remote extends PermissionService{        
    }
    
    /**
     * Метод возвращает класс с информацией о правах текущего пользователя для доменного объекта
     * @return
     */
    DomainObjectPermission getObjectPermission(Id domainObjectId);

    /**
     * Метод возвращает класс с информацией о правах переданного пользователя для доменного объекта
     * @return
     */
    DomainObjectPermission getObjectPermission(Id domainObjectId, Id userId);

    /**
     * Метод возвращает права всех пользователей для переданного доменного объекта
     * @param domainObjectId
     * @return
     */
    List<DomainObjectPermission> getObjectPermissions(Id domainObjectId);
    
    
}
