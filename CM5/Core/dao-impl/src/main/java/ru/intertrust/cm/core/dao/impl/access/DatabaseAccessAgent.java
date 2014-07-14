package ru.intertrust.cm.core.dao.impl.access;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AccessType;

/**
 * Интерфейс, через который реализация службы контроля доступа {@link AccessControlServiceImpl}
 * осуществляет собственно проверки прав доступа.
 * <p>Реализация интерфейса формирует запросы к БД и интерпретирует их результаты.
 * 
 * @author apirozhkov
 */
public interface DatabaseAccessAgent {

    /**
     * Проверяет доступ заданного типа заданного пользователя к заданному объекту.
     * 
     * @param userId Идентификатор пользователя
     * @param objectId Идентификатор доменного объекта
     * @param type Тип доступа
     * @return true если пользователь имеет разрешение на запрошенный доступ к запрошенному объекту
     */
    boolean checkDomainObjectAccess(int userId, Id objectId, AccessType type);

    /**
     * Проверяет доступ заданного типа заданного пользователя к заданным объектам.
     * 
     * @param userId Идентификатор пользователя
     * @param objectIds Массив идентификаторов доменных объектов
     * @param type Тип доступа
     * @return Массив идентификаторов объектов, доступ к которым разрешён
     */
    Id[] checkMultiDomainObjectAccess(int userId, Id[] objectIds, AccessType type);

    /**
     * Проверяет доступ заданных типов заданного пользователя к заданному объекту
     * 
     * @param userId Идентификатор пользователя
     * @param objectId Идентификатор доменного объекта
     * @param types Массив типов доступа
     * @return Массив типов доступа, разрешённых пользователю
     */
    AccessType[] checkDomainObjectMultiAccess(int userId, Id objectId, AccessType[] types);

    /**
     * Проверяет включение пользователя в заданную статическую группу.
     * 
     * @param userId Идентификатор группы
     * @param groupName Название группы
     * @return true если пользователь входит в группу
     */
    boolean checkUserGroup(int userId, String groupName);
    
    /**
     * Разрешена ли опрерация создания доменных объектов данного типа для пользователей из статических и безконтекстных
     * динамических групп.
     * @param userId идентификатор пользователя
     * @param objectType тип доменного объекта
     * @return
     */
    boolean isAllowedToCreateByStaticGroups(Id userId, String objectType);
    
    /**
     * 
     * @param userId
     * @param groupName
     * @param contextObjectId
     * @return
     */
    public boolean isUserInGroup(Id userId, String groupName, Id contextObjectId);

}
