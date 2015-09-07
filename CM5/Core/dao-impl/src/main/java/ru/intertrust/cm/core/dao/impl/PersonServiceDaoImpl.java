package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;

import java.util.HashMap;
import java.util.Map;

public class PersonServiceDaoImpl implements PersonServiceDao {

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;


    @Override
    @Cacheable(value="persons", key = "#login")
    public DomainObject findPersonByLogin(String login) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        Map<String, Value> uniqueKeyValuesByName = new HashMap<>();
        uniqueKeyValuesByName.put("login", new StringValue(login));

        DomainObject person = domainObjectDao.findByUniqueKey(GenericDomainObject.PERSON_DOMAIN_OBJECT, uniqueKeyValuesByName, accessToken);

        if (person == null) {
            throw new IllegalArgumentException("Person not found: " + login);
        }

        return person;
    }

    @Override
    @CacheEvict(value = "persons", key = "#login")
    public void personUpdated(String login) {
        // метод пустой, т.к. удаление объекта из кеша происходит в Spring
    }


}
