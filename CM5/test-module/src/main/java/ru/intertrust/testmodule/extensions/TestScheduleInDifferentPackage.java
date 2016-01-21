package ru.intertrust.testmodule.extensions;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

@ScheduleTask(name = "TestScheduleInDifferentPackage", minute = "*/1")
public class TestScheduleInDifferentPackage implements ScheduleTaskHandle{
    private Logger logger = LoggerFactory.getLogger(TestScheduleInDifferentPackage.class);

    @Override
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {
        logger.info("start TestScheduleInDifferentPackage");
        return "ОК";
    }

}
