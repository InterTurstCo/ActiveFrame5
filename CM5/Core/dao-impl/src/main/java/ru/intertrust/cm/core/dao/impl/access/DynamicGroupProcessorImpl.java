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
import ru.intertrust.cm.core.util.CustomSpringBeanAutowiringInterceptor;

@Stateless(name = "DynamicGroupProcessor")
@Local(DynamicGroupProcessor.class)
@Interceptors(CustomSpringBeanAutowiringInterceptor.class)
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
        return new AsyncResult<Void>(null);
    }

    @Override
    @Asynchronous
    public Future<Void> calculateGroupGroupAcync(Set<Id> groupIds) {
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
        return new AsyncResult<Void>(null);
    }

}
