package ru.intertrust.cm.core.business.api.access;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
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
    @Deprecated
    boolean isCreatePermitted(String domainObjectType);

    /**
     * Проверка прав на создание ДО. Учитываются права на создание, данные в косвенной матрице. Для правильного
     * оределения родительского типа, на который ссылается косвенная матрица, передается доменный объект. 
     * Родительский тип определяется по типу ссылки, а не по конфигурации ссылочного поля на родителя.
     * @param domainObject
     * @return
     */
    boolean isCreatePermitted(DomainObject domainObject);
    /**
     * Проверка прав на создание дочернего ДО для указанного родительского ДО. Если родительский объект не указан,
     * выполняется проверка прав на создание по типу ДО (разрешение указывается в теге <create> в матрице доступа).
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
