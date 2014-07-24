package ru.intertrust.cm.test.notification;


import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FindObjectSettings;
import ru.intertrust.cm.core.config.FindObjectsClassConfig;
import ru.intertrust.cm.core.config.FindObjectsType;
import ru.intertrust.cm.core.config.NotificationConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectFinder;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.test.configuration.FindPersonByDomainObjectFieldsSettings;
import ru.intertrust.testmodule.spring.ApplicationContextProvider;

public class TestNotificationAddresseFinder implements DomainObjectFinder {

    private ConfigurationExplorer configurationExplorer;
    
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    public TestNotificationAddresseFinder() {
        initializeSpringBeans();
    }

    private void initializeSpringBeans() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        configurationExplorer = applicationContext.getBean(ConfigurationExplorer.class);
        domainObjectTypeIdCache = applicationContext.getBean(DomainObjectTypeIdCache.class);
    }

    @Override
    public List<Id> findObjects(Id contextDomainObjectId) {
        List<Id> persons = new ArrayList();
        NotificationConfig notificationAddresseeConfig =
                configurationExplorer.getConfig(NotificationConfig.class, "NotificationAddresseeClassName");
        FindObjectsType findObjects =
                notificationAddresseeConfig.getNotificationTypeConfig().getNotificationAddresseConfig().getFindPerson()
                        .getFindObjectType();

        if (findObjects instanceof FindObjectsClassConfig) {
            FindObjectsClassConfig findByClassConfig = (FindObjectsClassConfig) findObjects;
            FindObjectSettings settings = findByClassConfig.getSettings();
            if (settings instanceof FindPersonByDomainObjectFieldsSettings) {
                String field = ((FindPersonByDomainObjectFieldsSettings) settings).getField();
                persons.add(new RdbmsId(1, domainObjectTypeIdCache.getId("Person")));

            }
        }
        return persons;
    }

}
