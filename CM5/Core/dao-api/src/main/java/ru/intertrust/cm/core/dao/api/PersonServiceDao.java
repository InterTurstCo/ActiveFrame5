package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.List;

/**
 * Сервис для работы персонами
 */
public interface PersonServiceDao {

    /**
     * поиск доменного объекта персоны по логину
     * Доменный объект Person кешируется
     * @param login логин
     * @return доменной объект персоны соответствующий логину
     */
    DomainObject findPersonByLogin(String login);

    /**
     * обновление кеша персон при изменении/удалении пользователя
     * @param login
     */
    void personUpdated(String login);

}
