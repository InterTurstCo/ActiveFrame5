package ru.intertrust.cm.core.business.impl.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.EventTrigger;
import ru.intertrust.cm.core.business.api.NotificationService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeContextRole;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeDynamicGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseePerson;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.impl.EventTriggerImpl.EventType;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.config.NotificationConfig;
import ru.intertrust.cm.core.config.TriggerConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectFinderService;

/**
 * 
 * @author atsvetkov
 *
 */
public abstract class NotificationSenderExtensionPointBase {
    
    @Autowired
    protected ConfigurationExplorer configurationExplorer;
    
    @Autowired    
    protected EventTrigger eventTrigger;

    @EJB
    protected NotificationService notificationService;
    
    @Autowired    
    protected CurrentUserAccessor currentUserAccessor;
    
    @Autowired        
    protected DomainObjectFinderService domainObjectFinderService;
    
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

    protected void sendNotifications(DomainObject domainObject, List<FieldModification> changedFields) {
        Collection<NotificationConfig> notifications = configurationExplorer.getConfigs(NotificationConfig.class);
        for (NotificationConfig notificationConfig : notifications) {
            List<TriggerConfig> notificationTriggers =
                    notificationConfig.getNotificationTypeConfig().getNotificationTriggersConfig().getTriggers();
            for (TriggerConfig triggerConfig : notificationTriggers) {
                boolean isTriggered = false;
                if (triggerConfig.getRefName() != null) {
                    isTriggered = eventTrigger.isTriggered(triggerConfig.getRefName(), getEventType().toString(),
                            domainObject, changedFields);
                } else {
                    isTriggered = eventTrigger.isTriggered(triggerConfig, getEventType().toString(),
                            domainObject, changedFields);
                }

                if (isTriggered) {
                    sendNotification(domainObject, notificationConfig);
                }
            }

        }
    }

    abstract protected EventType getEventType();

    protected void sendNotification(DomainObject domainObject, NotificationConfig notificationConfig) {
        String notificationType = notificationConfig.getNotificationTypeConfig().getName();
        NotificationContext notificationContext = new NotificationContext();
        notificationContext.addContextObject("document", domainObject.getId());
        NotificationPriority priority = NotificationPriority
                .valueOf(notificationConfig.getNotificationTypeConfig().getPriority());
        Id currentUserId = currentUserAccessor.getCurrentUserId();

        List<NotificationAddressee> addresseeList = getAddresseeList(domainObject, notificationConfig);
        notificationService.sendOnTransactionSuccess(notificationType, currentUserId,
                addresseeList, priority, notificationContext);
    }

    protected List<NotificationAddressee> getAddresseeList(DomainObject domainObject,
            NotificationConfig notificationConfig) {
        List<NotificationAddressee> addresseeList = new ArrayList<NotificationAddressee>();
        FindObjectsConfig findPerson = notificationConfig.getNotificationTypeConfig().getNotificationAddresseConfig().getFindPerson();
        
        List<NotificationAddressee> addresseList = new  ArrayList<NotificationAddressee>();        
        if(findPerson != null) {
            List<Id> personIds =
                    domainObjectFinderService.findObjects(findPerson, domainObject.getId());
            if (personIds != null) {
                for (Id personId : personIds) {
                    addresseList.add(new NotificationAddresseePerson(personId));
                }
            }
    
        } else if (notificationConfig.getNotificationTypeConfig().getNotificationAddresseConfig()
                .getContextRole() != null) {
            String contextRoleName =
                    notificationConfig.getNotificationTypeConfig().getNotificationAddresseConfig()
                            .getContextRole().getData();
            NotificationAddresseeContextRole addresseeContextRole =
                    new NotificationAddresseeContextRole(contextRoleName, domainObject.getId());
            addresseList.add(addresseeContextRole);
        } else if (notificationConfig.getNotificationTypeConfig().getNotificationAddresseConfig()
                .getDynamicGroup() != null) {
            String dynamicGroupName =
                    notificationConfig.getNotificationTypeConfig().getNotificationAddresseConfig()
                            .getDynamicGroup().getData();
            NotificationAddresseeDynamicGroup notificationAddresseeDynamicGroup =
                    new NotificationAddresseeDynamicGroup(dynamicGroupName, domainObject.getId());
            addresseList.add(notificationAddresseeDynamicGroup);
        }
        return addresseeList;
    }
}
