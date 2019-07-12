package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.EventLogCleaner;
import ru.intertrust.cm.core.dao.api.EventLogService;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.transaction.Status;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

// отдельный бин сделан для использования в нем пользовательских транзакций
@Stateless
@Local(EventLogCleaner.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class EventLogCleanerImpl implements EventLogCleaner {

    private final static Logger logger = LoggerFactory.getLogger(EventLogCleanerImpl.class);
    
    private static final String USER_EVENT_LOG = "user_event_log";

    private static final String OBJECT_ACCESS_LOG = "object_access_log";

    @Autowired
    @Qualifier("masterNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations masterJdbcTemplate;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private CollectionsDao collectionsDao;

    @Resource
    private EJBContext ejbContext;
    
    @Autowired
    private PersonManagementServiceDao personManagementServiceDao;

    @Autowired
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private GlobalCacheClient globalCacheClient;


    private DomainObject saveUserEventLog(String eventType, Id person, Date date, boolean isSuccess) {
        return saveUserEventLog(eventType, person, null, null, date, isSuccess);
    }
    
    private DomainObject saveUserEventLog(String eventType, Id person, String clientIp, String userId,Date date, boolean isSuccess) {
        DomainObject userEventLog = createDomainObject(USER_EVENT_LOG);
        userEventLog.setString("event_type", eventType);
        userEventLog.setReference("person", person);
        userEventLog.setString("client_ip_address", clientIp);
        userEventLog.setString("user_id", userId);
        userEventLog.setTimestamp("date", date);
        userEventLog.setBoolean("success", isSuccess);

        userEventLog = domainObjectDao.save(userEventLog, getSystemAccessToken());
        return userEventLog;
    }

    private AccessToken getSystemAccessToken() {
        return accessControlService.createSystemAccessToken(this.getClass().getName());
    }

    private DomainObject createDomainObject(String type) {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(type);
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);
        return domainObject;
    }


    @Override
    public void clearEventLogs() throws Exception{
        try {
            boolean b = clearLogs();
            if (!b) {
                recordClearLogsFailure();
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            // создание записи об очистке
            recordClearLogsFailure();
        }
    }

    private void recordClearLogsFailure() throws Exception {
        try {
            ejbContext.getUserTransaction().begin();

            saveUserEventLog(EventLogService.EventLogType.CLEAR_EVENT_LOG.name(), currentUserAccessor.getCurrentUserId(), new Date(), false);

            ejbContext.getUserTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (ejbContext.getUserTransaction().getStatus() == Status.STATUS_ACTIVE || ejbContext.getUserTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                ejbContext.getUserTransaction().rollback();
            }
        }
    }

    
    private boolean clearLogs() throws Exception {
        Id groupId = personManagementServiceDao.getGroupId("InfoSecAuditor");
        Id personId = currentUserAccessor.getCurrentUserId();

        if (!personManagementServiceDao.isPersonInGroup(groupId, personId)){
            throw new IllegalStateException("Not permitted");
        }

        // планировалось сделать запись о том, сколько записей лога удалено, но оказалось не так просто
//        int deleteCount = 0;

        try {
            ejbContext.getUserTransaction().begin();

            Map<String, Object> parameters = new java.util.HashMap();

            // удаление всех записей object_access_log - их может быть очень много, поэтому используем "быстрое" удаление
            masterJdbcTemplate.update("TRUNCATE object_access_log_read,object_access_log_acl,object_access_log_al,object_access_log RESTRICT", parameters); // выполнение запроса TRUNCATE не возвращает количество удаленных записей

            // удаление всех записей user_event_log
            masterJdbcTemplate.update("TRUNCATE user_event_log_read,user_event_log_acl,user_event_log_al,user_event_log RESTRICT", parameters);

            // удаление audit_log

            Collection<DomainObjectTypeConfig> typeConfigs = configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
            List<String> typeNamesToProcess = new ArrayList<>();

            // собираем все типы ДО, по которым можно удалить _al 
            // (выбор только ДО с audit-log="true" и их родителей - НЕ СРАБОТАЛО! т.к. влияют на включение лога еще и связанные объекты (по всей видимости)) 
            for (DomainObjectTypeConfig domainObjectTypeConfig : typeConfigs) {
                if (domainObjectTypeConfig.isTemplate()!=null && !domainObjectTypeConfig.isTemplate() && !configurationExplorer.isAuditLogType(domainObjectTypeConfig.getName())) {
                    typeNamesToProcess.add(domainObjectTypeConfig.getName());
                }
            }

            parameters = new java.util.HashMap();
            String tables = "";
            for (String doType : typeNamesToProcess) {
                String auditLogTableName = DataStructureNamingHelper.getALTableSqlName(doType);
//            deleteCount = deleteCount + masterJdbcTemplate.update("delete from "+auditLogTableName, parameters); // - дает ошибку, т.к. таблицы _al могут быть связаны
                tables = tables + auditLogTableName+ ",";
//            System.out.println("DELETING "+auditLogTableName);
            }
            tables = tables.substring(0, tables.length()-1);
            masterJdbcTemplate.update("TRUNCATE "+tables+" RESTRICT", parameters); // выполнение запроса TRUNCATE не возвращает количество удаленных записей
            
            // создание записи об успешной очистке
            saveUserEventLog(EventLogService.EventLogType.CLEAR_EVENT_LOG.name(), currentUserAccessor.getCurrentUserId(), new Date(), true);
            
            ejbContext.getUserTransaction().commit();
            
            globalCacheClient.clear();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (ejbContext.getUserTransaction().getStatus() == Status.STATUS_ACTIVE || ejbContext.getUserTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                ejbContext.getUserTransaction().rollback();
            }

            return false;
        }
        
//        return deleteCount;
        return true;
    }

}
