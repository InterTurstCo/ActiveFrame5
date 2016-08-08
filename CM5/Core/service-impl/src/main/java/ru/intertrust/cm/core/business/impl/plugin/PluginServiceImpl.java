package ru.intertrust.cm.core.business.impl.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.security.RunAs;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.plugin.AsyncPluginExecutor;
import ru.intertrust.cm.core.business.api.plugin.PluginInfo;
import ru.intertrust.cm.core.business.api.plugin.PluginService;
import ru.intertrust.cm.core.business.api.plugin.PluginStorage;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.model.FatalException;

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
    private CollectionsService collectionsService;    

    @PostConstruct
    public void cleanPluginStatus() {
        //Ищем в таблице со статусами плагинов подвисшие плагины и меняем у них статус
        List<Value> params = new ArrayList<Value>();
        params.add(new ReferenceValue(statusDao.getStatusIdByName("Run")));
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery("select id from plugin_status where status = {0}", params);
        for (IdentifiableObject identifiableObject : collection) {
            domainObjectDao.setStatus(identifiableObject.getId(), statusDao.getStatusIdByName("Sleep"), getSystemAccessToken());
        }
    }

    @Override
    public void init(String contextName, ApplicationContext applicationContext) {
        pluginStorage.init(contextName, applicationContext);
        //Автостарт плагина
        for (PluginInfo pluginInfo : pluginStorage.getPlugins().values()) {
            if (pluginInfo.isAutostart() && pluginInfo.getContextName().equals(contextName)) {
                String result = executePluginInternal(pluginInfo.getClassName(), null, false);
                logger.info("Autostart plugin " + pluginInfo.getClassName() + ". Result: " + result);
            }
        }
    }

    @Override
    public String executePlugin(String id, String param) {
        return executePluginInternal(id, param, true);
    }

    private String executePluginInternal(String pluginId, String param, boolean checkPermissions) {
        if (checkPermissions && !checkPermissions()) {
            throw new FatalException("Current user not permit execute plugin");
        }

        try {
            DomainObject status = getPluginStatus(pluginId);
            if (status.getStatus() == statusDao.getStatusIdByName("Run")) {
                throw new FatalException("Plugin " + pluginId + " is started.");
            }
            status = domainObjectDao.setStatus(status.getId(), statusDao.getStatusIdByName("Run"), getSystemAccessToken());
            status.setTimestamp("last_start", new Date());
            domainObjectDao.save(status, getSystemAccessToken());

            asyncPluginExecutor.execute(pluginId, param);
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
        if (result == null) {
            result = new GenericDomainObject("plugin_status");
            result.setString("plugin_id", pluginId);
            result = domainObjectDao.save(result, getSystemAccessToken());
        }
        return result;
    }

    private boolean checkPermissions() {
        UserGroupGlobalCache userCache = context.getBean(UserGroupGlobalCache.class);
        CurrentUserAccessor currentUserAccessor = context.getBean(CurrentUserAccessor.class);
        return userCache.isPersonSuperUser(currentUserAccessor.getCurrentUserId());
    }
}
