package ru.intertrust.cm.core.business.impl.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.ClusterManager;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.plugin.AsyncPluginExecutor;
import ru.intertrust.cm.core.business.api.plugin.PluginInfo;
import ru.intertrust.cm.core.business.api.plugin.PluginService;
import ru.intertrust.cm.core.business.api.plugin.PluginStorage;
import ru.intertrust.cm.core.business.impl.ConfigurationLoader;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

@Stateless(name = "PluginService")
@Local(PluginService.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class PluginServiceImpl implements PluginService {

    private static final Logger logger = LoggerFactory.getLogger(PluginServiceImpl.class);

    @Autowired
    private PluginStorage pluginStorage;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private AsyncPluginExecutor asyncPluginExecutor;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private StatusDao statusDao;

    @Autowired
    private CollectionsDao collectionsService;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ConfigurationLoader configurationLoader;

    private static Map<String, Future> futures = new HashMap<String, Future>();

    public void cleanPluginStatus() {
        Set<String> workNodes = clusterManager.getNodeIds();

        //Ищем в таблице со статусами плагинов подвисшие плагины и меняем у них статус
        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue("Run"));

        String query = "select ps.id, ps.node_id from plugin_status ps ";
        query += "join status s on s.id = ps.status ";
        query += "where s.name = {0}";

        IdentifiableObjectCollection collection =
                collectionsService.findCollectionByQuery(query, params, 0, 0, getSystemAccessToken());
        for (IdentifiableObject identifiableObject : collection) {
            //У тех плагинов которые запущены на остановленных нодах меняем статус 
            if (!workNodes.contains(identifiableObject.getString("node_id"))) {
                domainObjectDao.setStatus(identifiableObject.getId(), statusDao.getStatusIdByName("Sleep"), getSystemAccessToken());
                logger.info("Set Sleep plugin {} status. It started on stopped node {}", identifiableObject.getId(), identifiableObject.getString("node_id"));
            }
        }
    }

    @Override
    @Asynchronous
    public void init(String contextName, ApplicationContext applicationContext) {
        logger.info("Start init plugin service");
        pluginStorage.init(contextName, applicationContext);
        //Автостарт плагина
        for (PluginInfo pluginInfo : pluginStorage.getPlugins().values()) {
            if (pluginInfo.isAutostart() && pluginInfo.getContextName().equals(contextName)) {
                String result = executePluginInternal(pluginInfo.getClassName(), null, false);
                logger.info("Autostart plugin " + pluginInfo.getClassName() + ". Result: " + result);
            }
        }
        logger.info("End init plugin service");
    }

    @Override
    public String executePlugin(String id, String param) {
        return executePluginInternal(id, param, true);
    }

    /**
     * Периодичесмкая задача проверки статусов плагинов.
     */
    @Schedule(dayOfWeek = "*", hour = "*", minute = "*", second = "30", year = "*", persistent = false)
    public void backgroundProcessing() {
        try {
            if (configurationLoader.isConfigurationLoaded()) {
                //Ищем в таблице со статусами плагинов остановленные плагины отправляем процессу уведомление
                List<Value> params = new ArrayList<Value>();
                params.add(new ReferenceValue(statusDao.getStatusIdByName("Terminate")));
                params.add(new StringValue(clusterManager.getNodeId()));
                IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(
                        "select id, plugin_id from plugin_status where status = {0} and node_id = {1}", params, 0, 0, getSystemAccessToken());
                for (IdentifiableObject identifiableObject : collection) {
                    synchronized (futures) {
                        Future future = futures.get(identifiableObject.getString("plugin_id"));
                        if (future != null) {
                            future.cancel(true);
                            logger.info("Send cancal event for plugin {} execution process", identifiableObject.getString("plugin_id"));
                            futures.remove(identifiableObject.getString("plugin_id"));
                        }
                    }
                }

                //Очищаем таблицу с future от завершенных плагинов
                synchronized (futures) {
                    Set<String> pluginIds = new HashSet<String>();
                    for (String pluginId : futures.keySet()) {
                        if (futures.get(pluginId).isDone()) {
                            pluginIds.add(pluginId);
                        }
                    }

                    for (String pluginId : pluginIds) {
                        futures.remove(pluginId);
                    }
                }
                
                //Ищем подвисшие плагины
                cleanPluginStatus();
            }

        } catch (Exception ex) {
            logger.info("Error on perodic check plugin status", ex);
        }
    }

    private String executePluginInternal(String pluginId, String param, boolean checkPermissions) {
        if (checkPermissions && !checkPermissions()) {
            throw new FatalException("Current user not permit execute plugin");
        }

        try {
            DomainObject status = getPluginStatus(pluginId);
            if (status != null && status.getStatus() == statusDao.getStatusIdByName("Run")) {
                throw new FatalException("Plugin " + pluginId + " is started.");
            }

            Future future = asyncPluginExecutor.execute(pluginId, param);

            synchronized (futures) {
                futures.put(pluginId, future);
            }

            return "Started";
        } catch (FatalException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new FatalException("Error execute plugin.", ex);
        }
    }

    private AccessToken getSystemAccessToken() {
        return accessControlService.createSystemAccessToken(PluginServiceImpl.class.toString());
    }

    @Autowired
    private AccessControlService accessControlService;

    private DomainObject getPluginStatus(String pluginId) {
        Map<String, Value> key = new HashMap<String, Value>();
        key.put("plugin_id", new StringValue(pluginId));
        DomainObject result = domainObjectDao.findByUniqueKey("plugin_status", key, getSystemAccessToken());
        return result;
    }

    private boolean checkPermissions() {
        UserGroupGlobalCache userCache = context.getBean(UserGroupGlobalCache.class);
        CurrentUserAccessor currentUserAccessor = context.getBean(CurrentUserAccessor.class);
        return userCache.isPersonSuperUser(currentUserAccessor.getCurrentUserId());
    }

    @Override
    public void terminateExecution(String id) {
        DomainObject pluginStatus = getPluginStatus(id);
        domainObjectDao.setStatus(pluginStatus.getId(), statusDao.getStatusIdByName("Terminate"), getSystemAccessToken());
    }
}
