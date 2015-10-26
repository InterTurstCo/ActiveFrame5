package ru.intertrust.cm.core.business.api.schedule;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;

/**
 * Класс элемента рееста классов периодических заданий
 * @author larin
 *
 */
public class SheduleTaskReestrItem {
    private ScheduleTaskHandle scheduleTask;
    private ScheduleTask configuration;

    public SheduleTaskReestrItem(ScheduleTaskHandle scheduleTask, ScheduleTask configuration) {
        this.scheduleTask = scheduleTask;
        this.configuration = configuration;
    }

    public ScheduleTaskHandle getScheduleTask() {
        return scheduleTask;
    }

    public void setScheduleTask(ScheduleTaskHandle scheduleTask) {
        this.scheduleTask = scheduleTask;
    }

    public ScheduleTask getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ScheduleTask configuration) {
        this.configuration = configuration;
    }
}
