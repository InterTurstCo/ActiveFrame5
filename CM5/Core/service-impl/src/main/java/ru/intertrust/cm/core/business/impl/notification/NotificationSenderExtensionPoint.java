package ru.intertrust.cm.core.business.impl.notification;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.impl.EventTriggerImpl.EventType;
import ru.intertrust.cm.core.dao.api.ActionListener;
import ru.intertrust.cm.core.dao.api.UserTransactionService;
import ru.intertrust.cm.core.dao.api.extension.*;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private UserTransactionService userTransactionService;

    @Override
    public void onAfterDelete(DomainObject deletedDomainObject) {
        getTxListener().addSendNotificationInfo(deletedDomainObject, EventType.DELETE, null);
    }

    @Override
    public void onAfterCreate(DomainObject createdDomainObject) {
        getTxListener().addSendNotificationInfo(createdDomainObject, EventType.CREATE, null);
    }

    @Override
    public void onAfterChangeStatus(DomainObject domainObject) {
        getTxListener().addSendNotificationInfo(domainObject, EventType.CHANGE_STATUS, null);
    }

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        getTxListener().addSendNotificationInfo(domainObject, EventType.CHANGE, changedFields);
    }

    private NotificationSenderActionListener getTxListener() {
        NotificationSenderActionListener listener = userTransactionService.getListener(NotificationSenderActionListener.class);
        if (listener == null) {
            listener = new NotificationSenderActionListener();
            userTransactionService.addListener(listener);
        }
        return listener;
    }

    private class NotificationSenderActionListener implements ActionListener {
        private List<SendNotificationInfo> sendNotificationInfos = new ArrayList<SendNotificationInfo>();

        private void addSendNotificationInfo(DomainObject domainObject, EventType eventType, List<FieldModification> changedFields) {
            SendNotificationInfo sendNotificationInfo = new SendNotificationInfo();
            sendNotificationInfo.setChangedFields(changedFields);
            sendNotificationInfo.setDomainObject(domainObject);
            sendNotificationInfo.setEventType(eventType);
            sendNotificationInfos.add(sendNotificationInfo);
        }

        @Override
        public void onBeforeCommit() {
            // Ничего не делаем
        }

        @Override
        public void onRollback() {
            // Ничего не делаем
        }

        @Override
        public void onAfterCommit() {
            asyncSender.sendNotifications(sendNotificationInfos);
        }
    }

}
