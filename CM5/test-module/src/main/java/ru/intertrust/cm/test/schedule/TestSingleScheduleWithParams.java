package ru.intertrust.cm.test.schedule;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.model.ScheduleException;

@ScheduleTask(name = "TestSingleScheduleWithParams", minute = "*/1", configClass = TestSheduleDefaultParameter.class)
public class TestSingleScheduleWithParams implements ScheduleTaskHandle {

    @Override
    public String execute(ScheduleTaskParameters parameters) {
        try {
            TestScheduleParameters testScheduleParameters = (TestScheduleParameters)parameters;
            
            System.out.println("Run TestSingleScheduleWithParams");
            Thread.currentThread().sleep(testScheduleParameters.getWorkTime());
            return testScheduleParameters.getResult();
        } catch (Exception ex) {
            throw new ScheduleException("Error exec TestSingleSchedule", ex);
        }
    }

}
