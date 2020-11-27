package ru.intertrust.cm.core.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.PersonAltUid;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;

import javax.validation.constraints.NotNull;

public class PersonServiceDaoImpl implements PersonServiceDao {

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private AccessControlService accessControlService;


    @Override
    @Cacheable(value="persons", key = "#login")
    public DomainObject findPersonByLogin(String login) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue(login.toLowerCase()));

        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery("select id from person where lower(login) = {0}", params, 0, 0, accessToken);
        if (collection.size() == 0) {
            throw new IllegalArgumentException("Person not found: " + login);
        }
        
        DomainObject person = domainObjectDao.find(collection.get(0).getId(), accessToken);
        return person;
    }

    @Override
    @Cacheable(value="persons", key = "#unid")
    public DomainObject findPersonByUnid(String unid) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue(unid.toLowerCase()));

        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery("select id from person where lower(unid) = {0}", params, 0, 0, accessToken);
        if (collection.size() == 0) {
            return null;
        }

        DomainObject person = domainObjectDao.find(collection.get(0).getId(), accessToken);
        return person;
    }

    @Override
    @CacheEvict(value = "persons", key = "#login")
    public void personUpdated(String login) {
        // метод пустой, т.к. удаление объекта из кеша происходит в Spring
    }

    @Override
    public List<PersonAltUid> getPersonAltUids(String login) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        DomainObject personDomainObject = findPersonByLogin(login);
        List<DomainObject> personAltUids = domainObjectDao.findLinkedDomainObjects(personDomainObject.getId(), "person_alt_uids", "person", accessToken);
        List<PersonAltUid> result = new ArrayList<PersonAltUid>();
        for (DomainObject domainObject : personAltUids) {
            result.add(new PersonAltUid(domainObject.getString("alter_uid"), domainObject.getString("alter_uid_type")));
        }
        return result;
    }

    @Override
    public List<String> getPersonAltUids(String login, String alterUidType) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        String query = "select a.id, alter_uid from person_alt_uids a ";
        query += "join person p on p.id = a.person ";
        query += "where lower(p.login) = {0} and a.alter_uid_type = {1}";
        
        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue(login.toLowerCase()));
        params.add(new StringValue(alterUidType));
        
        
        List<String> result = new ArrayList<String>();
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, params, 0, 0, accessToken);
        for (IdentifiableObject row : collection) {
            result.add(row.getString("alter_uid"));
        }
        
        return result;
    }

    @Override
    public DomainObject findPersonByAltUid(String alterUid, String alterUidType) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        String query = "select person from person_alt_uids ";
        query += "where lower(alter_uid) = {0} and alter_uid_type = {1}";
        
        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue(alterUid.toLowerCase()));
        params.add(new StringValue(alterUidType));        
        
        DomainObject result = null;
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, params, 0, 0, accessToken);
        if (collection.size() > 0) {
            result = domainObjectDao.find(collection.get(0).getId(), accessToken);
        }
        
        return result;
    }

    @Override
    @NotNull public List<String> getPersonAltUids(@NotNull String login, @NotNull String alterUidType, @NotNull String desUidType) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        String query = "select b.alter_uid from person_alt_uids a " +
                "join person_alt_uids b on a.person = b.person " +
                "where lower(a.alter_uid) = {0} " +
                "and lower(a.alter_uid_type) = {1} " +
                "and lower(b.alter_uid_type) = {2}";

        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue(login.toLowerCase()));
        params.add(new StringValue(alterUidType.toLowerCase()));
        params.add(new StringValue(desUidType.toLowerCase()));

        List<String> result = new ArrayList();
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, params, 0, 0, accessToken);
        for (IdentifiableObject row: collection) {
            result.add(row.getString("alter_uid"));
        }

        return result;
    }

    @Override
    @Cacheable(value="persons", key = "#email")
    public List<DomainObject> findPersonsByEmail(String email) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery("select id from person where lower(email) = {0}",
                Collections.singletonList(new StringValue(email.toLowerCase())), 0, 0, accessToken);
        if (collection.size() == 0) {
            return Collections.emptyList();
        }
        final List<DomainObject> persons = new ArrayList<>(collection.size());
        for(IdentifiableObject id : collection) {
            persons.add(domainObjectDao.find(id.getId(), accessToken));
        }
        return persons;
    }
}
