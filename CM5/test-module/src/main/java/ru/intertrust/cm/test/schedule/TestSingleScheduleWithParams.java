package ru.intertrust.cm.test.schedule;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.model.ScheduleException;
import javax.ejb.SessionContext;

@ScheduleTask(name = "TestSingleScheduleWithParams", minute = "*/1", configClass = TestSheduleDefaultParameter.class)
public class TestSingleScheduleWithParams implements ScheduleTaskHandle {

    @Override
    public String execute(SessionContext sessionContext, ScheduleTaskParameters parameters) {
        try {
            TestScheduleParameters testScheduleParameters = (TestScheduleParameters)parameters;
            
            System.out.println("Run TestSingleScheduleWithParams");
            Thread.currentThread().sleep(testScheduleParameters.getWorkTime() * 1000);
            System.out.println("End Run TestSingleScheduleWithParams");
            return testScheduleParameters.getResult();
        } catch (Exception ex) {
            throw new ScheduleException("Error exec TestSingleSchedule", ex);
        }
    }

}
