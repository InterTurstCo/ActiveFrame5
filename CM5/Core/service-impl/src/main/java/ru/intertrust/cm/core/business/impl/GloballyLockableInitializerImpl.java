package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import org.springframework.transaction.jta.JtaTransactionManager;
import ru.intertrust.cm.core.business.api.ClusterManager;
import ru.intertrust.cm.core.business.api.InterserverLockingService;
import ru.intertrust.cm.core.business.api.plugin.PluginService;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskLoader;
import ru.intertrust.cm.core.business.load.ImportReportsData;
import ru.intertrust.cm.core.business.load.ImportSystemData;
import ru.intertrust.cm.core.config.localization.LocalizationLoader;
import ru.intertrust.cm.core.dao.api.*;
import ru.intertrust.cm.core.dao.api.extension.PostDataLoadApplicationInitializer;
import ru.intertrust.cm.core.dao.api.extension.PreDataLoadApplicationInitializer;
import ru.intertrust.cm.core.model.FatalException;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.*;
import javax.interceptor.Interceptors;
import javax.transaction.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private static final long serverId = new Random().nextLong();
    private static final String LOCK_KEY = "GloballyLockableInitializer_LOCK_KEY";

   // @Autowired private InitializationLockDao initializationLockDao;
    @Autowired private DataStructureDao dataStructureDao;
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

    @Autowired private JtaTransactionManager jtaTransactionManager;

    @Resource private EJBContext ejbContext;
    @EJB private StatisticsGatherer statisticsGatherer;

    @Override
    public void start() throws Exception {
        if(clusterManager.isMainServer()){
            if(!interserverLockingService.lock(LOCK_KEY)){
                throw new FatalException("Текущий сервер помечен как маснет но не смог получить блокировку");
            }
        }
        init();
    }

    @Override
    public void finish() throws Exception {
        if(clusterManager.isMainServer()){
            interserverLockingService.unlock(LOCK_KEY);
        }
    }


    private void init() throws Exception {
        UserTransaction userTransaction = null;
        // Проверяем является ли сервер мастером. Только мастеру разрешено производить создание и обновление структуры базы.
        if(clusterManager.isMainServer()){
            // если нет конфигукации предполагаем что необходимо создать все структуру базы.
            if(!configurationLoader.isConfigurationTableExist()){
                configurationLoader.load();
                executeInitialLoadingTasks();
                statisticsGatherer.gatherStatistics();
            }else{
                domainObjectTypeIdCache.build();
                configurationLoader.update();
                executeInitialLoadingTasks();
            }
        }else{
            // заполняем только кэши

            domainObjectTypeIdCache.build();
            configurationLoader.onLoadComplete();
            //++
            scheduleTaskLoader.load();
            configurationLoader.applyConfigurationExtensionCleaningOutInvalid();

            localizationLoader.load();

            migrationService.writeMigrationLog();
            pluginService.init(ExtensionService.PLATFORM_CONTEXT, context);

        }

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
