package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.access.PersonAltUid;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Сервис для работы персонами
 */
public interface PersonServiceDao {

    /**
     * поиск доменного объекта персоны по логину
     * Доменный объект Person кешируется. Если доменный объект не найден формируется исключение.
     * @param login логин
     * @return доменной объект персоны соответствующий логину
     */
    DomainObject findPersonByLogin(String login);

    /**
     * Поиск пользовыателя по UUID
     * @param unid uuid пользователя
     * @return доменной объект персоны
     */
    DomainObject findPersonByUnid(String unid);

    /**
     * обновление кеша персон при изменении/удалении пользователя
     * @param login
     */
    void personUpdated(String login);
    
    /**
     * Получение информации о всех альтернативных имен пользователя
     * @param login
     * @return
     */
    List<PersonAltUid> getPersonAltUids(String login);

    /**
     * Получение всех альтернативных имен определенного типа для пользователя
     * @param login
     * @param alterUidType
     * @return
     */
    List<String> getPersonAltUids(String login, String alterUidType);

    /**
     * Проверка есть ли у пользователя альтернативное имя определенного типа, и получение доменного объекта Person для данного альтернативного имени. 
     * Если персона не найдена возвращает null 
     * @param alterUid
     * @param alterUidType
     * @return
     */
    DomainObject findPersonByAltUid(String alterUid, String alterUidType);

    /**
     * Метод возвращает альтернативый login с указанным типом, по другому альтернативному логину с указанным типом
     * @param login - известный логин
     * @param alterUidType - тип известного логина
     * @param desUidType - тип искомого логина
     * @return - искомые логины
     */
    @NotNull List<String> getPersonAltUids(@NotNull String login, @NotNull String alterUidType, @NotNull String desUidType);


    /**
     * Поиск персоны по основному email-у
     * Если персон не найдено, возвращает пустой лист
     * @param email
     * @return - список найденных персон
     */
    @NotNull
    List<DomainObject> findPersonsByEmail(@NotNull final String email);
}
