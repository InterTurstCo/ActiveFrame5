package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.GlobalCacheManager;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.impl.utils.IdentifiableObjectConverter;

import java.rmi.ServerException;
import java.util.*;

/**
 * Реализация сервиса вхождения управления пользователями и группами
 *
 * @author larin
 *
 */
public class PersonManagementServiceDaoImpl implements PersonManagementServiceDao {
    final static Logger logger = LoggerFactory.getLogger(PersonManagementServiceDaoImpl.class);

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private IdentifiableObjectConverter identifiableObjectConverter;

    @Autowired
    private GlobalCacheManager globalCacheManager;

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    /**
     * Получение персон входящих непосредственно в группу
     */
    @Override
    public List<DomainObject> getPersonsInGroup(Id groupId) {
        AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");

        Filter filter = new Filter();
        filter.setFilter("byGroup");
        filter.addCriterion(0, new ReferenceValue(groupId));

        return identifiableObjectConverter.convertToDomainObjectList(collectionsDao.findCollection("PersonInGroup",
                Collections.singletonList(filter), null, 0, 0, accessToken));
    }

    /**
     * Получение всех персон входящих в группу с учетом наследования
     */
    @Override
    public List<DomainObject> getAllPersonsInGroup(Id groupId) {
        AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");

        Filter filter = new Filter();
        filter.setFilter("byGroup");
        filter.addCriterion(0, new ReferenceValue(groupId));

        return identifiableObjectConverter.convertToDomainObjectList(collectionsDao.findCollection("AllPersonInGroup",
                Collections.singletonList(filter), null, 0, 0, accessToken));
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
        AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");

        Filter filter = new Filter();
        filter.setFilter("byPerson");
        filter.addCriterion(0, new ReferenceValue(personId));

        return identifiableObjectConverter.convertToDomainObjectList(collectionsDao.findCollection("PersonGroups",
                Collections.singletonList(filter), null, 0, 0, accessToken));
    }

    /**
     * Проверка вхождения одной группы в другую. Проверка возможна как с учетом
     * наследования так и без учета наследования, в зависимости от параметра
     * recursive
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
     *
     *
     * @param parent
     *            Идентификатор родительской группы
     * @return
     */
    @Override
    public List<DomainObject> getAllParentGroup(Id parent) {
        AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");

        Filter filter = new Filter();
        filter.setFilter("allParentGroups");
        filter.addCriterion(0, new ReferenceValue(parent));

        return identifiableObjectConverter.convertToDomainObjectList(collectionsDao.findCollection("DynamicGroups",
                Collections.singletonList(filter), null, 0, 0, accessToken));
    }

    @Override
    public List<DomainObject> getChildGroups(Id parent) {
        AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");
        if (globalCacheManager.isEnabled()) {
            final List<DomainObject> groupGroupSettings =
                    domainObjectDao.findLinkedDomainObjects(parent, "group_group_settings", "parent_group_id", accessToken);
            if (groupGroupSettings == null || groupGroupSettings.isEmpty()) {
                return Collections.emptyList();
            }
            ArrayList<Id> childGroupIds = new ArrayList<>(groupGroupSettings.size());
            for (DomainObject groupGroupSetting : groupGroupSettings) {
                final Id childGroupId = groupGroupSetting.getReference("child_group_id");
                if (childGroupId != null) {
                    childGroupIds.add(childGroupId);
                }
            }
            return domainObjectDao.find(childGroupIds, accessToken);
        } else {
            Filter filter = new Filter();
            filter.setFilter("byParent");
            filter.addCriterion(0, new ReferenceValue(parent));

            return identifiableObjectConverter.convertToDomainObjectList(collectionsDao.findCollection("ChildGroups",
                    Collections.singletonList(filter), null, 0, 0, accessToken));
        }
    }

