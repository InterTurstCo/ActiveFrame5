package ru.intertrust.cm.core.business.impl.notification;

import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationTaskMode;
import ru.intertrust.cm.core.business.api.notification.NotificationTaskConfig;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskDefaultParameters;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.config.FindObjectsQueryConfig;
import ru.intertrust.cm.core.config.NotificationAddresseConfig;


public class NotificationTaskDefaultParameter implements ScheduleTaskDefaultParameters {
    @Override
    public ScheduleTaskParameters getDefaultParameters() {
        
        NotificationTaskConfig defaultParam = new NotificationTaskConfig();
        defaultParam.setName("DEFAULT_NOTIFICATION");
        defaultParam.setPriority(NotificationPriority.NORMAL);
        defaultParam.setTaskMode(NotificationTaskMode.BY_DOMAIN_OBJECT);
        
        //По всем организациям
        FindObjectsConfig findDomainObject = new FindObjectsConfig();
        findDomainObject.setFindObjectType(new FindObjectsQueryConfig("select id from status where id = -1"));
        defaultParam.setFindDomainObjects(findDomainObject);

        //Руководителям организации
        FindObjectsConfig findPersonObject = new FindObjectsConfig();
        findPersonObject.setFindObjectType(new FindObjectsQueryConfig("select id from status where id = {0}"));
        NotificationAddresseConfig notificationAddresseConfig = new NotificationAddresseConfig();
        notificationAddresseConfig.setFindPerson(findPersonObject);        
        defaultParam.setNotificationAddresseConfig(notificationAddresseConfig);
        
        return defaultParam;
    }
}
