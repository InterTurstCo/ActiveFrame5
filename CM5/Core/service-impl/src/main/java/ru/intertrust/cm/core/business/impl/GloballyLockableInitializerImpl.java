package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.load.ImportReportsData;
import ru.intertrust.cm.core.business.load.ImportSystemData;
import ru.intertrust.cm.core.business.shedule.ScheduleTaskLoader;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.InitializationLockDao;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.UnexpectedException;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.interceptor.Interceptors;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.Random;

/**
 * {@inheritDoc}
 */
@Stateless
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

    @Resource
    private EJBContext ejbContext;

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

                configurationLoader.load();
                executeInitialLoadingTasks();

                userTransaction = startTransaction();
                initializationLockDao.unlock();
                userTransaction.commit();

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

            configurationLoader.update();
            executeInitialLoadingTasks();

            userTransaction = startTransaction();
            initializationLockDao.unlock();
            userTransaction.commit();
        } finally {
            try {
                if (userTransaction != null && Status.STATUS_ACTIVE == userTransaction.getStatus()) {
                    userTransaction.commit();
                }
            } catch (Exception e) {
                logger.error("GloballyLockableInitializer: failed to commit transaction", e);
            }
        }
    }

    private void executeInitialLoadingTasks() throws Exception {
        domainObjectTypeIdCache.build();
        initialDataLoader.load();
        importSystemData.load();
        importReportsData.load();
        scheduleTaskLoader.load();
    }

    private UserTransaction startTransaction() throws SystemException, NotSupportedException {
        UserTransaction userTransaction = ejbContext.getUserTransaction();
        if (Status.STATUS_ACTIVE != userTransaction.getStatus()) {
            userTransaction.begin();
        }
        return userTransaction;
    }

}
