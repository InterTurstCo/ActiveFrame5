package ru.intertrust.cm.test.schedule;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.model.ScheduleException;

@ScheduleTask(name = "TestSingleSchedule", minute = "*/1")
public class TestSingleSchedule implements ScheduleTaskHandle {

    @Override
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) {
        try {
            System.out.println("Run TestSingleSchedule");
            Thread.currentThread().sleep(5000);
            System.out.println("End Run TestSingleSchedule");
            return "COMPLETE";
        } catch (Exception ex) {
            throw new ScheduleException("Error exec TestSingleSchedule", ex);
        }
    }
}
