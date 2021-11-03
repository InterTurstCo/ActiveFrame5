package ru.intertrust.cm.core.dao.impl.access;

import java.util.Set;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.DynamicGroupProcessor;
import ru.intertrust.cm.core.dao.access.DynamicGroupService;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

@Stateless(name = "DynamicGroupProcessor")
@Local(DynamicGroupProcessor.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class DynamicGroupProcessorImpl implements DynamicGroupProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DynamicGroupProcessorImpl.class);

    @Autowired
    private DynamicGroupService dynamicGroupService;

    @Autowired
    private PersonManagementServiceDao personManagementService;

    @Resource
    private SessionContext sessionContext;

    @Override
    @Asynchronous
    public Future<Void> calculateDynamicGroupAcync(Set<Id> groupIds) {
        return this.pvCalculateDynamicGroupAcync(groupIds, null);
    }
    
    @Override
    @Asynchronous
    public Future<Void> calculateDynamicGroupAcync(Set<Id> groupIds, Callback callback) {
        return this.pvCalculateDynamicGroupAcync(groupIds, callback);
    }
    
    private Future<Void> pvCalculateDynamicGroupAcync(Set<Id> groupIds, Callback callback) {
        if (callback != null) {
            callback.onBeforeCalculate();
        }
        logger.info("Start recalculate dynamic group package size=" + groupIds.size());
        for (Id groupId : groupIds) {
            try {
                sessionContext.getUserTransaction().begin();
                dynamicGroupService.recalcGroup(groupId);
                sessionContext.getUserTransaction().commit();
            } catch (Exception ex) {
                try {
                    sessionContext.getUserTransaction().rollback();
                } catch (Exception ignoreEx) {
                }
                logger.error("Error calculate group " + groupId, ex);
            }
        }
        logger.info("Finish recalculate dynamic group package");
        if (callback != null) {
            callback.onAfterCalculate();
        }
        return new AsyncResult<Void>(null);
    }

    @Override
    @Asynchronous
    public Future<Void> calculateGroupGroupAcync(Set<Id> groupIds) {
        return this.pvCalculateGroupGroupAcync(groupIds, null);
    }

    @Override
    @Asynchronous
    public Future<Void> calculateGroupGroupAcync(Set<Id> groupIds, Callback callback) {
        return this.pvCalculateGroupGroupAcync(groupIds, callback);
    }
    
    private Future<Void> pvCalculateGroupGroupAcync(Set<Id> groupIds, Callback callback) {
        if (callback != null) {
            callback.onBeforeCalculate();
        }
        logger.info("Start recalculate hierarchy of group package size=" + groupIds.size());
        for (Id groupId : groupIds) {
            try {
                sessionContext.getUserTransaction().begin();
                personManagementService.recalcGroupGroupForGroupAndChildGroups(groupId);
                sessionContext.getUserTransaction().commit();
            } catch (Exception ex) {
                try {
                    sessionContext.getUserTransaction().rollback();
                } catch (Exception ignoreEx) {
                }
                logger.error("Error calculate hierarchy of group " + groupId, ex);
            }
        }
        logger.info("Finish recalculate hierarchy of group package");
        if (callback != null) {
            callback.onAfterCalculate();
        }
        return new AsyncResult<Void>(null);
    }

}
