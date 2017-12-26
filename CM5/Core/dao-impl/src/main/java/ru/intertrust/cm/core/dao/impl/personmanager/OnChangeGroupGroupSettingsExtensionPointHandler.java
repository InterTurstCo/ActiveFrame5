package ru.intertrust.cm.core.dao.impl.personmanager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.DynamicGroupSettings;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
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
    final static Logger logger = LoggerFactory.getLogger(OnChangeGroupGroupSettingsExtensionPointHandler.class);

    @Autowired
    private PersonManagementServiceDao personManagementService;

    @Autowired
    private DomainObjectCacheService domainObjectCacheService;

    @Autowired
    private DynamicGroupSettings dynamicGroupSettings;    
    
    /**
     * Входная точка точки расширения. Вызывается когда сохраняется доменный
     * обьект group_group_settings
     */
    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        if (!dynamicGroupSettings.isDisableGroupUncover()) {
            clearCollectionCache();

            Id parent = domainObject.getReference("parent_group_id");
            Id child = domainObject.getReference("child_group_id");
            // Проверка на зацикливание
            if (personManagementService.isGroupInGroup(child, parent, true)) {
                throw new ExtensionPointException("Found cycle in groups. Group " + child + " exists role " + parent);
            }

            // Получаем группы, которые включают родительскую группу с учетом иерархии
            List<DomainObject> groups = personManagementService.getAllParentGroup(parent);

            // Вызываем пересчет состава ролей всех этих групп и самой изменяемой группы
            personManagementService.recalcGroupGroup(parent);
            for (DomainObject group : groups) {
                personManagementService.recalcGroupGroup(group.getId());
            }
        } else {
            logger.debug("Group uncover is disabled");
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

    @Override
    public void onAfterDelete(DomainObject domainObject) {
        clearCollectionCache();

        Id parent = domainObject.getReference("parent_group_id");

        // Получаем группы, которые включают родительскую группу с учетом иерархии
        List<DomainObject> groups = personManagementService.getAllParentGroup(parent);
        // Вызываем пересчет состава групп всех этих групп и самой изменяемой группы
        personManagementService.recalcGroupGroup(parent);
        for (DomainObject group : groups) {
            personManagementService.recalcGroupGroup(group.getId());
        }
    }
}
