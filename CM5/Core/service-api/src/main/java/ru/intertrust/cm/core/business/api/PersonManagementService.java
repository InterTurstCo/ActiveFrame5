package ru.intertrust.cm.core.business.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

import javax.validation.constraints.NotNull;

/**
 * Сервис работы с пользователями и группами
 * @author larin
 * 
 */
public interface PersonManagementService {

    public interface Remote extends PersonManagementService {
    }

    /**
     * Получение идентификатора персоны по его логину
     * @param login
     * @return
     */
    Id getPersonId(String login);

    /**
     * Получение идентификатора группы по имени
     * @param groupName
     * @return
     */
    Id getGroupId(String groupName);

    /**
     * Получение списка персон, входящих в группу
     * @param groupId
     * @return
     */
    List<DomainObject> getPersonsInGroup(Id groupId);

    /**
     * Получение списка персон входящих в группу, с учетом вхождения группы в группу
     * @param groupId
     * @return
     */
    List<DomainObject> getAllPersonsInGroup(Id groupId);

    /**
     * Проверка входит ли персона в группу с учетом вхождения группы в группу
     * @param groupId
     * @return
     */
    boolean isPersonInGroup(Id groupId, Id personId);

    /**
     * Получения списка групп, в которые входит персона с учетом наследования
     * @param personId
     * @return
     */
    List<DomainObject> getPersonGroups(Id personId);

    /**
     * Проверка вхождения группы в группу.
     * @param parent
     * @param child
     * @param recursive
     *            учитывать вхождение группы в группу
     * @return
     */
    boolean isGroupInGroup(Id parent, Id child, boolean recursive);

    /**
     * Получение всех родительских групп для группы, с учетом вхождения группы в группу
     * @param parent
     * @return
     */
    List<DomainObject> getAllParentGroup(Id child);

    /**
     * Получение групп, непосредственно входящие в группу
     * @param parent
     * @return
     */
    List<DomainObject> getChildGroups(Id parent);

    /**
     * Получение групп входящих в группу с учетом вхождения группы в группу
     * @param parent
     * @return
     */
    List<DomainObject> getAllChildGroups(Id parent);

    /**
     * Добавление персоны в группу
     * @param group
     * @param person
     */
    void addPersonToGroup(Id group, Id person);

    /**
     * Добавление группы в группу
     * @param parent
     * @param child
     */
    void addGroupToGroup(Id parent, Id child);

    /**
     * Удаление персоны из группы
     * @param group
     * @param person
     */
    void remotePersonFromGroup(Id group, Id person);

    /**
     * Удаление группы из группы
     * @param parent
     * @param child
     */
    void remoteGroupFromGroup(Id parent, Id child);

    /**
     * Получение динамической группу
     * @param name
     *            имя динамической группы
     * @param contectId
     *            идентификатор контекста динамической группы
     * @return
     */
    DomainObject findDynamicGroup(String name, Id contectId);

    /**
     * Метод возвращает альтернативый login с указанным типом, по другому альтернативному логину с указанным типом
     * @param login - известный логин
     * @param alterUidType - тип известного логина
     * @param desUidType - тип искомого логина
     * @return - искомые логины
     */
    @NotNull
    List<String> getPersonAltUids(@NotNull String login, @NotNull String alterUidType, @NotNull String desUidType);
}
