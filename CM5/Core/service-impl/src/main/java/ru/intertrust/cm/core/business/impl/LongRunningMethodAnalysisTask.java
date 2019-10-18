package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.impl.profiling.RAMUsageTracker;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import javax.annotation.PostConstruct;
import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

/**
 * Периодическое задание, анализирующее работающие потоки и методы и информирующее о тех, которые работают долго
 */
@ScheduleTask(name = "LongRunningMethodAnalysisTask", minute = "*/1", allNodes = true, active = true)
public class LongRunningMethodAnalysisTask implements ScheduleTaskHandle {
    public static final String AF5_MONITORING_DAEMON = "AF5-Monitoring-Daemon";
    public static final String AF5_DB_CHECK_DAEMON = "AF5-DBCheck-Daemon";
    private static final Logger logger = LoggerFactory.getLogger(LongRunningMethodAnalysisTask.class);

    @org.springframework.beans.factory.annotation.Value("${ram.usage.tracker.frequency:5}")
    private int frequency;
    @org.springframework.beans.factory.annotation.Value("${datasource.master:java:jboss/datasources/CM5}")
    private String datasourceJndiName;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RAMUsageTracker ramUsageTracker;

    @Autowired
    private RAMUsageTracker frequentRamUsageTracker;
    private RAMUsageThread ramUsageThread;
    private DBCheckThread dbCheckThread;

    @Autowired
    private DomainObjectDao dao;

    @Autowired
    private AccessControlService accessControlService;

    @PostConstruct
    void init() {
        ramUsageTracker.setLoggers(null, null, logger);
        ramUsageTracker.setSuspiciousTotalHeapDeltaBytesPerMinute(0);

        frequentRamUsageTracker.printHead();
    }

    @Override
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {
        ramUsageTracker.track(null);
        if (ramUsageThread == null) {
            DBCheckThread dbCheckThreadLocalCopy = dbCheckThread;
            if (dbCheckThreadLocalCopy == null) {
                dbCheckThread = new DBCheckThread();
                dbCheckThreadLocalCopy = dbCheckThread;
                dbCheckThread.start();
            }
            final long waitFor = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < waitFor) {
                if (!dbCheckThreadLocalCopy.getDbCheck().isTaskActive()) {
                    Thread.sleep(1000);
                }
            }
            if (dbCheckThreadLocalCopy.getDbCheck().isTaskActive()) {
                ramUsageThread = new RAMUsageThread();
                ramUsageThread.start();
            }
        }
        return "Done";
    }

    private DBCheck checkDatabase() {
        try {
            final long t1 = System.currentTimeMillis();
            try(Connection con = ((DataSource) new InitialContext().lookup(datasourceJndiName)).getConnection()) {
                final long t2 = System.currentTimeMillis();
                try(Statement stm = con.createStatement()) {
                    final long t3 = System.currentTimeMillis();
                    try(ResultSet rs = stm.executeQuery("SELECT active FROM schedule WHERE name = 'LongRunningMethodAnalysisTask'")) {
                        final long t4 = System.currentTimeMillis();
                        final boolean next = rs.next();
                        boolean active;
                        if (!next) {
                            active = false;
                        } else {
                            active = 1 == rs.getInt(1);
                        }
                        return new DBCheck(active, t2 - t1, t4 - t3);
                    }
                }
            }
        } catch (NamingException | SQLException e) {
            logger.error("Exception while checking DB Connectivity", e);
        }
        return DBCheck.INACTIVE;
    }

    private final class RAMUsageThread extends Thread {
        private long frequencyMillies = TimeUnit.MILLISECONDS.convert(frequency, TimeUnit.SECONDS);
        private long activityCheckMillies = TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS);
        private long nextTaskActivityCheck = System.currentTimeMillis() + activityCheckMillies;

        public RAMUsageThread() {
            setDaemon(true);
            setName(AF5_MONITORING_DAEMON);
        }

        @Override
        public void run() {
            while (true) {
                DBCheck dbCheck = dbCheckThread == null ? DBCheck.INACTIVE : dbCheckThread.getDbCheck();
                if (!dbCheck.isTaskActive()) {
                    break;
                }
                long t1 = System.currentTimeMillis();
                frequentRamUsageTracker.track(dbCheck);
                long t2 = System.currentTimeMillis();
                long toSleep = frequencyMillies - (t2 - t1);
                if (toSleep > 0) {
                    try {
                        Thread.sleep(toSleep);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.error("Exception", e);
                    }
                }
            }
            ramUsageThread = null;
            dbCheckThread = null;
            logger.warn("All tracking threads stopped");
        }
    }

    private final class DBCheckThread extends Thread {
        private volatile DBCheck dbCheck = DBCheck.INACTIVE;

        public DBCheckThread() {
            setDaemon(true);
            setName(AF5_DB_CHECK_DAEMON);
        }

        @Override
        public void run() {
            while (true) {
                dbCheck = new DBCheck(dbCheck.isTaskActive(), null, null);
                dbCheck = checkDatabase();
                if (!dbCheck.isTaskActive()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Exception", e);
                }
            }
            logger.warn("DB Check Thread stopped");
        }

        public DBCheck getDbCheck() {
            return dbCheck;
        }
    }

    public static class DBCheck {
        private static final DBCheck INACTIVE = new DBCheck(false, -1L, -1L);

        private volatile boolean taskActive;
        private volatile Long getConnectionTime;
        private volatile Long queryTime;

        public DBCheck(boolean taskActive, Long getConnectionTime, Long queryTime) {
            this.taskActive = taskActive;
            this.getConnectionTime = getConnectionTime;
            this.queryTime = queryTime;
        }

        public boolean isTaskActive() {
            return taskActive;
        }

        public Long getGetConnectionTime() {
            return getConnectionTime;
        }

        public Long getQueryTime() {
            return queryTime;
        }
    }
}
