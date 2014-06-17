package ru.intertrust.cm.core.business.shedule;

/**
 * Business interface for ScheduleTaskLoader
 * Created by vmatsukevich on 6/17/14.
 */
public interface ScheduleTaskLoaderInterface {

    interface Remote extends ScheduleTaskLoaderInterface {}

    void load();
}
