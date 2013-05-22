package ru.intertrust.cm.core.dao.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.Person;
import ru.intertrust.cm.core.dao.api.PersonDAO;

/**
 * Реализация DAO для работы с бизнес-объектом Person.
 * @author atsvetkov
 *
 */
public class PersonDAOImpl implements PersonDAO {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public int insertPerson(Person person) {
        String fieldNames = createFieldList(person);
        String parameterNames = createParameterList(person, true);
        String query = "insert into person (" + fieldNames + ") values (" + parameterNames + ")";
        return jdbcTemplate.update(query, person.getConfiguredFields());
    }

    @Override
    public Person findPersonByLogin(String login) {
        // TODO Определиться с форматом DTO для Персоны. Настраивается в конфигурации или отдельный POJO класс.
        Person person = null;
        String query = "select * from person p where p.login=:login";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("login", login);
        Map<String, Object> results = jdbcTemplate.queryForMap(query, paramMap);
        if (!results.isEmpty()) {
            person = new Person();
            person.setConfiguredFields(results);
        }
        return person;
    }

    private String createFieldList(Person person) {
        return createParameterList(person, false);
    }

    private String createParameterList(Person person, boolean isParameters) {
        StringBuffer fieldNames = new StringBuffer();

        Iterator<String> fieldsIterator = person.getConfiguredFields().keySet().iterator();

        while (fieldsIterator.hasNext()) {
            String field = (String) fieldsIterator.next();
            if (isParameters) {
                fieldNames.append(":");
            }
            fieldNames.append(field);
            if (fieldsIterator.hasNext()) {
                fieldNames.append(", ");
            }
        }
        return fieldNames.toString();
    }

}
