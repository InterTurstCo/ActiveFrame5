package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.business.api.MD5Service;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.Person;
import ru.intertrust.cm.core.dao.api.PersonDAO;

/**
 * Реализация сервиса для работы с бизнес-объектом Person
 * @author atsvetkov
 * 
 */
public class PersonServiceImpl implements PersonService {

    private MD5Service md5Service;

    private PersonDAO personDAO;

    /**
     * Добавляет пользователя в базу. Кодирует пароль, использую MD5 алгоритм. В базу сохраняется MD5 хеш значение
     * пароля.
     * @param person {@link Person}
     */
    @Override
    public void insertPerson(Person person) {
        String enteredPassword = (String) person.getConfiguredFields().get("password");
        String passwordHash = md5Service.getMD5(enteredPassword);
        person.getConfiguredFields().put("password", passwordHash);

        personDAO.insertPerson(person);
    }

    /**
     * Поиск пользователя по логину
     * @param login логин пользователя
     * @return {@link Person}
     */
    public Person findPersonByLogin(String login) {
        return personDAO.findPersonByLogin(login);
    }

    /**
     * Устанавливает {@see #md5Service}. Используется для кодирования паролей пользователей.
     * 
     * @param md5Service
     */
    public void setMd5Service(MD5Service md5Service) {
        this.md5Service = md5Service;
    }

    /**
     * Устанавливает {@see #personDAO}.
     * @param personDAO
     */
    public void setPersonDAO(PersonDAO personDAO) {
        this.personDAO = personDAO;
    }
}
