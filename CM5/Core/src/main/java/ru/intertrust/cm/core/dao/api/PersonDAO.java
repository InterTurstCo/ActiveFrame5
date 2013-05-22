package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.Person;

/**
 * DAO для работы с бизнес-объектом Person.
 * @author atsvetkov
 *
 */
public interface PersonDAO {

    /**
     * Добавление пользователя в базу данных
     * @param person {@link Person}
     * @return
     */
    int insertPerson(Person person);

    /**
     * Поиск пользователя по логину.
     * @param login логин пользователя
     * @return {@link Person}
     */
    Person findPersonByLogin(String login);

}
