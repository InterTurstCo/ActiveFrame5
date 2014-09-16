package ru.intertrust.cm.core.business.impl.notification;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.impl.EventTriggerImpl.EventType;
import ru.intertrust.cm.core.dao.api.ActionListener;
import ru.intertrust.cm.core.dao.api.UserTransactionService;
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
    private NotificationSenderAsync acyncSender;

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
        //задержка, эмуляция работы других точек расширения в этой транзакции
        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
            for (SendNotificationInfo sendNotificationInfo : sendNotificationInfos) {
                acyncSender.sendNotifications(sendNotificationInfo.getDomainObject(), sendNotificationInfo.getEventType(),
                        sendNotificationInfo.getChangedFields());
            }
        }
    }

    private class SendNotificationInfo {
        private DomainObject domainObject;
        private EventType eventType;
        private List<FieldModification> changedFields;

        public DomainObject getDomainObject() {
            return domainObject;
        }

        public void setDomainObject(DomainObject domainObject) {
            this.domainObject = domainObject;
        }

        public EventType getEventType() {
            return eventType;
        }

        public void setEventType(EventType eventType) {
            this.eventType = eventType;
        }

        public List<FieldModification> getChangedFields() {
            return changedFields;
        }

        public void setChangedFields(List<FieldModification> changedFields) {
            this.changedFields = changedFields;
        }
    }
}
