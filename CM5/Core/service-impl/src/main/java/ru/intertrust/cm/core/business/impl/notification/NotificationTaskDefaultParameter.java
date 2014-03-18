package ru.intertrust.cm.core.business.impl.notification;

import ru.intertrust.cm.core.business.api.notification.NotificationTaskConfig;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskDefaultParameters;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;


public class NotificationTaskDefaultParameter implements ScheduleTaskDefaultParameters {
    @Override
    public ScheduleTaskParameters getDefaultParameters() {
        return new NotificationTaskConfig();
    }
}
