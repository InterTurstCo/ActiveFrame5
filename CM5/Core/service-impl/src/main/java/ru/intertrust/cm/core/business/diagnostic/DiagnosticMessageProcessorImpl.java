package ru.intertrust.cm.core.business.diagnostic;

import java.util.Collections;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.ClusterManager;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.InterserverLockingService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.globalcache.CheckData;
import ru.intertrust.cm.core.business.api.dto.globalcache.CheckLockData;
import ru.intertrust.cm.core.business.api.dto.globalcache.DiagnosticData;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.CustomSpringBeanAutowiringInterceptor;

@Stateless(name = "DiagnosticMessageProcessor")
@Local(DiagnosticMessageProcessor.class)
@Interceptors(CustomSpringBeanAutowiringInterceptor.class)
@RunAs("system")
@TransactionManagement(TransactionManagementType.BEAN)
public class DiagnosticMessageProcessorImpl implements DiagnosticMessageProcessor {
    final static Logger logger = LoggerFactory.getLogger(DiagnosticMessageProcessorImpl.class);
    
    @Autowired
    private CrudService crudService;

    @Autowired
    private CollectionsService collectionService;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private InterserverLockingService lockingService;
    
    @Resource
    private EJBContext context;

    @Override
    public void processDiagnosticData(DiagnosticData diagnosticData) {
        if (diagnosticData instanceof CheckData) {

            IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(
                    "select id from resources where name = {0}",
                    Collections.singletonList(new StringValue(clusterManager.getNodeId())));

            DomainObject checkResource = crudService.find(collection.get(0).getId());
            checkResource.setString("string_value", clusterManager.getNodeId());
            crudService.save(checkResource);
        } else if (diagnosticData instanceof CheckLockData) {
            try {
                CheckLockData checkLockData = (CheckLockData) diagnosticData;
                logger.info("Process diagnostic message checkLock, resource id {}", checkLockData.getLockResourceId());

                DomainObject checkResource = crudService.find(checkLockData.getLockResourceId());

                while (!lockingService.lock(CheckClobalCacheInvalidation.TEST_RESOURCE)) {
                    logger.info("Lock fail. Wait Until Not Locked");
                    lockingService.waitUntilNotLocked(CheckClobalCacheInvalidation.TEST_RESOURCE);
                }
                logger.info("Lock success");
                
                // Транзакцию нужно открывать только после блокировки
                context.getUserTransaction().begin();
                checkResource = crudService.find(checkLockData.getLockResourceId());
                checkResource.setString("string_value", checkResource.getString("string_value") + " " + clusterManager.getNodeId());
                crudService.save(checkResource);
                context.getUserTransaction().commit();
            } catch (Exception ex) {
                try {
                    context.getUserTransaction().rollback();
                }catch (Exception ignoreEx) {
                }
                throw new FatalException("Error process diagnostic message", ex);
            } finally {
                lockingService.unlock(CheckClobalCacheInvalidation.TEST_RESOURCE);
                logger.info("Unlock success");
            }
        }
    }
}
