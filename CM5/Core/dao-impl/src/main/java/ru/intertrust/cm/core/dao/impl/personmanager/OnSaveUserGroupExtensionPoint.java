package ru.intertrust.cm.core.dao.impl.personmanager;

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
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.BeforeDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

/**
 * Точка расширения после сохранения пользовательской группы. Создает запись в
 * типе group_group со ссылкой сама на себя
 * @author larin
 * 
 */
@ExtensionPoint(filter = "User_Group")
public class OnSaveUserGroupExtensionPoint implements AfterSaveExtensionHandler, BeforeDeleteExtensionHandler {

    @Autowired
    private PersonManagementServiceDao personManagementService;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private CollectionsDao collectionsDao;

    @Override
    public void onAfterSave(DomainObject domainObject) {
        if (!personManagementService.isGroupInGroup(domainObject.getId(), domainObject.getId(), true)) {
            DomainObject groupGroup = createDomainObject("group_group");
            groupGroup.setReference("parent_group_id", domainObject.getId());
            groupGroup.setReference("child_group_id", domainObject.getId());
            domainObjectDao.save(groupGroup);
        }
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
     * Точка расширения удаления UserGroup
     */
    @Override
    public void onBeforeDelete(DomainObject deletedDomainObject) {
        Id groupGroupId = getGroupGroupId(deletedDomainObject.getId(), deletedDomainObject.getId());
        domainObjectDao.delete(groupGroupId);
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

}
