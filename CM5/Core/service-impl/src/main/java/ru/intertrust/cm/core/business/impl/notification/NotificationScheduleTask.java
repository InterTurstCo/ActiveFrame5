package ru.intertrust.cm.core.business.impl.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.SessionContext;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.NotificationService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseePerson;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.notification.NotificationTaskConfig;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.api.schedule.SheduleType;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectFinderService;

/**
 * Периодическое задание формирующее уведомления по расписанию
 */
@ScheduleTask(name = "NotificationScheduleTask", minute = "*/1", configClass = NotificationTaskDefaultParameter.class,
        type = SheduleType.Multipliable)
public class NotificationScheduleTask implements ScheduleTaskHandle {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DomainObjectFinderService findObjectService;

    @Autowired
    private CrudService crudService;

    @Override
    public String execute(SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {

        NotificationTaskConfig notificationTaskConfig = (NotificationTaskConfig) parameters;

        switch (notificationTaskConfig.getTaskMode()) {
            case BY_DOMAIN_OBJECT:
                sendNotificationsByDomainObject(notificationTaskConfig, sessionContext);
                break;
            case BY_PERSON:
                sendNotificationsByPerson(notificationTaskConfig, sessionContext);
                break;
        }


        return "COMPLETE";
    }

    private void sendNotificationsByDomainObject(NotificationTaskConfig notificationTaskConfig, SessionContext sessionContext)
            throws InterruptedException {
        FindObjectsConfig findDomainObjects = notificationTaskConfig.getFindDomainObjects();
        List<Id> domainObjectIds = findObjectService.findObjects(findDomainObjects, null);
        if (domainObjectIds != null) {
            for (Id domainObjectId : domainObjectIds) {
                FindObjectsConfig findPersons = notificationTaskConfig.getFindPersons();
                List<Id> personIds = findObjectService.findObjects(findPersons, domainObjectId);
                sendNotification(notificationTaskConfig, personIds, domainObjectId, sessionContext);
            }
        }
    }

    private void sendNotificationsByPerson(NotificationTaskConfig notificationTaskConfig, SessionContext sessionContext)
            throws InterruptedException {
        FindObjectsConfig findPersons = notificationTaskConfig.getFindPersons();
        List<Id> personIds = findObjectService.findObjects(findPersons, null);
        if (personIds != null) {
            for (Id personId : personIds) {
                FindObjectsConfig findDomainObjects = notificationTaskConfig.getFindDomainObjects();
                List<Id> domainObjectIds = findObjectService.findObjects(findDomainObjects, personId);
                if (domainObjectIds != null) {
                    for (Id domainObjectId : domainObjectIds) {
                        sendNotification(notificationTaskConfig, Collections.singletonList(personId), domainObjectId, sessionContext);
                    }
                }
            }
        }
    }


    private void sendNotification(NotificationTaskConfig notificationTaskConfig,
                                  List<Id> personIds, Id domainObjectId, SessionContext sessionContext)
            throws InterruptedException {
        if (personIds == null || personIds.isEmpty()) return;
        if (domainObjectId == null) return;

        if (sessionContext.wasCancelCalled()) {
            throw new InterruptedException("Notification schedule task was interrupted");
        }

        List<NotificationAddressee> addresseeList = new ArrayList<>();
        for (Id personId : personIds) {
            if (personId != null){
                addresseeList.add(new NotificationAddresseePerson(personId));
            }
        }

        if (addresseeList.size() > 0) {

            DomainObject domainObject = crudService.find(domainObjectId);

            NotificationContext notificationContext = new NotificationContext();
            notificationContext.addContextObject("document", domainObject);


            notificationService.sendOnTransactionSuccess(
                    notificationTaskConfig.getNotificationType(),
                    null,
                    addresseeList,
                    notificationTaskConfig.getNotificationPriority(),
                    notificationContext);
        }
    }

}
