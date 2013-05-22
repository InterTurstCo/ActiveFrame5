package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.Person;

/**
 * Сервис для работы с бизнес-объектом Person.
 * 
 * @author atsvetkov
 * 
 */
public interface PersonService {

    /**
     * Добавление пользователя.
     * @param person {@link Person} пользователь для сохранения
     * @return
     */
    void insertPerson(Person person);

    /**
     * Проверяет сужествует ли пользователь с указанным логином.
     * @param login логин пользователя
     * @return true, если существует пользователь, false иначе
     */
    boolean existsPerson(String login);

}
