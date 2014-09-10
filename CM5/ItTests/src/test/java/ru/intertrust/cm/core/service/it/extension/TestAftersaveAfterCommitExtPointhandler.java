package ru.intertrust.cm.core.service.it.extension;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.extension.AfterChangeStatusAfterCommitExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterCreateAfterCommitExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

/**
 * Тестовый обработчик точек расширения, выполняющихся после комита транзакции
 * @author larin
 *
 */
@ExtensionPoint(filter = "Organization")
public class TestAftersaveAfterCommitExtPointhandler implements AfterSaveAfterCommitExtensionHandler, AfterChangeStatusAfterCommitExtentionHandler, AfterCreateAfterCommitExtentionHandler, AfterDeleteAfterCommitExtensionHandler {
    public static List<ActionInfo> save = new ArrayList<ActionInfo>();
    public static List<Id> create = new ArrayList<Id>();
    public static List<Id> delete = new ArrayList<Id>();
    public static List<Id> changeStatus = new ArrayList<Id>();

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.domainObjectId = domainObject.getId();
        actionInfo.modifications = changedFields;
        save.add(actionInfo);
    }

    public class ActionInfo {
        public Id domainObjectId;
        public List<FieldModification> modifications;
    }

    @Override
    public void onAfterDelete(DomainObject deletedDomainObject) {
        delete.add(deletedDomainObject.getId());
        
    }

    @Override
    public void onAfterCreate(DomainObject createdDomainObject) {
        create.add(createdDomainObject.getId());        
    }

    @Override
    public void onAfterChangeStatus(DomainObject domainObject) {
        changeStatus.add(domainObject.getId());        
    }
    
    public static void clear(){
        save.clear();
        create.clear();
        delete.clear();
        changeStatus.clear();
    }

}
