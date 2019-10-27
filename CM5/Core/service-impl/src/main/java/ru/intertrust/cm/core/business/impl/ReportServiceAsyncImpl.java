package ru.intertrust.cm.core.business.impl;

import java.util.Date;
import java.util.Map;
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
import javax.transaction.Status;
import javax.transaction.SystemException;

import org.activiti.engine.impl.cfg.TransactionState;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.ReportServiceAsync;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReportResult;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.model.ReportServiceException;
import ru.intertrust.cm.core.util.CustomSpringBeanAutowiringInterceptor;

@Stateless
@Local(ReportServiceAsync.class)
@Interceptors(CustomSpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ReportServiceAsyncImpl implements ReportServiceAsync {
    private static final Logger logger = LoggerFactory.getLogger(ReportServiceAsyncImpl.class);
    @Autowired
    private ReportService reportService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    protected CollectionsDao collectionsDao;

    @Autowired
    protected DomainObjectDao domainObjectDao;

    @Autowired
    protected AccessControlService accessControlService;

    @Autowired
    protected AttachmentService attachmentService;

    @Autowired
    protected StatusDao statusDao;
    
    @Resource
    private SessionContext sessionContext;

    @Override
    @Asynchronous
    public Future<ReportResult> generateAsync(String name, Map<String, Object> parameters, Id queueId, String ticket) {
        try {
            currentUserAccessor.setTicket(ticket);

            sessionContext.getUserTransaction().begin();
            
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            ReportResult result = null;
            try {
                result = reportService.generate(name, parameters, 1);
                DomainObject queueObject = domainObjectDao.setStatus(queueId, statusDao.getStatusIdByName("Complete"), accessToken);
                queueObject.setTimestamp("finish", new Date());
                queueObject.setReference("result_id", result.getResultId());
                queueObject.setString("file_name", result.getFileName());
                domainObjectDao.save(queueObject, accessToken);
            } catch (Exception ex) {
                logger.error("Error async report generation", ex);
                DomainObject queueObject = domainObjectDao.setStatus(queueId, statusDao.getStatusIdByName("Fault"), accessToken);
                queueObject.setTimestamp("finish", new Date());
                queueObject.setString("error", ExceptionUtils.getStackTrace(ex));
                domainObjectDao.save(queueObject, accessToken);
            }
            sessionContext.getUserTransaction().commit();
            return new AsyncResult<>(result);
        }catch(Exception ex){
            try {
                if (sessionContext.getUserTransaction().getStatus() == Status.STATUS_ACTIVE){
                    sessionContext.getUserTransaction().rollback();
                }
            } catch (IllegalStateException | SecurityException | SystemException e) {
                logger.error("Error rollback trancaction", ex);
            }
            logger.error("Error generate async report", ex);
            throw new ReportServiceException("Error generate async report", ex);
        } finally {
            currentUserAccessor.cleanTicket();
        }
    }

}
