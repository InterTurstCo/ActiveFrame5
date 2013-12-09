package ru.intertrust.cm.test.schedule;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.model.ScheduleException;

@ScheduleTask(name = "TestSingleSchedule", minute = "*/1")
public class TestSingleSchedule implements ScheduleTaskHandle {

    @Override
    public String execute(ScheduleTaskParameters parameters) {
        try {
            System.out.println("Run TestSingleSchedule");
            Thread.currentThread().sleep(10000);
            return "COMPLETE";
        } catch (Exception ex) {
            throw new ScheduleException("Error exec TestSingleSchedule", ex);
        }
    }
}
