package ru.intertrust.cm.core.business.api.access;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Проверяет права доступа к объектам и на выполнение действий (action) без выполнения самой операции. Работает с
 * текущим пользователем системы.
 * @author atsvetkov
 */
public interface AccessVerificationService {

    public interface Remote extends AccessVerificationService {
    }

    /**
     * Проверка прав на чтение.
     * @param objectId идентификатор проверяемого объекта 
     * @return
     */
    boolean isReadPermitted(Id objectId);

    /**
     * Проверка прав на запись.
     * @param objectId идентификатор проверяемого объекта
     * @return
     */
    boolean isWritePermitted(Id objectId);

    /**
     * Проверка прав на удаление.
     * @param objectId идентификатор проверяемого объекта
     * @return
     */
    boolean isDeletePermitted(Id objectId);

    /**
     * Проверка прав на создание несвязанного ДО (без учета его связи с другими ДО).
     * @param domainObjectType тип доменного объекта
     * @return
     */
    boolean isCreatePermitted(String domainObjectType);

    /**
     * Проверка прав на создание дочернего ДО для указанного родительского ДО.
     * @param domainObjectType тип создаваемого доменного объекта
     * @param parentObjectId идентификатор родительского доменного объекта
     * @return
     */
    boolean isCreateChildPermitted(String domainObjectType, Id parentObjectId);

    /**
     * Проверка прав на выполнение действий.
     * @param action название(идентификатор) действия 
     * @param objectId идентификатор проверяемого объекта
     * @return
     */
    boolean isExecuteActionPermitted(String actionName, Id objectId);

}
