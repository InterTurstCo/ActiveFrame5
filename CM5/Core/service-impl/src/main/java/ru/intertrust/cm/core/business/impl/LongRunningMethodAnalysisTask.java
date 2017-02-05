package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.impl.profiling.RAMUsageTracker;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import javax.annotation.PostConstruct;
import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Периодическое задание, анализирующее работающие потоки и методы и информирующее о тех, которые работают долго
 */
@ScheduleTask(name = "LongRunningMethodAnalysisTask", minute = "*/1", allNodes = true)
public class LongRunningMethodAnalysisTask implements ScheduleTaskHandle {
    private static final Logger logger = LoggerFactory.getLogger(LongRunningMethodAnalysisTask.class);

    @org.springframework.beans.factory.annotation.Value("${ram.usage.tracker.frequency:1}")
    private int frequency;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RAMUsageTracker ramUsageTracker;

    @Autowired
    private RAMUsageTracker frequentRamUsageTracker;
    private RAMUsageThread ramUsageThread;

    @Autowired
    private DomainObjectDao dao;

    @Autowired
    private AccessControlService accessControlService;

    @PostConstruct
    void init() {
        ramUsageTracker.setLoggers(null, null, logger);
        ramUsageTracker.setSuspiciousTotalHeapDeltaBytesPerMinute(0);
    }

    @Override
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {
        ramUsageTracker.track();
        if (ramUsageThread == null) {
            ramUsageThread = new RAMUsageThread();
            ramUsageThread.start();
        }
        return "Done";
    }


    private final class RAMUsageThread extends Thread {
        private long frequencyMillies = TimeUnit.MILLISECONDS.convert(frequency, TimeUnit.SECONDS);
        private long activityCheckMillies = TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS);
        private long nextTaskActivityCheck = System.currentTimeMillis() + activityCheckMillies;

        public RAMUsageThread() {
            setDaemon(true);
        }

        @Override
        public void run() {
            while (isTaskActive()) {
                long t1 = System.currentTimeMillis();
                frequentRamUsageTracker.track();
                long t2 = System.currentTimeMillis();
                long toSleep = frequencyMillies - (t2 - t1);
                if (toSleep > 0) {
                    try {
                        Thread.sleep(toSleep);
                    } catch (InterruptedException e) {
                        logger.error("Exception", e);
                    }
                }
            }
            ramUsageThread = null;
        }

        private boolean isTaskActive() {
            final long curTime = System.currentTimeMillis();
            if (curTime < nextTaskActivityCheck) {
                return true;
            }
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            final DomainObject task = dao.findByUniqueKey("schedule", Collections.singletonMap("name", (Value) new StringValue("LongRunningMethodAnalysisTask")), accessToken);
            nextTaskActivityCheck = curTime + activityCheckMillies;
            return task.getBoolean("active");
        }
    }
}
