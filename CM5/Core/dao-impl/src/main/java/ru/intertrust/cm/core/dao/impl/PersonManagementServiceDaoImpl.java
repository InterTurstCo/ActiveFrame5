package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.*;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService.COLLECTION_CACHE_CATEGORY;
import ru.intertrust.cm.core.dao.impl.utils.MultipleObjectRowMapper;
import ru.intertrust.cm.core.dao.impl.utils.SingleObjectRowMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private EventLogService eventLogService;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private PersonManagementQueryHelper personManagementQueryHelper;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    @Qualifier("switchableNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    private DomainObjectCacheService domainObjectCacheService;

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }


    /**
     * Получение персон входящих непосредственно в группу
     */
    @Override
    public List<DomainObject> getPersonsInGroup(Id groupId) {
        List<DomainObject> personsInGroup =
                domainObjectCacheService.getCollection(groupId, COLLECTION_CACHE_CATEGORY.PERSON_IN_GROUP.toString());
        if (personsInGroup != null) {
            return personsInGroup;
        } else {
            String typeName = "Person";
            AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");
            String query = personManagementQueryHelper.generateFindPersonsInGroupQuery(typeName, accessToken);

            personsInGroup = findMultipleDomainObjects(query, typeName, groupId, accessToken);
            domainObjectCacheService.putCollectionOnRead(groupId, personsInGroup, COLLECTION_CACHE_CATEGORY.PERSON_IN_GROUP.toString());
            return personsInGroup;
        }
    }

    /**
     * Получение всех персон входящих в группу с учетом наследования
     */
    @Override
    public List<DomainObject> getAllPersonsInGroup(Id groupId) {
        List<DomainObject> personsInGroup =
                domainObjectCacheService.getCollection(groupId, COLLECTION_CACHE_CATEGORY.PERSON_IN_GROUP_AND_SUBGROUP.toString());
        if (personsInGroup != null) {
            return personsInGroup;
        } else {
            String typeName = "Person";
            AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");
            String query = personManagementQueryHelper.generateFindAllPersonsInGroupQuery(typeName, accessToken);

            personsInGroup = findMultipleDomainObjects(query, typeName, groupId, accessToken);
            domainObjectCacheService.putCollectionOnRead(groupId, personsInGroup, COLLECTION_CACHE_CATEGORY.PERSON_IN_GROUP_AND_SUBGROUP.toString());
            return personsInGroup;
        }
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

        List<DomainObject> personGroups = domainObjectCacheService.getCollection(personId, COLLECTION_CACHE_CATEGORY.GROUP_FOR_PERSON.toString());
        if (personGroups != null) {
            return personGroups;
        } else {
            String typeName = "User_Group";
            AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");
            String query = personManagementQueryHelper.generateFindPersonGroups(typeName, accessToken);
            personGroups = findMultipleDomainObjects(query, typeName, personId, accessToken);
            domainObjectCacheService.putCollectionOnRead(personId, personGroups, COLLECTION_CACHE_CATEGORY.GROUP_FOR_PERSON.toString());
            return personGroups;
        }
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
        List<DomainObject> personGroups = domainObjectCacheService.getCollection(parent, COLLECTION_CACHE_CATEGORY.ALL_PARENT_GROUPS.toString());
        if (personGroups != null) {
            return personGroups;
        } else {
            String typeName = "User_Group";
            AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");
            String query = personManagementQueryHelper.generateFindAllParentGroups(typeName, accessToken);
            personGroups = findMultipleDomainObjects(query, typeName, parent, accessToken);
            domainObjectCacheService.putCollectionOnRead(parent, personGroups, COLLECTION_CACHE_CATEGORY.ALL_PARENT_GROUPS.toString());
            return personGroups;
        }
    }

    @Override
    public List<DomainObject> getChildGroups(Id parent) {
        List<DomainObject> personGroups = domainObjectCacheService.getCollection(parent, COLLECTION_CACHE_CATEGORY.CHILD_GROUPS.toString());
        if (personGroups != null) {
            return personGroups;
        } else {
            String typeName = "User_Group";
            AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");
            String query = personManagementQueryHelper.generateFindChildGroups(typeName, accessToken);
            personGroups = findMultipleDomainObjects(query, typeName, parent, accessToken);
            domainObjectCacheService.putCollectionOnRead(parent, personGroups, COLLECTION_CACHE_CATEGORY.CHILD_GROUPS.toString());
            return personGroups;
        }
    }

    @Override
    public List<DomainObject> getAllChildGroups(Id parent) {
        List<DomainObject> personGroups = domainObjectCacheService.getCollection(parent, COLLECTION_CACHE_CATEGORY.ALL_CHILD_GROUPS.toString());
        if (personGroups != null) {
            return personGroups;
        } else {
            String typeName = "User_Group";
            AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");
            String query = personManagementQueryHelper.generateFindAllChildGroups(typeName, accessToken);
            personGroups = findMultipleDomainObjects(query, typeName, parent, accessToken);

            domainObjectCacheService.putCollectionOnRead(parent, personGroups, COLLECTION_CACHE_CATEGORY.ALL_CHILD_GROUPS.toString());
            return personGroups;
        }
    }

    @Override
    public void addPersonToGroup(Id group, Id person) {
        DomainObject groupMembers = createDomainObject("Group_Member");
        groupMembers.setReference("person_id", person);
        groupMembers.setReference("UserGroup", group);
        AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");
        domainObjectDao.save(groupMembers, accessToken);
    }

    @Override
    public void addGroupToGroup(Id parent, Id child) {
        DomainObject groupGroup = createDomainObject("group_group_settings");
        groupGroup.setReference("parent_group_id", parent);
        groupGroup.setReference("child_group_id", child);
        AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");
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
        String typeName = "User_Group";
        AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");

        String query = personManagementQueryHelper.generateFindDynamicGroup(typeName, accessToken);

        Map<String, Object> parameters = personManagementQueryHelper.initializeParameters(contectId, accessToken);
        parameters.put("name", name);

        DomainObject result = jdbcTemplate.query(query, parameters,
                new SingleObjectRowMapper(typeName, configurationExplorer, domainObjectTypeIdCache));

        if (result == null) {
            return null;
        }

        domainObjectCacheService.putOnRead(result, accessToken);

        eventLogService.logAccessDomainObjectEvent(result.getId(), EventLogService.ACCESS_OBJECT_READ, true);

        return result;
    }

    private List<DomainObject> findMultipleDomainObjects(String query, String typeName, Id id, AccessToken accessToken) {
        Map<String, Object> parameters = personManagementQueryHelper.initializeParameters(id, accessToken);

        List<DomainObject> result = jdbcTemplate.query(query, parameters,
                new MultipleObjectRowMapper(typeName, configurationExplorer, domainObjectTypeIdCache));

        domainObjectCacheService.putAllOnRead(result, accessToken);

        eventLogService.logAccessDomainObjectEventByDo(result, EventLogService.ACCESS_OBJECT_READ, true);

        return result;
    }

}