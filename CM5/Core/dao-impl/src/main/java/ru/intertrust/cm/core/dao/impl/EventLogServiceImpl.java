package ru.intertrust.cm.core.dao.impl;

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
import ru.intertrust.cm.core.config.eventlog.EventLogsConfig;
import ru.intertrust.cm.core.config.eventlog.LogDomainObjectAccessConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.EventLogService;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Stateless
@Local(EventLogService.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class EventLogServiceImpl implements EventLogService {

    private static final String USER_EVENT_LOG = "user_event_log";

    private static final String OBJECT_ACCESS_LOG = "object_access_log";

    @Autowired
    @Qualifier("masterNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations masterJdbcTemplate;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private CollectionsDao collectionsDao;

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

    @EJB
    private EventLogService newTransactionService;

    @Override
    public void logLogInEvent(String login, String ip, boolean success) {
        if (!isLoginEventEnabled()) {
            return;
        }
        newTransactionService.doLogLogInEvent(login, ip, success);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void doLogLogInEvent(String login, String ip, boolean success) {
        UserEventLogBuilder userEventLogBuilder = createLoginEventLogBuilder(login, ip, success);
        saveUserEventLog(userEventLogBuilder);
    }

    private boolean isLoginEventEnabled() {
        EventLogsConfig eventLogsConfiguration = configurationExplorer.getEventLogsConfiguration();
        if (eventLogsConfiguration != null && eventLogsConfiguration.getLoginConfig() != null) {
            return eventLogsConfiguration.getLoginConfig().isEnable();
        }
        return false;
    }

    @Override
    public void logLogOutEvent(String login) {
        if (!isLogoutEventEnabled()) {
            return;
        }

        newTransactionService.doLogLogOutEvent(login);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void doLogLogOutEvent(String login) {
        UserEventLogBuilder logoutEventLogBuilder = createLogoutEventLogBuilder(login);
        saveUserEventLog(logoutEventLogBuilder);
    }

    private UserEventLogBuilder createLogoutEventLogBuilder(String login) {
        UserEventLogBuilder userEventLogBuilder = new UserEventLogBuilder();
        userEventLogBuilder.setUserId(login);
        userEventLogBuilder.setPerson(currentUserAccessor.getCurrentUserId());
        userEventLogBuilder.setDate(new Date()).setEventType(EventLogType.LOGOUT.name()).setSuccess(true);
        return userEventLogBuilder;
    }

    private UserEventLogBuilder createLoginEventLogBuilder(String login, String ip, boolean success) {
        UserEventLogBuilder userEventLogBuilder = new UserEventLogBuilder();
        userEventLogBuilder.setUserId(login);
        if (success) {
            userEventLogBuilder.setPerson(currentUserAccessor.getCurrentUserId());
        }
        userEventLogBuilder.setClientIp(ip).setDate(new Date()).setEventType(EventLogType.LOGIN.name()).setSuccess(success);
        return userEventLogBuilder;
    }

    private boolean isLogoutEventEnabled() {
        EventLogsConfig eventLogsConfiguration = configurationExplorer.getEventLogsConfiguration();
        if (eventLogsConfiguration != null && eventLogsConfiguration.getLogoutConfig() != null) {
            return eventLogsConfiguration.getLogoutConfig().isEnable();
        }
        return false;
    }

    @Override
    public void logDownloadAttachmentEvent(Id attachment) {
        if (!isDownloadAttachmentEventEnabled()) {
            return;
        }

        newTransactionService.doLogDownloadAttachmentEvent(attachment);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void doLogDownloadAttachmentEvent(Id attachment) {
        ObjectAccessLogBuilder accessLogBuilder = new ObjectAccessLogBuilder();
        if (currentUserAccessor.getCurrentUserId() != null) {
            accessLogBuilder.setPerson(currentUserAccessor.getCurrentUserId());
        } else {
            accessLogBuilder.setProcessName("system");
        }
        accessLogBuilder.setEventType(EventLogType.DOWNLOAD_ATTACHMENT.name());
        accessLogBuilder.setObjectId(attachment).setDate(new Date()).setSuccess(true);

        saveObjectAccessLog(accessLogBuilder);
    }

    @Override
    public void logAccessDomainObjectEvent(Id objectId, String accessType, boolean success) {
        if (objectId == null) {
            return;
        }

        if (!isAccessDomainObjectEventEnabled(objectId, accessType, success)) {
            return;
        }

        if (!EventLogService.ACCESS_OBJECT_READ.equals(accessType) && !EventLogService.ACCESS_OBJECT_WRITE.equals(accessType)) {
            throw new IllegalArgumentException("Illegal access type '" + accessType + "' passed.");
        }
        newTransactionService.doLogDomainObjectAccess(objectId, accessType, success);
    }

    @Override
    public void logAccessDomainObjectEvent(List<Id> objectIds, String accessType, boolean success) {
        if (!isDomainObjectAccessLogEnabled()) {
            return;
        }

        List<DomainObject> objectAccessLogs = null;

        for (Id objectId : objectIds) {
            DomainObject objectAccessLog = createObjectAccessLogObject(objectId, accessType, success);
            if (objectAccessLog != null) {
                if (objectAccessLogs == null) {
                    objectAccessLogs = new ArrayList<>();
                }
                objectAccessLogs.add(objectAccessLog);
            }
        }
        if (objectAccessLogs != null && objectAccessLogs.size() > 0) {
            newTransactionService.saveAccessLogObjects(objectAccessLogs);
        }
    }

    @Override
    public void logAccessDomainObjectEventByDo(List<DomainObject> objects, String accessType, boolean success) {
        if (!isDomainObjectAccessLogEnabled()) {
            return;
        }

        List<DomainObject> objectAccessLogs = null;

        for (DomainObject object : objects) {
            DomainObject objectAccessLog = createObjectAccessLogObject(object.getId(), accessType, success);
            if (objectAccessLog != null) {
                if (objectAccessLogs == null) {
                    objectAccessLogs = new ArrayList<>();
                }
                objectAccessLogs.add(objectAccessLog);
            }
        }
        if (objectAccessLogs != null && objectAccessLogs.size() > 0) {
            newTransactionService.saveAccessLogObjects(objectAccessLogs);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void saveAccessLogObjects(List<DomainObject> objectAccessLogs) {
        if (objectAccessLogs.size() > 0) {
            domainObjectDao.save(objectAccessLogs, getSystemAccessToken());
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void doLogDomainObjectAccess(Id objectId, String accessType, boolean success) {
        ObjectAccessLogBuilder accessLogBuilder = new ObjectAccessLogBuilder();
        if (currentUserAccessor.getCurrentUserId() != null) {
            accessLogBuilder.setPerson(currentUserAccessor.getCurrentUserId());
        } else {
            accessLogBuilder.setProcessName("system");
        }
        accessLogBuilder.setEventType(EventLogType.ACCESS_OBJECT.name());
        accessLogBuilder.setObjectId(objectId).setAccessType(accessType).setDate(new Date()).setSuccess(success);

        saveObjectAccessLog(accessLogBuilder);
    }

    private DomainObject createObjectAccessLogObject(Id objectId, String accessType, boolean success) {
        if (objectId == null) {
            return null;
        }

        if (!isAccessDomainObjectEventEnabled(objectId, accessType, success)) {
            return null;
        }

        if (!EventLogService.ACCESS_OBJECT_READ.equals(accessType) && !EventLogService.ACCESS_OBJECT_WRITE.equals(accessType)) {
            throw new IllegalArgumentException("Illegal access type '" + accessType + "' passed.");
        }

        ObjectAccessLogBuilder accessLogBuilder = new ObjectAccessLogBuilder();
        if (currentUserAccessor.getCurrentUserId() != null) {
            accessLogBuilder.setPerson(currentUserAccessor.getCurrentUserId());
        } else {
            accessLogBuilder.setProcessName("system");
        }
        accessLogBuilder.setEventType(EventLogType.ACCESS_OBJECT.name());
        accessLogBuilder.setObjectId(objectId).setAccessType(accessType).setDate(new Date()).setSuccess(success);

        return createObjectAccessLogDO(accessLogBuilder);
    }

    private DomainObject saveObjectAccessLog(ObjectAccessLogBuilder objectAccessLogBuilder) {
        DomainObject objectAccessLog = createObjectAccessLogDO(objectAccessLogBuilder);

        objectAccessLog = domainObjectDao.save(objectAccessLog, getSystemAccessToken());
        return objectAccessLog;
    }

    private DomainObject createObjectAccessLogDO(ObjectAccessLogBuilder objectAccessLogBuilder) {
        DomainObject objectAccessLog = createDomainObject(OBJECT_ACCESS_LOG);
        objectAccessLog.setString("event_type", objectAccessLogBuilder.getEventType());
        objectAccessLog.setReference("person", objectAccessLogBuilder.getPerson());
        objectAccessLog.setString("client_ip_address", objectAccessLogBuilder.getClientIp());
        objectAccessLog.setString("user_id", objectAccessLogBuilder.getUserId());

        objectAccessLog.setReference("object", objectAccessLogBuilder.getObjectId());
        objectAccessLog.setString("access_type", objectAccessLogBuilder.getAccessType());
        objectAccessLog.setTimestamp("date", objectAccessLogBuilder.getDate());
        objectAccessLog.setBoolean("success", objectAccessLogBuilder.isSuccess());
        objectAccessLog.setString("process_name", objectAccessLogBuilder.getProcessName());
        return objectAccessLog;
    }

    private DomainObject saveUserEventLog(UserEventLogBuilder objectAccessLogBuilder) {
        DomainObject userEventLog = createDomainObject(USER_EVENT_LOG);
        userEventLog.setString("event_type", objectAccessLogBuilder.getEventType());
        userEventLog.setReference("person", objectAccessLogBuilder.getPerson());
        userEventLog.setString("client_ip_address", objectAccessLogBuilder.getClientIp());
        userEventLog.setString("user_id", objectAccessLogBuilder.getUserId());
        userEventLog.setTimestamp("date", objectAccessLogBuilder.getDate());
        userEventLog.setBoolean("success", objectAccessLogBuilder.isSuccess());

        userEventLog = domainObjectDao.save(userEventLog, getSystemAccessToken());
        return userEventLog;
    }

    private boolean isDownloadAttachmentEventEnabled() {
        EventLogsConfig eventLogsConfiguration = configurationExplorer.getEventLogsConfiguration();
        if (eventLogsConfiguration != null && eventLogsConfiguration.getDownloadAttachment() != null) {
            return eventLogsConfiguration.getDownloadAttachment().isEnable();
        }
        return false;
    }

    private boolean isDomainObjectAccessLogEnabled() {
        EventLogsConfig eventLogsConfiguration = configurationExplorer.getEventLogsConfiguration();
        if (eventLogsConfiguration == null || eventLogsConfiguration.getDomainObjectAccess() == null) {
            return false;
        }
        return eventLogsConfiguration.getDomainObjectAccess().isEnable();
    }

    public boolean isAccessDomainObjectEventEnabled(Id objectId, String accessType, boolean success) {
        EventLogsConfig eventLogsConfiguration = configurationExplorer.getEventLogsConfiguration();
        if (eventLogsConfiguration != null && eventLogsConfiguration.getDomainObjectAccess() != null) {
            if (!eventLogsConfiguration.getDomainObjectAccess().isEnable())
                return false;

            String typeName = domainObjectTypeIdCache.getName(objectId);

            if (typeName.startsWith("sel_")) {
                return false;
            }

            LogDomainObjectAccessConfig accessEventLogsConfiguration = configurationExplorer.getDomainObjectAccessEventLogsConfiguration(typeName);

            if (!accessEventLogsConfiguration.isEnable()) {
                return false;
            }

            if (!"*".equals(accessEventLogsConfiguration.getAccessType()) && !accessType.equals(accessEventLogsConfiguration.getAccessType())) {
                return false;
            }

            String accessWasGranted = success ? EventLogService.ACCESS_OBJECT_WAS_GRANTED_YES : EventLogService.ACCESS_OBJECT_WAS_GRANTED_NO;
            // TODO add support for true/false
            if (!"*".equals(accessEventLogsConfiguration.getAccessWasGranted())
                    && !accessWasGranted.equals(accessEventLogsConfiguration.getAccessWasGranted())) {
                return false;
            }

            return true;
        }
        return false;
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


    /**
     * Удаляет данные audit_log и event_log (CMFIVE-30153) 
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void clearEventLogs() {
        Id groupId = personManagementServiceDao.getGroupId("InfoSecAuditor");
        Id personId = currentUserAccessor.getCurrentUserId();

        if (!personManagementServiceDao.isPersonInGroup(groupId, personId)){
            throw new IllegalStateException("Not permitted");
        }
        
        int deleteCount = 0;
        
        AccessToken token = getSystemAccessToken();

        // удаление всех записей object_access_log
        IdentifiableObjectCollection col = collectionsDao.findCollectionByQuery("select id from object_access_log", 0, 0, token);
        List<Id> accessObjects = new ArrayList<>();
        for (IdentifiableObject obj: col)
            accessObjects.add(obj.getId());
        deleteCount = deleteCount + domainObjectDao.delete(accessObjects, token);

        // удаление всех записей user_event_log
        col = collectionsDao.findCollectionByQuery("select id from user_event_log", 0, 0, token);
        List<Id> ueObjects = new ArrayList<>();
        for (IdentifiableObject obj: col)
            ueObjects.add(obj.getId());
        deleteCount = deleteCount + domainObjectDao.delete(ueObjects, token);

        // удаление audit_log

        Collection<DomainObjectTypeConfig> typeConfigs = configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
        List<String> typeNamesToProcess = new ArrayList<>();

        // собираем ДО, по котрым включен лог + нужно взять всех "дочерних" с включенным логом и обработать их родителей - т.е. таблицы, которые нужно чистить
        // НЕ СРАБОТАЛО! т.к. влияют на включение лога еще и связанные объекты (по всей видимости) 
//        for (DomainObjectTypeConfig domainObjectTypeConfig : typeConfigs) {
//            if (domainObjectTypeConfig.isAuditLog()!=null && domainObjectTypeConfig.isAuditLog()) {
//                if (!typeNamesToProcess.contains(domainObjectTypeConfig.getName())) {
//                    typeNamesToProcess.add(domainObjectTypeConfig.getName());
//                }
//                DomainObjectTypeConfig currConfig = domainObjectTypeConfig;
//                while ( currConfig.getExtendsAttribute()!=null && !"".equals(currConfig.getExtendsAttribute()) ) {
//                    currConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, currConfig.getExtendsAttribute());
//                    if (!typeNamesToProcess.contains(currConfig.getName())) {
//                        typeNamesToProcess.add(currConfig.getName());
//                    }
//                }
//            }
//        }
        // собираем все типы ДО, по которым можно удалить _al 
        for (DomainObjectTypeConfig domainObjectTypeConfig : typeConfigs) {
            if (domainObjectTypeConfig.isTemplate()!=null && !domainObjectTypeConfig.isTemplate() && !configurationExplorer.isAuditLogType(domainObjectTypeConfig.getName())) {
                typeNamesToProcess.add(domainObjectTypeConfig.getName());
            }
        }

        Map<String, Object> parameters = new java.util.HashMap();
        String tables = "";
        for (String doType : typeNamesToProcess) {
            String auditLogTableName = DataStructureNamingHelper.getALTableSqlName(doType);
//            deleteCount = deleteCount + masterJdbcTemplate.update("delete from "+auditLogTableName, parameters); // - дает ошибку, т.к. таблицы _al могут быть связаны
            tables = tables + auditLogTableName+ ",";
//            System.out.println("DELETING "+auditLogTableName);
        }
        tables = tables.substring(0, tables.length()-1);
        deleteCount = deleteCount + masterJdbcTemplate.update("TRUNCATE "+tables+" RESTRICT", parameters); // выполнение запроса TRUNCATE не возвращает количество удаленных записей77

        // создание записи об очистке
        UserEventLogBuilder userEventLogBuilder = new UserEventLogBuilder();
        userEventLogBuilder.setPerson(currentUserAccessor.getCurrentUserId());
        userEventLogBuilder.setDate(new Date());
        userEventLogBuilder.setEventType(EventLogType.CLEAR_EVENT_LOG.name());
        userEventLogBuilder.setSuccess(true);
        saveUserEventLog(userEventLogBuilder);
        
//        return deleteCount;
    }

    /**
     * Builder для ДО object_access_log.
     * @author atsvetkov
     */
    private class ObjectAccessLogBuilder extends BaseEventLogBuilder {
        private String accessType;
        private Id objectId;
        private String processName;

        public String getAccessType() {
            return accessType;
        }

        public ObjectAccessLogBuilder setAccessType(String accessType) {
            this.accessType = accessType;
            return this;
        }

        public Id getObjectId() {
            return objectId;
        }

        public ObjectAccessLogBuilder setObjectId(Id object) {
            this.objectId = object;
            return this;
        }

        public String getProcessName() {
            return processName;
        }

        public ObjectAccessLogBuilder setProcessName(String processName) {
            this.processName = processName;
            return this;
        }

    }

    /**
     * Builder для ДО user_event_log.
     * @author atsvetkov
     */
    private class UserEventLogBuilder extends BaseEventLogBuilder {

    }

    /**
     * @author atsvetkov
     */
    private class BaseEventLogBuilder {
        private Id person;
        private String clientIp;
        private String userId;

        private String eventType;
        private boolean success;
        private Date date;

        public Id getPerson() {
            return person;
        }

        public BaseEventLogBuilder setPerson(Id person) {
            this.person = person;
            return this;
        }

        public String getClientIp() {
            return clientIp;
        }

        public BaseEventLogBuilder setClientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public String getEventType() {
            return eventType;
        }

        public BaseEventLogBuilder setEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public boolean isSuccess() {
            return success;
        }

        public BaseEventLogBuilder setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public Date getDate() {
            return date;
        }

        public BaseEventLogBuilder setDate(Date date) {
            this.date = date;
            return this;
        }

        public String getUserId() {
            return userId;
        }

        public BaseEventLogBuilder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

    }

}
