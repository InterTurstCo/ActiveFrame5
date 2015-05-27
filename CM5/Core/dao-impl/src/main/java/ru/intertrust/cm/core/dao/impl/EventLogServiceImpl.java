package ru.intertrust.cm.core.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.eventlog.EventLogsConfig;
import ru.intertrust.cm.core.config.eventlog.LogDomainObjectAccessConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.EventLogService;

@Stateless
@Local(EventLogService.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class EventLogServiceImpl implements EventLogService {

    private static final String USER_EVENT_LOG = "user_event_log";

    private static final String OBJECT_ACCESS_LOG = "object_access_log";

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logLogInEvent(String login, String ip, boolean success) {
        if (!isLoginEventEnabled()) {
            return;
        }
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
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logLogOutEvent(String login) {
        if (!isLogoutEventEnabled()) {
            return;
        }

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
    //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logDownloadAttachmentEvent(Id attachment) {
        if (!isDownloadAttachmentEventEnabled()) {
            return;
        }

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
    //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logAccessDomainObjectEvent(Id objectId, String accessType, boolean success) {
        logAccessDomainObject(objectId, accessType, success);
    }

    @Override
    //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logAccessDomainObjectEvent(List<Id> objectIds, String accessType, boolean success) {
        List<DomainObject> objectAccessLogs = new ArrayList<>();

        for (Id objectId : objectIds) {
            DomainObject objectAccessLog = createObjectAccessLogObject(objectId, accessType, success);
            if (objectAccessLog != null) {
                objectAccessLogs.add(objectAccessLog);
            }
        }
        domainObjectDao.save(objectAccessLogs, getSystemAccessToken());
    }

    @Override
    //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logAccessDomainObjectEventByDo(List<DomainObject> objects, String accessType, boolean success) {
        List<DomainObject> objectAccessLogs = new ArrayList<>();

        for (DomainObject object : objects) {
            DomainObject objectAccessLog = createObjectAccessLogObject(object.getId(), accessType, success);
            if (objectAccessLog != null) {
                objectAccessLogs.add(objectAccessLog);
            }
        }
        if (objectAccessLogs.size() > 0) {
            domainObjectDao.save(objectAccessLogs, getSystemAccessToken());
        }
    }

    private void logAccessDomainObject(Id objectId, String accessType, boolean success) {
        if (!EventLogService.ACCESS_OBJECT_READ.equals(accessType) && !EventLogService.ACCESS_OBJECT_WRITE.equals(accessType)) {
            throw new IllegalArgumentException("Illegal access type '" + accessType + "' passed.");
        }

        if (objectId == null) {
            return;
        }

        if (!isAccessDomainObjectEventEnabled(objectId, accessType, success)) {
            return;
        }

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
        DomainObject objectAccessLog = null;
        if (!EventLogService.ACCESS_OBJECT_READ.equals(accessType) && !EventLogService.ACCESS_OBJECT_WRITE.equals(accessType)) {
            throw new IllegalArgumentException("Illegal access type '" + accessType + "' passed.");
        }

        if (objectId == null) {
            return objectAccessLog;
        }

        if (!isAccessDomainObjectEventEnabled(objectId, accessType, success)) {
            return objectAccessLog;
        }

        ObjectAccessLogBuilder accessLogBuilder = new ObjectAccessLogBuilder();
        if (currentUserAccessor.getCurrentUserId() != null) {
            accessLogBuilder.setPerson(currentUserAccessor.getCurrentUserId());
        } else {
            accessLogBuilder.setProcessName("system");
        }
        accessLogBuilder.setEventType(EventLogType.ACCESS_OBJECT.name());
        accessLogBuilder.setObjectId(objectId).setAccessType(accessType).setDate(new Date()).setSuccess(success);

        objectAccessLog = createObjectAccessLogDO(accessLogBuilder);
        return objectAccessLog;
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