    @Override
    public List<DomainObject> getAllChildGroups(Id parent) {
        AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");

        Filter filter = new Filter();
        filter.setFilter("allChildGroups");
        filter.addCriterion(0, new ReferenceValue(parent));

        return identifiableObjectConverter.convertToDomainObjectList(collectionsDao.findCollection("DynamicGroups",
                Collections.singletonList(filter), null, 0, 0, accessToken));
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
    public void addGroupsToGroup(Id parent, Collection<Id> children) {
        ArrayList<DomainObject> groupGroupSettings = new ArrayList<>(children.size());
        for (Id child : children) {
            DomainObject groupGroup = createDomainObject("group_group_settings");
            groupGroup.setReference("parent_group_id", parent);
            groupGroup.setReference("child_group_id", child);
            groupGroupSettings.add(groupGroup);
        }
        AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");
        domainObjectDao.save(groupGroupSettings, accessToken);
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

        for (IdentifiableObject person : persons) {
            remotePersonFromGroup(groupId, person.getId());
        }

        List<DomainObject> groups = getChildGroups(groupId);

        for (IdentifiableObject group : groups) {
            remoteGroupFromGroup(groupId, group.getId());
        }
    }

    @Override
    public DomainObject findDynamicGroup(String name, Id connectId) {
        AccessToken accessToken = accessControlService.createSystemAccessToken("PersonManagementService");

        Filter filter = new Filter();
        filter.setFilter("byObjectIdAndGroupName");
        filter.addCriterion(0, new ReferenceValue(connectId));
        filter.addCriterion(1, new StringValue(name));

        IdentifiableObjectCollection result = collectionsDao.findCollection("DynamicGroups",
                Collections.singletonList(filter), null, 0, 0, accessToken);

        if (result == null || result.size() == 0) {
            return null;
        }

        return identifiableObjectConverter.convertToDomainObject(result.get(0));
    }

    @Override
    public Set<Id> getAllRootGroup() {
        AccessToken accessToken = accessControlService.createSystemAccessToken(PersonManagementServiceDaoImpl.class.getName());
        // Получение всех групп, у которых есть в составе другие группы, но они не входят в состав ни одной группы
        // Иными словами получаем верхушки иерархий всех групп в системе
        String query = "select id from user_group ";
        query += "where not exists (select 1 from group_group_settings s where s.child_group_id = user_group.id) ";
        query += "and exists(select 1 from group_group_settings s where s.parent_group_id = user_group.id)";

        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 0, accessToken);
        Set<Id> result = new HashSet<Id>();
        //Для каждой группы верхнего уровня вызываем пересчет состава всех ее дочерних групп
        for (IdentifiableObject collectionRow : collection) {
            result.add(collectionRow.getId());
        }
        return result;
    }

    private Set<Id> recalcGroupGroupForGroupAndChildGroups(Id groupId, boolean hierarchy, AccessToken accessToken) {
        // Получаем состав группы из конфигурации
        HashMap<Id, Set<Id>> groupsMembers = new HashMap<Id, Set<Id>>();
        getAllChildGroupsIdByConfig(groupsMembers, groupId, accessToken);
        //В результате groupsMembers содержит информацию о составе переданной группы и всех дочерних групп, с учетом иерархии

        Set<Id> result = new HashSet<Id>();
        if (hierarchy) {
            //Если в параметре передано пересчитывать все дочерние группы
            //Для каждой группы из иерархии выполняем получение ее состава в базе и при необходимости корректируем состав 
            for (Id recalGroupId : groupsMembers.keySet()) {
                Set<Id> childGroups = groupsMembers.get(recalGroupId);
                correctGroupMembers(recalGroupId, childGroups, accessToken);
                result.add(recalGroupId);
            }
        } else {
            //Пересчитываем состав в базе для единственной группы
            Set<Id> childGroups = groupsMembers.get(groupId);
            correctGroupMembers(groupId, childGroups, accessToken);
            result.add(groupId);
        }
        return result;
    }

    private void correctGroupMembers(Id groupId, Set<Id> childGroups, AccessToken accessToken) {
        logger.debug("Correct group {}", groupId);
        // Получаем состав группы из базы
        String query = "select gg.child_group_id from group_group gg where gg.parent_group_id = {0} and gg.child_group_id != {0}";
        List<Value> params = new ArrayList<Value>();
        params.add(new ReferenceValue(groupId));
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, params, 0, 0, accessToken);
        Set<Id> childGroupsBase = getIds(collection);

