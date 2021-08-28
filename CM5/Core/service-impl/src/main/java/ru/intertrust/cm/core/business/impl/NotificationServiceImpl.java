package ru.intertrust.cm.core.business.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelHandle;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelLoader;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelSelector;
import ru.intertrust.cm.core.business.api.notification.NotificationServiceController;
import ru.intertrust.cm.core.dao.access.DynamicGroupService;
import ru.intertrust.cm.core.dao.access.PermissionServiceDao;
import ru.intertrust.cm.core.dao.api.ActionListener;
import ru.intertrust.cm.core.dao.api.UserTransactionService;
import ru.intertrust.cm.core.model.NotificationException;
import ru.intertrust.cm.core.model.RemoteSuitableException;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

@Stateless(name = "NotificationService")
@Local(NotificationService.class)
@Remote(NotificationService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@RunAs("system")
public class NotificationServiceImpl implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private UserTransactionService userTransactionService;

    @Autowired
    private PersonManagementService personManagementService;

    @Autowired
    private NotificationChannelSelector notificationChannelSelector;

    @Autowired
    private NotificationChannelLoader notificationChannelLoader;

    @Autowired
    private DynamicGroupService dynamicGroupService;

    @Autowired
    private PermissionServiceDao permissionService;

    @Autowired
    private NotificationServiceController notificationServiceController;

    @Resource
    private SessionContext sessionContext;

    @Override
    public void sendOnTransactionSuccess(String notificationType, Id sender, List<NotificationAddressee> addresseeList,
            NotificationPriority priority, NotificationContext context) {
        try {
            if (notificationServiceController.isEnable()) {
                SendNotificationActionListener listener =
                        new SendNotificationActionListener(notificationType, sender, addresseeList, priority, context);
                userTransactionService.addListener(listener);
                logger.debug("Register to send notification " + notificationType + " " + addresseeList);
            } else {
                logger.warn("Notification service is disabled");
            }
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    @Asynchronous
    public Future<Boolean> sendNow(String notificationType, Id sender, List<NotificationAddressee> addresseeList,
            NotificationPriority priority, NotificationContext context) {
        sendSync(notificationType, sender, addresseeList, priority, context);
        return new AsyncResult<Boolean>(true);
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

    public class SendNotificationActionListener implements ActionListener {
        private String notificationType;
        private Id sender;
        private String senderName;
        private List<NotificationAddressee> addresseeList;
        private NotificationPriority priority;
        private NotificationContext context;

        public SendNotificationActionListener(String notificationType, Id sender,
                List<NotificationAddressee> addresseeList, NotificationPriority priority, NotificationContext context) {
            super();
            this.notificationType = notificationType;
            this.sender = sender;
            this.addresseeList = addresseeList;
            this.priority = priority;
            this.context = context;
        }

        public SendNotificationActionListener(String notificationType, String senderName,
                List<NotificationAddressee> addresseeList, NotificationPriority priority, NotificationContext context) {
            super();
            this.notificationType = notificationType;
            this.setSenderName(senderName);
            this.addresseeList = addresseeList;
            this.priority = priority;
            this.context = context;
        }

        public String getNotificationType() {
            return notificationType;
        }

        public void setNotificationType(String notificationType) {
            this.notificationType = notificationType;
        }

        public Id getSender() {
            return sender;
        }

        public void setSender(Id sender) {
            this.sender = sender;
        }

        public List<NotificationAddressee> getAddresseeList() {
            return addresseeList;
        }

        public void setAddresseeList(List<NotificationAddressee> addresseeList) {
            this.addresseeList = addresseeList;
        }

        public NotificationPriority getPriority() {
            return priority;
        }

        public void setPriority(NotificationPriority priority) {
            this.priority = priority;
        }

        public NotificationContext getContext() {
            return context;
        }

        public void setContext(NotificationContext context) {
            this.context = context;
        }

        @Override
        public void onBeforeCommit() {
            //Вызываем асинхронный метод отправки уведомлений
            NotificationService notificationService = sessionContext.getBusinessObject(NotificationService.class);
            notificationService.sendNow(notificationType, sender, addresseeList, priority, context);
        }

        @Override
        public void onRollback() {
            // Ничего не делаем при откате транзакции
        }

        @Override
        public void onAfterCommit() {
            // Ничего не делаем

        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

    }

    @Override
    public void sendSync(String notificationType, Id sender, List<NotificationAddressee> addresseeList, NotificationPriority priority,
            NotificationContext context) {
        try {
            if (notificationServiceController.isEnable()) {
                logger.debug("Send notification " + notificationType + " " + addresseeList);
                //Получаем список адресатов
                List<Id> persons = getAddressee(addresseeList);
                int addresseeIdx = 0;

                for (Id personId : persons) {
                    context.setAddresseeIdx(addresseeIdx++);
                    context.addContextObject("addressee", new DomainObjectAccessor(personId));
                    //Получаем список каналов для персоны
                    List<String> channelNames =
                            notificationChannelSelector.getNotificationChannels(notificationType, personId, priority);
                    for (String channelName : channelNames) {
                        try {
                            NotificationChannelHandle notificationChannelHandle =
                                    notificationChannelLoader.getNotificationChannel(channelName);
                            notificationChannelHandle.send(notificationType, sender, personId, priority, context);
                        } catch (NotificationException ex) {
                            //skip exception, allow other channels to be executed.
                            logger.error("Error sending message on " + channelName + ", notificationType " + notificationType, ex);
                        }
                    }
                }
            } else {
                logger.warn("Notification service is disabled");
            }

        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void sendOnTransactionSuccess(String notificationType, String senderName, List<NotificationAddressee> addresseeList, NotificationPriority priority,
            NotificationContext context) {
        try {
            if (notificationServiceController.isEnable()) {
                SendNotificationActionListener listener =
                        new SendNotificationActionListener(notificationType, senderName, addresseeList, priority, context);
                userTransactionService.addListener(listener);
                logger.debug("Register to send notification " + notificationType + " " + addresseeList);
            } else {
                logger.warn("Notification service is disabled");
            }
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public Future<Boolean> sendNow(String notificationType, String senderName, List<NotificationAddressee> addresseeList, NotificationPriority priority,
            NotificationContext context) {
        sendSync(notificationType, senderName, addresseeList, priority, context);
        return new AsyncResult<Boolean>(true);
    }

    @Override
    public void sendSync(String notificationType, String senderName, List<NotificationAddressee> addresseeList, NotificationPriority priority,
            NotificationContext context) {
        try {
            if (notificationServiceController.isEnable()) {
                logger.debug("Send notification " + notificationType + " " + addresseeList);
                //Получаем список адресатов
                List<Id> persons = getAddressee(addresseeList);

                for (Id personId : persons) {
                    context.addContextObject("addressee", new DomainObjectAccessor(personId));
                    //Получаем список каналов для персоны
                    List<String> channelNames =
                            notificationChannelSelector.getNotificationChannels(notificationType, personId, priority);
                    for (String channelName : channelNames) {
                        try {
                            NotificationChannelHandle notificationChannelHandle =
                                    notificationChannelLoader.getNotificationChannel(channelName);
                            notificationChannelHandle.send(notificationType, senderName, personId, priority, context);
                        } catch (NotificationException ex) {
                            //skip exception, allow other channels to be executed.
                            logger.error("Error sending message on " + channelName + ", notificationType " + notificationType, ex);
                        }
                    }
                }
            } else {
                logger.warn("Notification service is disabled");
            }

        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }
}
