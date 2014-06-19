package ru.intertrust.cm.core.business.shedule;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;

import java.util.List;

/**
 * Business interface for ScheduleTaskLoader
 * Created by vmatsukevich on 6/17/14.
 */
public interface ScheduleTaskLoaderInterface {

    interface Remote extends ScheduleTaskLoaderInterface {}

    void load();

    List<SheduleTaskReestrItem> getSheduleTaskReestrItems(boolean singletonOnly);

    DomainObject createTaskDomainObject(SheduleTaskReestrItem item);

    SheduleTaskReestrItem getSheduleTaskReestrItem(String className);

    DomainObject createTaskDomainObject(SheduleTaskReestrItem item, String name);

    ScheduleTaskHandle getSheduleTaskHandle(String className);

}
