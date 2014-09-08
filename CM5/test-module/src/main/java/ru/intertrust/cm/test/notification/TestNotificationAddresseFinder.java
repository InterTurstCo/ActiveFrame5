package ru.intertrust.cm.test.notification;


import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FindObjectSettings;
import ru.intertrust.cm.core.config.NotificationTypeConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectFinder;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.test.configuration.FindPersonByDomainObjectFieldsSettings;
import ru.intertrust.testmodule.spring.ApplicationContextProvider;

/**
 * Тестовый класс поиска адресатов
 * @author atsvetkov
 *
 */
public class TestNotificationAddresseFinder implements DomainObjectFinder {

    private ConfigurationExplorer configurationExplorer;
    
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    private FindObjectSettings settings;

    private NotificationTypeConfig notificationTypeConfig;

    public TestNotificationAddresseFinder() {
        initializeSpringBeans();
    }

    @Override
    public void init(FindObjectSettings settings, Dto extensionContext) {
        this.settings = settings;
        this.notificationTypeConfig = (NotificationTypeConfig) extensionContext;
    }

    private void initializeSpringBeans() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        configurationExplorer = applicationContext.getBean(ConfigurationExplorer.class);
        domainObjectTypeIdCache = applicationContext.getBean(DomainObjectTypeIdCache.class);
    }

    @Override
    public List<Id> findObjects(Id contextDomainObjectId) {
        List<Id> persons = new ArrayList();
        if (settings instanceof FindPersonByDomainObjectFieldsSettings) {
            String field = ((FindPersonByDomainObjectFieldsSettings) settings).getField();
            persons.add(new RdbmsId(domainObjectTypeIdCache.getId("Person"), 1));

        }
        return persons;
    }

}
