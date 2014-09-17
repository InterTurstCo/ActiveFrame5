package ru.intertrust.cm.core.business.impl.notification;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.NotificationService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeContextRole;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeDynamicGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseePerson;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.notification.NotificationTaskConfig;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.api.schedule.SheduleType;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.dao.access.DynamicGroupService;
import ru.intertrust.cm.core.dao.access.PermissionServiceDao;
import ru.intertrust.cm.core.dao.api.DomainObjectFinderService;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;

/**
 * Периодическое задание формирующее уведомления по расписанию
 */
@ScheduleTask(name = "NotificationScheduleTask", minute = "*/1", configClass = NotificationTaskDefaultParameter.class,
        type = SheduleType.Multipliable)
public class NotificationScheduleTask extends NotificationSenderBase implements ScheduleTaskHandle {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DomainObjectFinderService findObjectService;

    @Autowired
    private CrudService crudService;

    @Autowired
    private PersonManagementService personManagementService;

    @Autowired
    private DynamicGroupService dynamicGroupService;

    @Autowired
    private PermissionServiceDao permissionService;

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
        List<Id> domainObjectIds = findObjectService.findObjects(findDomainObjects, null, notificationTaskConfig);
        if (domainObjectIds != null) {
            for (Id domainObjectId : domainObjectIds) {
                List<NotificationAddressee> addresseeList = getAddresseeList(domainObjectId, notificationTaskConfig);
                sendNotification(notificationTaskConfig, addresseeList, domainObjectId, sessionContext);
            }
        }
    }

    private void sendNotificationsByPerson(NotificationTaskConfig notificationTaskConfig, SessionContext sessionContext)
            throws InterruptedException {
        List<NotificationAddressee> addresseeList = getAddresseeList(null, notificationTaskConfig);
        List<Id> personIds = getAddressee(addresseeList);
        if (personIds != null) {
            for (Id personId : personIds) {
                FindObjectsConfig findDomainObjects = notificationTaskConfig.getFindDomainObjects();
                List<Id> domainObjectIds = findObjectService.findObjects(findDomainObjects, personId, notificationTaskConfig);
                if (domainObjectIds != null) {
                    for (Id domainObjectId : domainObjectIds) {
                        List<NotificationAddressee> addressee = new ArrayList<NotificationAddressee>();
                        addressee.add(new NotificationAddresseePerson(personId));
                        sendNotification(notificationTaskConfig, addressee, domainObjectId, sessionContext);
                    }
                }
            }
        }
    }

    private void sendNotification(NotificationTaskConfig notificationTaskConfig,
            List<NotificationAddressee> addresseeList, Id domainObjectId, SessionContext sessionContext)
            throws InterruptedException {
        if (addresseeList == null || addresseeList.isEmpty())
            return;
        if (domainObjectId == null)
            return;

        if (sessionContext.wasCancelCalled()) {
            throw new InterruptedException("Notification schedule task was interrupted");
        }

        if (addresseeList.size() > 0) {

            DomainObject domainObject = crudService.find(domainObjectId);

            NotificationContext notificationContext = new NotificationContext();
            notificationContext.setNotificationSettings(notificationTaskConfig);
            notificationContext.addContextObject("document", new DomainObjectAccessor(domainObject));
            fillAdditionalContextObjects(notificationContext, notificationTaskConfig.getNotificationContextConfig(), domainObject);

            Id sender = null;
            if (notificationTaskConfig.getSenderConfig() != null) {
                List<Id> senders = findObjectService.findObjects(notificationTaskConfig.getSenderConfig(), domainObject.getId(), notificationTaskConfig);
                if (senders != null && senders.size() > 0) {
                    sender = senders.get(0);
                }
            }

            notificationService.sendOnTransactionSuccess(
                    notificationTaskConfig.getName(),
                    sender,
                    addresseeList,
                    notificationTaskConfig.getPriority(),
                    notificationContext);
        }
    }

    /**
     * Получение списка персон адресатов уведомления
     * @param addresseeList
     * @return
     */
    private List<Id> getAddressee(List<NotificationAddressee> addresseeList) {
        List<Id> persons = new ArrayList<Id>();

        for (NotificationAddressee notificationAddressee : addresseeList) {

            if (notificationAddressee instanceof NotificationAddresseePerson) {
                persons.add(((NotificationAddresseePerson) notificationAddressee).getPersonId());
            } else if (notificationAddressee instanceof NotificationAddresseeGroup) {
                Id groupId = ((NotificationAddresseeGroup) notificationAddressee).getGroupId();
                List<DomainObject> personDomainObjects = personManagementService.getAllPersonsInGroup(groupId);
                for (DomainObject personObject : personDomainObjects) {
                    persons.add(personObject.getId());
                }
            } else if (notificationAddressee instanceof NotificationAddresseeDynamicGroup) {
                NotificationAddresseeDynamicGroup addressee = (NotificationAddresseeDynamicGroup) notificationAddressee;
                persons.addAll(dynamicGroupService.getPersons(addressee.getContextId(), addressee.getGroupName()));
            } else if (notificationAddressee instanceof NotificationAddresseeContextRole) {
                NotificationAddresseeContextRole addressee = (NotificationAddresseeContextRole) notificationAddressee;
                persons.addAll(permissionService.getPersons(addressee.getContextId(), addressee.getRoleName()));
            }
        }
        return persons;
    }
}
