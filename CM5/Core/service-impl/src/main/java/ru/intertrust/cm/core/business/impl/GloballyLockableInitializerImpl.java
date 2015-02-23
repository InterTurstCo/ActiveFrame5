package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import org.springframework.transaction.jta.JtaTransactionManager;
import ru.intertrust.cm.core.business.api.Localizer;
import ru.intertrust.cm.core.business.load.ImportReportsData;
import ru.intertrust.cm.core.business.load.ImportSystemData;
import ru.intertrust.cm.core.business.shedule.ScheduleTaskLoader;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.InitializationLockDao;
import ru.intertrust.cm.core.model.FatalException;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.interceptor.Interceptors;
import javax.transaction.*;
import java.util.Random;
import java.util.concurrent.*;

/**
 * {@inheritDoc}
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Local(GloballyLockableInitializer.class)
@Remote(GloballyLockableInitializer.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class GloballyLockableInitializerImpl implements GloballyLockableInitializer, GloballyLockableInitializer.Remote {

    private static final Logger logger = LoggerFactory.getLogger(GloballyLockableInitializerImpl.class);

    private static final long serverId = new Random().nextLong();

    @Autowired private InitializationLockDao initializationLockDao;
    @Autowired private ConfigurationLoader configurationLoader;
    @Autowired private DomainObjectTypeIdCache domainObjectTypeIdCache;
    @Autowired private InitialDataLoader initialDataLoader;
    @Autowired private ImportSystemData importSystemData;
    @Autowired private ImportReportsData importReportsData;
    @Autowired private ScheduleTaskLoader scheduleTaskLoader;
    @Autowired private Localizer localizer;
    @Autowired
    private MigrationService migrationService;

    @Autowired private JtaTransactionManager jtaTransactionManager;

    @Resource private EJBContext ejbContext;

    @Override
    public void init() throws Exception {
        UserTransaction userTransaction = null;
        try {
            if (!initializationLockDao.isInitializationLockTableCreated()) {
                try {
                    initializationLockDao.createInitializationLockTable();
                } catch (DataAccessException e) {
                    logger.error("Error creating initialization_lock table", e);
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e1) {
                        throw new FatalException(e1);
                    }
                    init();
                }

                userTransaction = startTransaction();
                initializationLockDao.createLockRecord(serverId);
                userTransaction.commit();

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(new LockUpdaterTask());

                configurationLoader.load();
                executeInitialLoadingTasks();

                executorService.shutdownNow();
                return;
            }

            userTransaction = startTransaction();

            while(true) {
                if (userTransaction == null) {
                    userTransaction = startTransaction();
                }

                if (!initializationLockDao.isLocked()) {
                    break;
                }

                userTransaction.commit();
                userTransaction = null;

                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    throw new FatalException(e);
                }
            }

            initializationLockDao.lock(serverId);
            userTransaction.commit();

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new LockUpdaterTask());

            configurationLoader.update();
            executeInitialLoadingTasks();

            executorService.shutdownNow();
        } catch (Exception e) {
            if (userTransaction != null) {
                userTransaction.rollback();
            }
            logger.error("GloballyLockableInitializer: failed to initialize application", e);
            throw e;
        } finally{
            try {
                userTransaction = startTransaction();
                initializationLockDao.unlock();
                userTransaction.commit();
            } catch (Exception e) {
                if (userTransaction != null) {
                    userTransaction.rollback();
                }
                logger.error("GloballyLockableInitializer: failed to unlock initialization lock", e);
            }
        }
    }

    private void executeInitialLoadingTasks() throws Exception {
        domainObjectTypeIdCache.build();
        initialDataLoader.load();
        importSystemData.load();
        importReportsData.load();
        scheduleTaskLoader.load();
        localizer.load();
        migrationService.writeMigrationLog(migrationService.getMaxMigrationSequenceNumberFromConfiguration());
    }

    private UserTransaction startTransaction() throws SystemException, NotSupportedException {
        UserTransaction userTransaction = ejbContext.getUserTransaction();
        if (Status.STATUS_ACTIVE != userTransaction.getStatus()) {
            userTransaction.begin();
        }
        return userTransaction;
    }

    private class LockUpdaterTask implements Runnable {

        @Override
        public void run() {
            TransactionManager transactionManager = jtaTransactionManager.getTransactionManager();

            while(!Thread.interrupted()) {
                try {
                    Thread.currentThread().sleep(3000);
                } catch (InterruptedException e) {
                    return;
                }

                try {
                    transactionManager.begin();
                    initializationLockDao.updateLock();
                    transactionManager.commit();
                } catch (Exception e) {
                    try {
                        transactionManager.rollback();
                    } catch (SystemException e1) {
                        logger.error("Failed to rollback transaction", e1);
                    }
                    throw new FatalException(e);
                }
            }
        }
    }

}
