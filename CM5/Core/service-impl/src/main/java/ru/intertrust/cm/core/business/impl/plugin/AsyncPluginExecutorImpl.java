package ru.intertrust.cm.core.business.impl.plugin;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.transaction.Status;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.ClusterManager;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.plugin.AsyncPluginExecutor;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;
import ru.intertrust.cm.core.business.api.plugin.PluginInfo;
import ru.intertrust.cm.core.business.api.plugin.PluginStorage;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;

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

    @Autowired
    protected ClusterManager clusterManager;
    
    @Override
    @Asynchronous
    public Future<Void> execute(String pluginId, String param) {
        try {
            String result = null;
            ejbContext.getUserTransaction().begin();
            PluginInfo pluginInfo = pluginStorage.getPlugins().get(pluginId);
            
            DomainObject status = pluginStorage.getPluginStatus(pluginInfo.getClassName());
            if (status == null){
                status = new GenericDomainObject("plugin_status");
                status.setString("plugin_id", pluginId);
                status = domainObjectDao.save(status, getSystemAccessToken());
            }
            status = domainObjectDao.setStatus(status.getId(), statusDao.getStatusIdByName("Run"), getSystemAccessToken());
            status.setTimestamp("last_start", new Date());
            status.setString("node_id", clusterManager.getNodeId());            
            status = domainObjectDao.save(status, getSystemAccessToken());
            
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
                if (pluginInfo.isTransactional() && ejbContext.getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
                    ejbContext.getUserTransaction().rollback();
                }
                result = ExceptionUtils.getStackTrace(ex);
            }

            ejbContext.getUserTransaction().begin();
            
            status = pluginStorage.getPluginStatus(pluginInfo.getClassName());
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

            ejbContext.getUserTransaction().commit();
            
            return new AsyncResult<Void>(null); 
        } catch (Exception ex) {
            try {
                ejbContext.getUserTransaction().rollback();
            } catch (Exception ignoreEx) {
                logger.warn("", ignoreEx);
            }
            logger.error("Error execute plugin ", ex);
            throw new FatalException("Error execute plugin", ex);
        }
    }

    private AccessToken getSystemAccessToken() {
        return accessControlService.createSystemAccessToken(PluginServiceImpl.class.toString());
    }    

}
