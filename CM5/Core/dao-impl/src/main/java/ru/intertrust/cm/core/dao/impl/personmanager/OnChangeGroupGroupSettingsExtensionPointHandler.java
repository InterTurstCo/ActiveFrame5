package ru.intertrust.cm.core.dao.impl.personmanager;

import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.model.ExtensionPointException;

/**
 * Обработчик точки расширения сохранения конфигурации вхождения группы в
 * группу. Производит разворачивание вхождение группу в группу с учетом иерархии
 * @author larin
 * 
 */
@ExtensionPoint(filter = "group_group_settings")
public class OnChangeGroupGroupSettingsExtensionPointHandler implements AfterSaveExtensionHandler, AfterDeleteExtensionHandler {

    @Autowired
    private PersonManagementServiceDao personManagementService;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private AccessControlService accessControlService;

    /**
     * Входная точка точки расширения. Вызывается когда сохраняется доменный
     * обьект group_group_settings
     */
    @Override
    public void onAfterSave(DomainObject domainObject) {
        Id parent = domainObject.getReference("parent_group_id");
        Id child = domainObject.getReference("child_group_id");
        // Проверка на зацикливание
        if (personManagementService.isGroupInGroup(child, parent, true)) {
            throw new ExtensionPointException("Found cycle in groups. Group " + child + " exists role " + parent);
        }

        // Получаем роли, которые включают родительскую роль с учетом иерархии
        List<DomainObject> roles = personManagementService.getAllParentGroup(parent);

        // Вызываем пересчет состава ролей всех этих ролей и самой изменяемой
        // роли
        refreshRoleRoles(parent);
        for (DomainObject role : roles) {
            refreshRoleRoles(role.getId());
        }
    }

    private void refreshRoleRoles(Id parent) {
        // Получаем состав группы из конфигурации
        List<Id> childGroups = getAllChildGroupsIdByConfig(parent);
        // Получаем состав роли из базы
        List<Id> childGroupsBase = getIdList(personManagementService.getAllChildGroups(parent));
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
        for (Id group : addList) {
            DomainObject domainObject = createDomainObject("group_group");
            domainObject.setReference("parent_group_id", parent);
            domainObject.setReference("child_group_id", group);
            AccessToken accessToken = accessControlService.createSystemAccessToken("OnChangeGroupGroupSettingsExtensionPointHandler");
            domainObjectDao.save(domainObject, accessToken);
        }
        // Непосредственно удаляем элементы
        for (Id group : delList) {
            Id groupGroupId = getGroupGroupId(parent, group);
            domainObjectDao.delete(groupGroupId);
        }
    }

    /**
     * Рекурсивное получение настройки вхождения роль в роль из таблицы
     * group_group_settings
     * 
     * @param parentRoleId
     * @return
     * @throws ServerException
     */
    private List<Id> getAllChildGroupsIdByConfig(Id parent) {
        List<Id> allGroups = new ArrayList<Id>();
        List<Id> childGroups = getIdList(personManagementService.getChildGroups(parent));
        for (Id group : childGroups) {
            if (!allGroups.contains(group)) {
                allGroups.add(group);
            }
            List<Id> childGroupsHierarchy = getAllChildGroupsIdByConfig(group);
            for (Id childGroup : childGroupsHierarchy) {
                if (!allGroups.contains(childGroup)) {
                    allGroups.add(childGroup);
                }
            }
        }
        return allGroups;
    }

    /**
     * Получение списка идентификаторов переданного списка доменных объектов
     * @param domainObjects
     * @return
     */
    private List<Id> getIdList(List<DomainObject> domainObjects) {
        List<Id> result = new ArrayList<Id>();
        if (domainObjects != null) {
            for (DomainObject domainObject : domainObjects) {
                result.add(domainObject.getId());
            }
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
                
        Id parent = domainObject.getReference("parent_group_id");
        Id child = domainObject.getReference("child_group_id");

        // Получаем роли, которые включают родительскую роль с учетом иерархии
        List<DomainObject> roles = personManagementService.getAllParentGroup(parent);

        // Вызываем пересчет состава ролей всех этих ролей и самой изменяемой
        // роли
        refreshRoleRoles(parent);
        for (DomainObject role : roles) {
            refreshRoleRoles(role.getId());
        }
    }
}
