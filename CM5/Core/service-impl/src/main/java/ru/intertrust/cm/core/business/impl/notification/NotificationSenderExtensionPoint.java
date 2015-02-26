package ru.intertrust.cm.core.business.impl.notification;

import java.util.List;

import javax.ejb.EJB;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.impl.EventType;
import ru.intertrust.cm.core.dao.api.extension.AfterChangeStatusAfterCommitExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterCreateAfterCommitExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

/**
 * 
 * @author atsvetkov
 * 
 */
@ExtensionPoint
public class NotificationSenderExtensionPoint implements AfterSaveAfterCommitExtensionHandler, AfterChangeStatusAfterCommitExtentionHandler,
        AfterCreateAfterCommitExtentionHandler, AfterDeleteAfterCommitExtensionHandler {
    @EJB
    private NotificationSenderAsync asyncSender;

    @Override
    public void onAfterDelete(DomainObject deletedDomainObject) {
        asyncSender.sendNotifications(deletedDomainObject, EventType.DELETE, null);
    }

    @Override
    public void onAfterCreate(DomainObject createdDomainObject) {
        asyncSender.sendNotifications(createdDomainObject, EventType.CREATE, null);
    }

    @Override
    public void onAfterChangeStatus(DomainObject domainObject) {
        asyncSender.sendNotifications(domainObject, EventType.CHANGE_STATUS, null);
    }

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        asyncSender.sendNotifications(domainObject, EventType.CHANGE, changedFields);
    }
}