        // Сравниваем две коллекции
        // Получаем список на добавление
        List<Id> addList = new ArrayList<Id>();
        for (Id group : childGroups) {
            if (!childGroupsBase.contains(group)) {
                addList.add(group);
            }
        }
        // Получаем список на удаление
        List<Id> delList = new ArrayList<Id>();
        for (Id group : childGroupsBase) {
            if (!childGroups.contains(group)) {
                delList.add(group);
            }
        }

        // Непосредственно добавляем элементы
        ArrayList<DomainObject> groupGroups = new ArrayList<>(addList.size());
        for (Id group : addList) {
            DomainObject domainObject = createDomainObject("group_group");
            domainObject.setReference("parent_group_id", groupId);
            domainObject.setReference("child_group_id", group);
            groupGroups.add(domainObject);
            logger.debug("To group {} add member {}", groupId, group);
        }
        domainObjectDao.save(groupGroups, accessToken);

        // Непосредственно удаляем элементы
        ArrayList<Id> toDelete = new ArrayList<>(delList.size());
        for (Id childGroup : delList) {
            Id groupGroupId = getGroupGroupId(groupId, childGroup, accessToken);
            toDelete.add(groupGroupId);
            logger.debug("From group {} delete member {}", groupId, childGroup);
        }
        domainObjectDao.delete(toDelete, accessToken);
    }

    /**
     * Рекурсивное получение настройки вхождения группы в группу из таблицы
     * group_group_settings
     * 
     * @param parentRoleId
     * @return
     * @throws ServerException
     */
    private void getAllChildGroupsIdByConfig(HashMap<Id, Set<Id>> groupsMembers, Id parent, AccessToken accessToken) {
        HashSet<Id> allGroups = new HashSet<>();
        //Непосредственные вхождения
        String query = "select child_group_id from group_group_settings where parent_group_id = {0}";
        List<Value> params = new ArrayList<Value>();
        params.add(new ReferenceValue(parent));
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, params, 0, 0, accessToken);

        Set<Id> childGroups = getIds(collection);
        allGroups.addAll(childGroups);
        //В цикле собираем информацию о составе дочерних групп
        for (Id childGroup : childGroups) {
            //Получаем состав дочерних групп
            getAllChildGroupsIdByConfig(groupsMembers, childGroup, accessToken);
            //Добавляем состав дочерних групп в основную группу
            allGroups.addAll(groupsMembers.get(childGroup));
        }
        groupsMembers.put(parent, allGroups);
    }

    /**
     * Получение списка идентификаторов элементов коллекции
     * @param domainObjects
     * @return
     */
    private Set<Id> getIds(IdentifiableObjectCollection collection) {
        if (collection == null || collection.size() == 0) {
            return Collections.emptySet();
        }
        Set<Id> result = new HashSet<>(collection.size());
        for (IdentifiableObject row : collection) {
            result.add(row.getId());
        }
        return result;
    }

    /**
     * Получение идентификаторов доменных объектов вхождения группы в группу по
     * идентификатору родительской и дочерней группы
     * @param parent
     * @param child
     * @return
     */
    private Id getGroupGroupId(Id parent, Id child, AccessToken accessToken) {
        String query = "select g.id from group_group g ";
        query += "where g.parent_group_id = {0} and g.child_group_id = {1}";

        List<Value> params = new ArrayList<Value>();
        params.add(new ReferenceValue(parent));
        params.add(new ReferenceValue(child));

        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, params, 0, 0, accessToken);
        Id result = null;

        if (collection.size() > 0) {
            result = collection.get(0).getId();
        }
        return result;
    }

    @Override
    public Set<Id> recalcGroupGroupForGroupAndChildGroups(Id groupId) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(PersonManagementServiceDaoImpl.class.getName());
        return recalcGroupGroupForGroupAndChildGroups(groupId, true, accessToken);
    }

    
    @Override
    public void recalcGroupGroup(Id groupId) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(PersonManagementServiceDaoImpl.class.getName());
        recalcGroupGroupForGroupAndChildGroups(groupId, false, accessToken);
    }
}
