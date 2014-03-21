package ru.intertrust.cm.core.business.impl.notification;

import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationTaskMode;
import ru.intertrust.cm.core.business.api.notification.NotificationTaskConfig;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskDefaultParameters;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.config.FindObjectsQueryConfig;


public class NotificationTaskDefaultParameter implements ScheduleTaskDefaultParameters {
    @Override
    public ScheduleTaskParameters getDefaultParameters() {
        
        NotificationTaskConfig defaultParam = new NotificationTaskConfig();
        defaultParam.setNotificationType("DEFAULT_NOTIFICATION");
        defaultParam.setNotificationPriority(NotificationPriority.NORMAL);
        defaultParam.setTaskMode(NotificationTaskMode.BY_DOMAIN_OBJECT);
        
        //По всем организациям
        FindObjectsConfig findDomainObject = new FindObjectsConfig();
        findDomainObject.setFindObjectType(new FindObjectsQueryConfig("select id from status where id = -1"));
        defaultParam.setFindDomainObjects(findDomainObject);

        //Руководителям организации
        FindObjectsConfig findPersonObject = new FindObjectsConfig();
        findPersonObject.setFindObjectType(new FindObjectsQueryConfig("select id from status where id = {0}"));
        defaultParam.setFindPersons(findPersonObject);
        
        return defaultParam;
    }
}
