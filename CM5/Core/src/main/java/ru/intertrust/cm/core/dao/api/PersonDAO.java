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
     * Проверяет сужествует ли пользователь с указанным логином.
     * @param login логин пользователя
     * @return true, если существует пользователь, false иначе
     */
    boolean existsPerson(String login);

}
