package ru.intertrust.cm.core.business.impl.notification;

import java.util.Collection;
import java.util.List;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.EventTrigger;
import ru.intertrust.cm.core.business.api.NotificationService;
import ru.intertrust.cm.core.business.api.ScriptService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.impl.EventType;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.config.NotificationConfig;
import ru.intertrust.cm.core.config.TriggerConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DoelEvaluator;
import ru.intertrust.cm.core.dao.api.DomainObjectFinderService;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;

@Stateless(name = "NotificationSenderAsync")
@Local(NotificationSenderAsync.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class NotificationSenderAsyncImpl extends NotificationSenderBase implements NotificationSenderAsync {
    final static Logger logger = LoggerFactory.getLogger(NotificationSenderAsyncImpl.class);

    @Autowired
    protected ConfigurationExplorer configurationExplorer;

    @Autowired
    protected EventTrigger eventTrigger;

    @EJB
    protected NotificationService notificationService;

    @Override
    @Asynchronous
    public void sendNotifications(List<SendNotificationInfo> sendNotificationInfos) {
        for (SendNotificationInfo sendNotificationInfo : sendNotificationInfos) {
            try {
                doSendNotification(sendNotificationInfo.getDomainObject(), sendNotificationInfo.getEventType(), sendNotificationInfo.getChangedFields());
            } catch (Throwable throwable) { // do not block other exceptions which might be successful
                logger.error("Notification failed", throwable);
            }
        }
    }

    @Override
    @Asynchronous
    public void sendNotifications(DomainObject domainObject, EventType eventType, List<FieldModification> changedFields) {
        try {
            doSendNotification(domainObject, eventType, changedFields);
        } catch (Throwable throwable) {
            logger.error("Notification failed", throwable);
        }
    }

    public ConfigurationExplorer getConfigurationExplorer() {
        return configurationExplorer;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public EventTrigger getEventTrigger() {
        return eventTrigger;
    }

    public void setEventTrigger(EventTrigger eventTrigger) {
        this.eventTrigger = eventTrigger;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public CurrentUserAccessor getCurrentUserAccessor() {
        return currentUserAccessor;
    }

    public void setCurrentUserAccessor(CurrentUserAccessor currentUserAccessor) {
        this.currentUserAccessor = currentUserAccessor;
    }

    public DomainObjectFinderService getDomainObjectFinderService() {
        return domainObjectFinderService;
    }

    public void setDomainObjectFinderService(DomainObjectFinderService domainObjectFinderService) {
        this.domainObjectFinderService = domainObjectFinderService;
    }

    private void doSendNotification(DomainObject domainObject, EventType eventType, List<FieldModification> changedFields) {
        Collection<NotificationConfig> notifications = configurationExplorer.getConfigs(NotificationConfig.class);
        for (NotificationConfig notificationConfig : notifications) {
            try {
                List<TriggerConfig> notificationTriggers =
                        notificationConfig.getNotificationTypeConfig().getNotificationTriggersConfig().getTriggers();
                for (TriggerConfig triggerConfig : notificationTriggers) {
                    boolean isTriggered = false;
                    if (triggerConfig.getRefName() != null) {
                        isTriggered = eventTrigger.isTriggered(triggerConfig.getRefName(), eventType.toString(),
                                domainObject, changedFields);
                    } else {
                        isTriggered = eventTrigger.isTriggered(triggerConfig, eventType.toString(),
                                domainObject, changedFields);
                    }

                    if (isTriggered) {
                        sendNotification(domainObject, eventType, notificationConfig);
                        break;
                    }
                }
            } catch (Throwable ex) {
                throw new FatalException("Error send notification with type " + notificationConfig.getNotificationTypeConfig().getName(), ex);
            }
        }
    }

    protected void sendNotification(DomainObject domainObject, EventType eventType, NotificationConfig notificationConfig) {
        String notificationType = notificationConfig.getNotificationTypeConfig().getName();
        NotificationContext notificationContext = new NotificationContext();
        notificationContext.setNotificationSettings(notificationConfig.getNotificationTypeConfig());
        notificationContext.addContextObject("document", new DomainObjectAccessor(domainObject));
        fillAdditionalContextObjects(notificationContext, notificationConfig.getNotificationTypeConfig().getNotificationContextConfig(), domainObject);
        NotificationPriority priority = notificationConfig.getNotificationTypeConfig().getPriority();
        Id senderId = getSender(domainObject, notificationConfig);

        List<NotificationAddressee> addresseeList = getAddresseeList(domainObject.getId(), notificationConfig.getNotificationTypeConfig());
        logger.debug("Sending notification: " + notificationType + " on event: " + eventType + " for Domain Object: " + domainObject);
        notificationService.sendSync(notificationType, senderId,
                addresseeList, priority, notificationContext);
    }

    protected Id getSender(DomainObject domainObject, NotificationConfig notificationConfig) {
        FindObjectsConfig findPersonConfig = notificationConfig.getNotificationTypeConfig().getSenderConfig();
        if (findPersonConfig != null) {
            List<Id> senders = domainObjectFinderService.findObjects(findPersonConfig, domainObject.getId(), notificationConfig.getNotificationTypeConfig());

            if (senders != null && senders.size() > 0) {
                return senders.get(0);
            }

        }
        return currentUserAccessor.getCurrentUserId();
    }
}
