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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.ClusterManager;
import ru.intertrust.cm.core.business.api.InterserverLockingService;
import ru.intertrust.cm.core.business.api.plugin.PluginService;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskLoader;
import ru.intertrust.cm.core.business.load.ImportReportsData;
import ru.intertrust.cm.core.business.load.ImportSystemData;
import ru.intertrust.cm.core.config.localization.LocalizationLoader;
import ru.intertrust.cm.core.config.server.ServerStatus;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.StatisticsGatherer;
import ru.intertrust.cm.core.dao.api.extension.PostDataLoadApplicationInitializer;
import ru.intertrust.cm.core.dao.api.extension.PreDataLoadApplicationInitializer;
import ru.intertrust.cm.core.model.FatalException;

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

    @Resource private EJBContext ejbContext;
    @EJB private StatisticsGatherer statisticsGatherer;

    @Override
    public void start() throws Exception {
        logger.info("on Start");
        if(clusterManager.isMainServer()){
            if(!interserverLockingService.lock(LOCK_KEY)){
                throw new FatalException("Current server marked as main but could not get lock");
            }
        }
        if (ServerStatus.isEnable()) {
            init();
        }
    }

    @Override
    public void finish() throws Exception {
        logger.info("on Finish");
        if(clusterManager.isMainServer()){
            interserverLockingService.unlock(LOCK_KEY);
        }
    }


    private void init() throws Exception {
       logger.info("Run init");
        // Проверяем является ли сервер мастером. Только мастеру разрешено производить создание и обновление структуры базы.
        if(clusterManager.isMainServer()){
            logger.info("server is main");
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
        }else{
            // заполняем только кэши
            logger.info("server is not main -> just fill cache");

            domainObjectTypeIdCache.build();
            configurationLoader.onLoadComplete();
            //++
            scheduleTaskLoader.load();
            configurationLoader.applyConfigurationExtensionCleaningOutInvalid();

            localizationLoader.load();

            migrationService.writeMigrationLog();
            pluginService.init(ExtensionService.PLATFORM_CONTEXT, context);

        }

        logger.info("Finish init");
    }

    private void executeInitialLoadingTasks() throws Exception {
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
    }


}
