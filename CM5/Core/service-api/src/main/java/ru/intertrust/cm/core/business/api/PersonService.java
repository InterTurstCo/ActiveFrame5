package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Сервис для работы персонами
 */
public interface PersonService {

    public interface Remote extends PersonService {
    }

    /**
     * поиск доменного объекта персоны по логину
     * @param login логин
     * @return доменной объект персоны соответствующий логину
     */
    DomainObject findPersonByLogin(String login);

    /**
     * Возвращает доменный объект персоны, осуществляющей операцию
     * @return доменный объект персоны, осуществляющей операцию
     */
    DomainObject getCurrentPerson();

    /**
     * Возвращает уникальный идентификатор персоны, осуществляющей операцию
     * @return уникальный идентификатор персоны, осуществляющей операцию
     */
    String getCurrentPersonUid();

    /**
     * Получение персоны по альтернативному идентификатору
     * @param alterUid
     * @param alterUidType
     * @return
     */
    DomainObject findPersonByAltUid(String alterUid, String alterUidType);
}
