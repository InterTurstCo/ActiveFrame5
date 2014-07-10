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

    DomainObject getCurrentPerson();
}
