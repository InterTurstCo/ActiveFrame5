package ru.intertrust.cm.core.business.impl;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.ClusterManager;
import ru.intertrust.cm.core.business.api.InterserverLockingService;
import ru.intertrust.cm.core.business.api.plugin.PluginService;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskLoader;
import ru.intertrust.cm.core.business.load.ImportReportsData;
import ru.intertrust.cm.core.business.load.ImportSystemData;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.localization.LocalizationLoader;
import ru.intertrust.cm.core.config.server.ServerStatus;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.StatisticsGatherer;
import ru.intertrust.cm.core.dao.api.clusterlock.ClusteredLockDao;
import ru.intertrust.cm.core.dao.api.extension.NotManagerDataLoadApplicationInitializer;
import ru.intertrust.cm.core.dao.api.extension.PostDataLoadApplicationInitializer;
import ru.intertrust.cm.core.dao.api.extension.PreDataLoadApplicationInitializer;
import ru.intertrust.cm.core.dao.impl.DatabaseDaoFactory;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.process.DeployModuleProcesses;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

/**
 * {@inheritDoc}
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Local(GloballyLockableInitializer.class)
@Remote(GloballyLockableInitializer.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
@RunAs("system")
public class GloballyLockableInitializerImpl implements GloballyLockableInitializer, GloballyLockableInitializer.Remote {

    private static final Logger logger = LoggerFactory.getLogger(GloballyLockableInitializerImpl.class);

    private static final String LOCK_KEY = "GloballyLockableInitializer_LOCK_KEY";

    @Autowired private ConfigurationLoader configurationLoader;
    @Autowired private ConfigurationExplorer configurationExplorer;
    @Autowired private DomainObjectTypeIdCache domainObjectTypeIdCache;
    @Autowired private InitialDataLoader initialDataLoader;
    @Autowired private ImportSystemData importSystemData;
    @Autowired private ImportReportsData importReportsData;
    @Autowired private ScheduleTaskLoader scheduleTaskLoader;
    @Autowired private LocalizationLoader localizationLoader;
    @Autowired private MigrationService migrationService;
    @Autowired private ExtensionService extensionService;
    @Autowired private PluginService pluginService;
    @Autowired private ApplicationContext context;
    @Autowired private ClusterManager clusterManager;
    @Autowired private InterserverLockingService interserverLockingService;
    @Autowired private DeployModuleProcesses deployModuleProcesses;
    @Autowired private ClusteredLockDao clusteredLockDao;
    @Autowired private DatabaseDaoFactory dbDaoFactory;
    @Value("${scheme.transaction.timeout:60}") private int schemeTransactionTimeout = 60; // minutes
    @Value("${scheme.transaction.disable:false}") private boolean isSchemeTransactionDisable = false;

    @Resource private EJBContext ejbContext;
    @EJB private StatisticsGatherer statisticsGatherer;
    
    // CMFIVE-25095 Переменная, где запоминаем результат первого вызова clusterManager.isMainServer(), чтобы использовать его на всем протяжении инициализации
    private boolean isMainServer = false;

    @Override
    public void start() throws Exception {
        logger.info("on Start");
        if(clusterManager.isMainServer()){
            if(!interserverLockingService.lock(LOCK_KEY)){
                throw new FatalException("Current server marked as main but could not get lock");
            }
            isMainServer = true;
        }else{
            interserverLockingService.waitUntilNotLocked(LOCK_KEY);
        }
        if (ServerStatus.isEnable()) {
            init();
        }
    }

    @Override
    public void finish() throws Exception {
        logger.info("on Finish");
        if(isMainServer){
            interserverLockingService.unlock(LOCK_KEY);
        }
    }

    private void init() throws Exception {
       logger.info("Run init");
        configurationExplorer.validate();
        // Проверяем является ли сервер мастером. Только мастеру разрешено производить создание и обновление структуры базы.
        if(isMainServer){
            logger.info("server is main");
            UserTransaction tx = null;
            if (!this.isSchemeTransactionDisable && this.dbDaoFactory.isDdlTransactionsSupports()) {
                logger.info("DDL transactions supports");
                tx = this.ejbContext.getUserTransaction();
                tx.setTransactionTimeout(this.schemeTransactionTimeout * 60);
                tx.begin();
            }
            try {
                // если нет конфигукации предполагаем что необходимо создать все структуру базы.
                if(!configurationLoader.isConfigurationTableExist()){
                    logger.info("no database-> create database structure");
                    configurationLoader.load();
                    executeInitialLoadingTasks();
                    statisticsGatherer.gatherStatistics();
                }else{
                    logger.info("database exist-> update database structure");
                    domainObjectTypeIdCache.build();
                    configurationLoader.update();
                    executeInitialLoadingTasks();
                }
            } catch (final Throwable e) {
                if (tx != null) {
                    try {
                        tx.rollback();
                    } catch (final Throwable e2) {
                        e.addSuppressed(e2);
                    }
                }
                throw e;
            }
            if (tx != null) {
                if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                    tx.rollback();
                } else {
                    try {
                        tx.commit();
                    } catch (final Throwable e) {
                        try {
                            tx.rollback();
                        } catch (final Throwable e2) {
                            e.addSuppressed(e2);
                        }
                        throw e;
                    }
                }
            }
        }else{
            // заполняем только кэши
            logger.info("server is not main -> just fill cache");

            domainObjectTypeIdCache.build();
            configurationLoader.onLoadComplete();
            extensionService.getExtentionPoint(NotManagerDataLoadApplicationInitializer.class, null).notManagerinitialize();
            //++
            scheduleTaskLoader.load();
            configurationLoader.applyConfigurationExtensionCleaningOutInvalid();

            localizationLoader.load();

            migrationService.writeMigrationLog();
            pluginService.init(ExtensionService.PLATFORM_CONTEXT, context);

        }

        logger.info("Finish init");
    }

    /**
     * Метод запускается только на ведущем сервере
     * @throws Exception
     */
    private void executeInitialLoadingTasks() throws Exception {

        clusteredLockDao.init();

        domainObjectTypeIdCache.build();

        initialDataLoader.load();
        extensionService.getExtentionPoint(PreDataLoadApplicationInitializer.class, null).initialize();
        importSystemData.load();
        importReportsData.load();
        extensionService.getExtentionPoint(PostDataLoadApplicationInitializer.class, null).initialize();

        scheduleTaskLoader.load();
        configurationLoader.applyConfigurationExtensionCleaningOutInvalid();

        localizationLoader.load();

        migrationService.writeMigrationLog();
        pluginService.init(ExtensionService.PLATFORM_CONTEXT, context);
        
        deployModuleProcesses.load();
    }


}
