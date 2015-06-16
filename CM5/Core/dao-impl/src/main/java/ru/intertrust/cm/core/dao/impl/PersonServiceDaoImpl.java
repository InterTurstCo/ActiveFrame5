package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.dao.impl.utils.SingleObjectRowMapper;

import java.util.HashMap;
import java.util.Map;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

public class PersonServiceDaoImpl implements PersonServiceDao {

    @Autowired
    @Qualifier("switchableNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private DomainObjectCacheService domainObjectCacheService;


    @Override
    @Cacheable(value="persons", key = "#login")
    public DomainObject findPersonByLogin(String login) {
        String query = "select p.* from " + wrap(getSqlName(GenericDomainObject.PERSON_DOMAIN_OBJECT)) +
                " p where p." + wrap("login") + "=:login";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("login", login);
        DomainObject person = jdbcTemplate.query(query, paramMap,
                new SingleObjectRowMapper(GenericDomainObject.PERSON_DOMAIN_OBJECT, configurationExplorer,
                        domainObjectTypeIdCache));
        if (person == null) {
            throw new IllegalArgumentException("Person not found: " + login);
        } else {
            domainObjectCacheService.putOnRead(person);
        }
        return person;
    }

    @Override
    @CacheEvict(value = "persons", key = "#login")
    public void personUpdated(String login) {
        // метод пустой, т.к. удаление объекта из кеша происходит в Spring
    }


}
