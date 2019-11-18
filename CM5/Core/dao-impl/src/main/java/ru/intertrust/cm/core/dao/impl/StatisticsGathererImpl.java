package ru.intertrust.cm.core.dao.impl;

import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.StatisticsGatherer;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

/**
 * @author Denis Mitavskiy
 *         Date: 27.07.2016
 *         Time: 15:39
 */
@Singleton(name = "StatisticsGatherer")
@TransactionManagement(TransactionManagementType.CONTAINER)
@Local(StatisticsGatherer.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class StatisticsGathererImpl implements StatisticsGatherer {
    private final static Logger logger = LoggerFactory.getLogger(StatisticsGathererImpl.class);
    private static final Object LOCK = new Object();

    @Autowired
    private DataStructureDao dataStructureDao;

    @Override
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void gatherStatistics() {
        logger.info("Database statistics gathering - wait for LOCK");
        synchronized (LOCK) {
            logger.warn("Database statistics gathering started");
            final long t1 = System.currentTimeMillis();
            dataStructureDao.gatherStatistics();
            final long t2 = System.currentTimeMillis();
            logger.warn("Database statistics gathering finished in " + (t2 - t1) / 1000 + " seconds");
        }
    }
}
