package ru.intertrust.cm.core.dao.impl.personmanager;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

/**
 * Точка расширения после сохранения пользовательской группы. Создает запись в
 * типе group_group со ссылкой сама на себя
 * @author larin
 * 
 */
@ExtensionPoint(filter = "User_Group")
public class OnSaveUserGroupExtensionPoint implements AfterSaveExtensionHandler {

    @Autowired
    private PersonManagementServiceDao personManagementService;

    @Autowired
    private DomainObjectDao domainObjectDao;    
    
    @Override
    public void onAfterSave(DomainObject domainObject) {
        if (!personManagementService.isGroupInGroup(domainObject.getId(), domainObject.getId(), true)){
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

}
