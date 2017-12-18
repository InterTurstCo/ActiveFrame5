package ru.intertrust.cm.core.dao.impl.personmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.model.ExtensionPointException;

import java.rmi.ServerException;
import java.util.*;

/**
 * Обработчик точки расширения сохранения конфигурации вхождения группы в
 * группу. Производит разворачивание вхождение группу в группу с учетом иерархии
 * @author larin
 * 
 */
@ExtensionPoint(filter = "group_group_settings")
public class OnChangeGroupGroupSettingsExtensionPointHandler implements AfterSaveExtensionHandler, AfterDeleteExtensionHandler {
    final static Logger logger = LoggerFactory.getLogger(OnChangeGroupGroupSettingsExtensionPointHandler.class);    

    @Autowired
    private PersonManagementServiceDao personManagementService;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private DomainObjectCacheService domainObjectCacheService;

    @org.springframework.beans.factory.annotation.Value("${disable.group.uncover:false}")
    private boolean disableGroupUncover;

    /**
     * Входная точка точки расширения. Вызывается когда сохраняется доменный
     * обьект group_group_settings
     */
    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        if (!disableGroupUncover) {
            clearCollectionCache();

            Id parent = domainObject.getReference("parent_group_id");
            Id child = domainObject.getReference("child_group_id");
            // Проверка на зацикливание
            if (personManagementService.isGroupInGroup(child, parent, true)) {
                throw new ExtensionPointException("Found cycle in groups. Group " + child + " exists role " + parent);
            }

            // Получаем группы, которые включают родительскую группу с учетом иерархии
            List<DomainObject> roles = personManagementService.getAllParentGroup(parent);

            // Вызываем пересчет состава ролей всех этих групп и самой изменяемой группы
            refreshGroupGroups(parent);
            for (DomainObject role : roles) {
                refreshGroupGroups(role.getId());
            }
        }else{
            logger.warn("Group uncover is disabled");
        }
    }

    private void clearCollectionCache() {
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.CHILD_GROUPS.name());
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.ALL_CHILD_GROUPS.name());
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.ALL_PARENT_GROUPS.name());
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.PERSON_IN_GROUP.name());
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.PERSON_IN_GROUP_AND_SUBGROUP.name());
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.GROUP_FOR_PERSON.name());

    }

    private void refreshGroupGroups(Id parent) {
        // Получаем состав группы из конфигурации
        Set<Id> childGroups = getAllChildGroupsIdByConfig(parent);
        // Получаем состав роли из базы
        Set<Id> childGroupsBase = getIds(personManagementService.getAllChildGroups(parent));
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
        AccessToken accessToken = accessControlService.createSystemAccessToken("OnChangeGroupGroupSettingsExtensionPointHandler");

        // Непосредственно добавляем элементы
        ArrayList<DomainObject> groupGroups = new ArrayList<>(addList.size());
        for (Id group : addList) {
            DomainObject domainObject = createDomainObject("group_group");
            domainObject.setReference("parent_group_id", parent);
            domainObject.setReference("child_group_id", group);
            groupGroups.add(domainObject);
        }
        domainObjectDao.save(groupGroups, accessToken);
        // Непосредственно удаляем элементы

        ArrayList<Id> toDelete = new ArrayList<>(delList.size());
        for (Id group : delList) {
            Id groupGroupId = getGroupGroupId(parent, group);
            toDelete.add(groupGroupId);
        }
        domainObjectDao.delete(toDelete, accessToken);
    }

    /**
     * Рекурсивное получение настройки вхождения роль в роль из таблицы
     * group_group_settings
     * 
     * @param parentRoleId
     * @return
     * @throws ServerException
     */
    private Set<Id> getAllChildGroupsIdByConfig(Id parent) {
        HashSet<Id> allGroups = new HashSet<>();
        Set<Id> childGroups = getIds(personManagementService.getChildGroups(parent));
        for (Id group : childGroups) {
            allGroups.add(group);
            Set<Id> childGroupsHierarchy = getAllChildGroupsIdByConfig(group);
            for (Id childGroup : childGroupsHierarchy) {
                allGroups.add(childGroup);
            }
        }
        return allGroups;
    }

    /**
     * Получение списка идентификаторов переданного списка доменных объектов
     * @param domainObjects
     * @return
     */
    private Set<Id> getIds(List<DomainObject> domainObjects) {
        if (domainObjects == null || domainObjects.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Id> result = new HashSet<>(domainObjects.size());
        for (DomainObject domainObject : domainObjects) {
            result.add(domainObject.getId());
        }
        return result;
    }

    /**
     * Создание нового доменного обьекта переданного типа
     * 
     * @param type
     * @return
     */
    private DomainObject createDomainObject(String type) {
        GenericDomainObject taskDomainObject = new GenericDomainObject();
        taskDomainObject.setTypeName(type);
        Date currentDate = new Date();
        taskDomainObject.setCreatedDate(currentDate);
        taskDomainObject.setModifiedDate(currentDate);
        return taskDomainObject;
    }

    /**
     * Получение идентификаторов доменных объектов вхождения группы в группу по
     * идентификатору родительской и дочерней группы
     * @param parent
     * @param child
     * @return
     */
    private Id getGroupGroupId(Id parent, Id child) {
        Filter filter = new Filter();
        filter.setFilter("byMember");
        ReferenceValue rv = new ReferenceValue(parent);
        filter.addCriterion(0, rv);
        rv = new ReferenceValue(child);
        filter.addCriterion(1, rv);
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);

        AccessToken accessToken = accessControlService
                .createSystemAccessToken("OnSavePersonExtension");

        IdentifiableObjectCollection collection = collectionsDao
                .findCollection("GroupGroup", filters, null, 0, 0,
                        accessToken);
        Id result = null;

        if (collection.size() > 0) {
            result = collection.get(0).getId();
        }
        return result;
    }

    @Override
    public void onAfterDelete(DomainObject domainObject) {
        clearCollectionCache();

        Id parent = domainObject.getReference("parent_group_id");

        // Получаем роли, которые включают родительскую роль с учетом иерархии
        List<DomainObject> roles = personManagementService.getAllParentGroup(parent);
        // Вызываем пересчет состава ролей всех этих ролей и самой изменяемой роли
        refreshGroupGroups(parent);
        for (DomainObject role : roles) {
            refreshGroupGroups(role.getId());
        }
    }
}
