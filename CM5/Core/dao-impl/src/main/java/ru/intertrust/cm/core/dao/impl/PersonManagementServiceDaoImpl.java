package ru.intertrust.cm.core.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;

/**
 * Реализация сервиса вхождения управления пользователями и группами
 * 
 * @author larin
 * 
 */
public class PersonManagementServiceDaoImpl implements PersonManagementServiceDao {

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }


    /**
     * Получение персон входящих непосредственно в группу
     */
    @Override
    public List<DomainObject> getPersonsInGroup(Id groupId) {
        Filter filter = new Filter();
        filter.setFilter("byGroup");
        ReferenceValue rv = new ReferenceValue(groupId);
        filter.addCriterion(0, rv);
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);

        AccessToken accessToken = accessControlService
                .createSystemAccessToken("PersonManagementService");

        IdentifiableObjectCollection collection = collectionsDao
                .findCollection("PersonInGroup", filters, null, 0, 0,
                        accessToken);
        List<DomainObject> result = new ArrayList<DomainObject>();
        for (IdentifiableObject item : collection) {
            DomainObject group = domainObjectDao
                    .find(item.getId(), accessToken);
            result.add(group);
        }
        return result;
    }

    /**
     * Получение всех персон входящих в группу с учетом наследования
     */
    @Override
    public List<DomainObject> getAllPersonsInGroup(Id groupId) {
        Filter filter = new Filter();
        filter.setFilter("byGroup");
        ReferenceValue rv = new ReferenceValue(groupId);
        filter.addCriterion(0, rv);
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);

        AccessToken accessToken = accessControlService
                .createSystemAccessToken("PersonManagementService");

        IdentifiableObjectCollection collection = collectionsDao
                .findCollection("AllPersonInGroup", filters, null, 0, 0,
                        accessToken);
        List<DomainObject> result = new ArrayList<DomainObject>();
        for (IdentifiableObject item : collection) {
            DomainObject group = domainObjectDao
                    .find(item.getId(), accessToken);
            result.add(group);
        }
        return result;
    }

    /**
     * Проверка входит ли персона в группу
     */
    @Override
    public boolean isPersonInGroup(Id groupId, Id personId) {
        Filter filter = new Filter();
        filter.setFilter("byGroupAndPerson");
        ReferenceValue rvGroup = new ReferenceValue(groupId);
        filter.addCriterion(0, rvGroup);
        ReferenceValue rvPerson = new ReferenceValue(personId);
        filter.addCriterion(1, rvPerson);
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);

        AccessToken accessToken = accessControlService
                .createSystemAccessToken("PersonManagementService");

        IdentifiableObjectCollection collection = collectionsDao
                .findCollection("IsPersonInGroup", filters, null, 0, 0,
                        accessToken);
        return collection.size() > 0;
    }

    /**
     * Получение всех групп, куда входит персона
     */
    @Override
    public List<DomainObject> getPersonGroups(Id personId) {
        Filter filter = new Filter();
        filter.setFilter("byPerson");
        ReferenceValue rv = new ReferenceValue(personId);
        filter.addCriterion(0, rv);
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);

        AccessToken accessToken = accessControlService
                .createSystemAccessToken("PersonManagementService");

        IdentifiableObjectCollection collection = collectionsDao
                .findCollection("PersonGroups", filters, null, 0, 0,
                        accessToken);
        List<DomainObject> result = new ArrayList<DomainObject>();
        for (IdentifiableObject item : collection) {
            DomainObject group = domainObjectDao
                    .find(item.getId(), accessToken);
            result.add(group);
        }
        return result;
    }

    /**
     * Проверка вхождения одной группы в другую. Проверка возможна как с учетом наследования так и без учета наследования, в зависимости от параметра recursive
     * @param parent
     *            родительская группа
     * @param child
     *            дочерняя группа
     * @param recursive
     *            флаг рекурсивности проверки
     * @return
     */
    @Override
    public boolean isGroupInGroup(Id parent, Id child, boolean recursive) {
        Filter filter = new Filter();
        filter.setFilter("byMembers");
        ReferenceValue rv = new ReferenceValue(parent);
        filter.addCriterion(0, rv);
        rv = new ReferenceValue(child);
        filter.addCriterion(1, rv);
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);

        /*
         * AccessToken accessToken = accessControlService
         * .createCollectionAccessToken(personIdAsint);
         */
        // TODO пока права не работают работаю от имени админа
        AccessToken accessToken = accessControlService
                .createSystemAccessToken("OnSavePersonExtensionPoint");

        String collectionName = null;
        if (recursive) {
            collectionName = "AllGroupInGroup";
        } else {
            collectionName = "GroupInGroup";
        }

        IdentifiableObjectCollection collection = collectionsDao
                .findCollection(collectionName, filters, null, 0, 0, accessToken);

        return collection.size() > 0;
    }

    /**
     * Получение всех родительских групп для группы. с учетом иерархии
     * @param parent
     *            Идентификатор родительской группы
     * @return
     */
    @Override
    public List<DomainObject> getAllParentGroup(Id parent) {
        Filter filter = new Filter();
        filter.setFilter("byChild");
        ReferenceValue rv = new ReferenceValue(parent);
        filter.addCriterion(0, rv);
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);

        /*
         * AccessToken accessToken = accessControlService
         * .createCollectionAccessToken(personIdAsint);
         */
        // TODO пока права не работают работаю от имени админа
        AccessToken accessToken = accessControlService
                .createSystemAccessToken("PersonManagmentService");

        String collectionName = "AllParentGroups";

        IdentifiableObjectCollection collection = collectionsDao
                .findCollection(collectionName, filters, null, 0, 0, accessToken);
        List<DomainObject> result = new ArrayList<DomainObject>();
        for (IdentifiableObject identifiableObject : collection) {
            // Не добавляем ссылку на саму себя
            if (!parent.equals(identifiableObject.getId())) {
                result.add(domainObjectDao.find(identifiableObject.getId(), accessToken));
            }
        }

        return result;
    }

    @Override
    public List<DomainObject> getChildGroups(Id parent) {
        Filter filter = new Filter();
        filter.setFilter("byParent");
        ReferenceValue rv = new ReferenceValue(parent);
        filter.addCriterion(0, rv);
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);

        /*
         * AccessToken accessToken = accessControlService
         * .createCollectionAccessToken(personIdAsint);
         */
        // TODO пока права не работают работаю от имени админа
        AccessToken accessToken = accessControlService
                .createSystemAccessToken("PersonManagmentService");

        String collectionName = "ChildGroups";

        IdentifiableObjectCollection collection = collectionsDao
                .findCollection(collectionName, filters, null, 0, 0, accessToken);
        List<DomainObject> result = new ArrayList<DomainObject>();
        for (IdentifiableObject identifiableObject : collection) {
            result.add(domainObjectDao.find(identifiableObject.getId(), accessToken));
        }

        return result;
    }

    @Override
    public List<DomainObject> getAllChildGroups(Id parent) {
        Filter filter = new Filter();
        filter.setFilter("byParent");
        ReferenceValue rv = new ReferenceValue(parent);
        filter.addCriterion(0, rv);
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);

        /*
         * AccessToken accessToken = accessControlService
         * .createCollectionAccessToken(personIdAsint);
         */
        // TODO пока права не работают работаю от имени админа
        AccessToken accessToken = accessControlService
                .createSystemAccessToken("PersonManagmentService");

        String collectionName = "AllChildGroups";

        IdentifiableObjectCollection collection = collectionsDao
                .findCollection(collectionName, filters, null, 0, 0, accessToken);
        List<DomainObject> result = new ArrayList<DomainObject>();
        for (IdentifiableObject identifiableObject : collection) {
            // Не добавляем ссылку на саму себя
            if (!parent.equals(identifiableObject.getId())) {
                result.add(domainObjectDao.find(identifiableObject.getId(), accessToken));
            }
        }

        return result;
    }

    @Override
    public void addPersonToGroup(Id group, Id person) {
        DomainObject groupMembers = createDomainObject("Group_Member");
        groupMembers.setReference("person_id", person);
        groupMembers.setReference("UserGroup", group);
        AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagmentService");
        domainObjectDao.save(groupMembers, accessToken);
    }

    @Override
    public void addGroupToGroup(Id parent, Id child) {
        DomainObject groupGroup = createDomainObject("group_group_settings");
        groupGroup.setReference("parent_group_id", parent);
        groupGroup.setReference("child_group_id", child);
        AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagmentService");
        domainObjectDao.save(groupGroup, accessToken);
    }

    @Override
    public void remotePersonFromGroup(Id group, Id person) {
        // Получение записи из group_members
        Filter filter = new Filter();
        filter.setFilter("byMember");
        ReferenceValue rv = new ReferenceValue(group);
        filter.addCriterion(0, rv);
        rv = new ReferenceValue(person);
        filter.addCriterion(1, rv);
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);

        /*
         * AccessToken accessToken = accessControlService
         * .createCollectionAccessToken(personIdAsint);
         */
        // TODO пока права не работают работаю от имени админа
        AccessToken accessToken = accessControlService
                .createSystemAccessToken("PersonManagmentService");

        String collectionName = "PersonGroup";

        // Получение колекции
        IdentifiableObjectCollection collection = collectionsDao
                .findCollection(collectionName, filters, null, 0, 0, accessToken);
        // Удаление в цикле всех записей
        for (IdentifiableObject identifiableObject : collection) {
            domainObjectDao.delete(identifiableObject.getId(), accessToken);
        }
    }

    @Override
    public void remoteGroupFromGroup(Id parent, Id child) {
        // Получение записи из group_members
        Filter filter = new Filter();
        filter.setFilter("byMember");
        ReferenceValue rv = new ReferenceValue(parent);
        filter.addCriterion(0, rv);
        rv = new ReferenceValue(child);
        filter.addCriterion(1, rv);
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);

        /*
         * AccessToken accessToken = accessControlService
         * .createCollectionAccessToken(personIdAsint);
         */
        // TODO пока права не работают работаю от имени админа
        AccessToken accessToken = accessControlService
                .createSystemAccessToken("PersonManagmentService");

        String collectionName = "GroupGroupSettings";

        // Получение колекции
        IdentifiableObjectCollection collection = collectionsDao
                .findCollection(collectionName, filters, null, 0, 0, accessToken);
        // Удаление в цикле всех записей
        for (IdentifiableObject identifiableObject : collection) {
            domainObjectDao.delete(identifiableObject.getId(), accessToken);
        }
    }

    /**
     * Создание нового доменного обьекта переданного типа
     * 
     * @param type
     * @return
     */
    private DomainObject createDomainObject(String type) {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(type);
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);
        return domainObject;
    }

    @Override
    public Id getGroupId(String groupName) {
        Filter filter = new Filter();
        filter.setFilter("byName");
        StringValue sv = new StringValue(groupName);
        filter.addCriterion(0, sv);
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);

        AccessToken accessToken = accessControlService
                .createSystemAccessToken("PersonManagementService");

        IdentifiableObjectCollection collection = collectionsDao
                .findCollection("GroupByName", filters, null, 0, 0,
                        accessToken);
        Id result = null;
        if (collection.size() > 0) {
            IdentifiableObject io = collection.get(0);
            result = io.getId();
        }
        return result;
    }

    @Override
    public void removeGroupMembers(Id groupId) {
        List<DomainObject> persons = getPersonsInGroup(groupId);

        for (DomainObject person : persons) {
            remotePersonFromGroup(groupId, person.getId());
        }

        List<DomainObject> groups = getChildGroups(groupId);

        for (DomainObject group : groups) {
            remoteGroupFromGroup(groupId, group.getId());
        }
    }

    @Override
    public DomainObject findDynamicGroup(String name, Id contectId) {
        AccessToken accessToken = accessControlService
                .createSystemAccessToken("PersonManagementService");
        String query = "select t.id from User_Group t where group_name = '" + name + "' and object_id = '" + ((RdbmsId) contectId).getId() + "'";
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 1000, accessToken);
        DomainObject result = null;
        if (collection.size() > 0) {
            result = domainObjectDao.find(collection.getId(0), accessToken);
        }
        return result;
    }

}
