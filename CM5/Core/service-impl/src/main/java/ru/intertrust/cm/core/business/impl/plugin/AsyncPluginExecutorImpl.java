package ru.intertrust.cm.core.business.impl.plugin;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.plugin.AsyncPluginExecutor;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;
import ru.intertrust.cm.core.business.api.plugin.PluginInfo;
import ru.intertrust.cm.core.business.api.plugin.PluginStorage;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.model.FatalException;

@Stateless(name = "AsyncPluginExecutor")
@Local(AsyncPluginExecutor.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class AsyncPluginExecutorImpl implements AsyncPluginExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AsyncPluginExecutorImpl.class);

    
    @Resource
    private EJBContext ejbContext;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private StatusDao statusDao;

    @Autowired
    protected AttachmentService attachmentService;

    @Autowired
    protected PluginStorage pluginStorage;
    
    @Override
    @Asynchronous
    public void execute(String pluginId, String param) {
        try {
            String result = null;
            ejbContext.getUserTransaction().begin();
            PluginInfo pluginInfo = pluginStorage.getPlugins().get(pluginId);
            ejbContext.getUserTransaction().commit();

            logger.info("Execute plugin " + pluginInfo.getClassName());
            
            try {
                
                if (pluginInfo.isTransactional()) {
                    ejbContext.getUserTransaction().begin();
                }
                
                PluginHandler pluginHandler = pluginStorage.getPluginHandler(pluginInfo);
                result = pluginHandler.execute(ejbContext, param);

                if (pluginInfo.isTransactional()) {
                    ejbContext.getUserTransaction().commit();
                }
            } catch (Throwable ex) {
                logger.error("Error execute plugin ", ex);
                if (pluginInfo.isTransactional()) {
                    ejbContext.getUserTransaction().rollback();
                }
                result = ex.toString();
            }

            if (pluginInfo.isTransactional()) {
                ejbContext.getUserTransaction().begin();
            }
            
            DomainObject status = getPluginStatus(pluginInfo.getClassName());
            status = domainObjectDao.setStatus(status.getId(), statusDao.getStatusIdByName("Sleep"), getSystemAccessToken());
            status.setTimestamp("last_finish", new Date());
            domainObjectDao.save(status, getSystemAccessToken());

            //Удаляем все предыдущие логи
            List<DomainObject> logs = domainObjectDao.findLinkedDomainObjects(status.getId(), "execution_log", "plugin_status", getSystemAccessToken());
            for (DomainObject domainObject : logs) {
                domainObjectDao.delete(domainObject.getId(), getSystemAccessToken());
            }
            
            //Создаем новый лог
            DomainObject attachment = attachmentService.createAttachmentDomainObjectFor(status.getId(), "execution_log");
            attachment.setString("Name", pluginInfo.getClassName() + "_execution.log");
            ByteArrayInputStream bis = new ByteArrayInputStream(result.getBytes());
            DirectRemoteInputStream directRemoteInputStream = new DirectRemoteInputStream(bis, false);
            attachmentService.saveAttachment(directRemoteInputStream, attachment);
            if (pluginInfo.isTransactional()) {
                ejbContext.getUserTransaction().commit();
            }
        } catch (Exception ex) {
            try {
                ejbContext.getUserTransaction().rollback();
            } catch (Exception ignoreEx) {
            }
            logger.error("Error execute plugin ", ex);
            throw new FatalException("Error execute plugin", ex);
        }
    }

    private AccessToken getSystemAccessToken() {
        return accessControlService.createSystemAccessToken(PluginServiceImpl.class.toString());
    }
    
    private DomainObject getPluginStatus(String pluginId) {
        Map<String, Value> key = new HashMap<String, Value>();
        key.put("plugin_id", new StringValue(pluginId));
        return domainObjectDao.findByUniqueKey("plugin_status", key, getSystemAccessToken());
    }  

}
