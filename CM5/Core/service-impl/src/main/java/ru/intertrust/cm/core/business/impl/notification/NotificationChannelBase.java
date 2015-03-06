package ru.intertrust.cm.core.business.impl.notification;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.NotificationTextFormer;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Profile;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

/**
 * Базовый класс для всех каналов отправки уведомлений. Содержит общий функционал всех каналов.
 * @author atsvetkov
 *
 */
public class NotificationChannelBase {

    @Autowired
    protected DomainObjectDao domainObjectDao;

    @Autowired
    protected MailSenderWrapper mailSenderWrapper;

    @Autowired
    protected AccessControlService accessControlService;

    @Autowired
    protected NotificationTextFormer notificationTextFormer;

    @Autowired
    protected CollectionsService collectionService;
    
    @Autowired
    protected IdService idService;
    
    @Autowired
    protected ConfigurationExplorer configurationExplorer;
    
    @Autowired
    protected ProfileService profileService;
    
    @Inject
    protected AttachmentService attachmentService;    

    public void setMailSender(MailSenderWrapper mailSenderWrapper) {
        this.mailSenderWrapper = mailSenderWrapper;
    }

    public void setDomainObjectDao(DomainObjectDao domainObjectDao) {
        this.domainObjectDao = domainObjectDao;
    }

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    public void setNotificationTextFormer(NotificationTextFormer notificationTextFormer) {
        this.notificationTextFormer = notificationTextFormer;
    }

    public void setCollectionService(CollectionsService.Remote collectionService) {
        this.collectionService = collectionService;
    }
    
    protected String getPersonLocale(Id personId){
        Profile profile = profileService.getPersonProfileByPersonId(personId);
        return profile.getString(ProfileService.LOCALE);
    }
    
    protected Id findLocaleIdByName(String localeName) {
        String query = "select t.id from locale t where t.name='" + localeName + "'";

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);
        Id locale = null;
        if (collection.size() > 0) {
            locale = collection.get(0).getId();
        }
        return locale;
    }
}
