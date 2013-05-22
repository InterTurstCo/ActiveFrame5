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
     * Поиск пользователя по логину.
     * @param login логин пользователя
     * @return объект {@link Person}, если пользователь существует, иначе возвращает null.
     */
    Person findPersonByLogin(String login);

}
