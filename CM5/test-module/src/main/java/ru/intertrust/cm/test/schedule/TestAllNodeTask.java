package ru.intertrust.cm.test.schedule;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.api.schedule.SheduleType;

@ScheduleTask(name = "TestAllNodeTask", minute = "*/1", type = SheduleType.Singleton, allNodes=true)
public class TestAllNodeTask implements ScheduleTaskHandle{
    private static final Logger logger = LoggerFactory.getLogger(TestAllNodeTask.class);
    @Override
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {
        logger.info("Start TestAllNodeTask");
        return null;
    }

}
