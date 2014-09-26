package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.EventLogService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.eventlog.EventLogsConfig;
import ru.intertrust.cm.core.config.eventlog.LogDomainObjectAccessConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
@Local(EventLogService.class)
@Remote(EventLogService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class EventLogServiceImpl implements EventLogService, EventLogService.Remote {

    @Autowired
    private CrudService crudService;

    @Autowired
    private PersonServiceDao personServiceDao;

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
        if (!isLoginEventEnabled()) return;

        DomainObject selSubjUser = createSelSubjUserRecord(success ? getCurrentUserId() : null, login, ip);
        createSystemEventLogRecord(LOGIN, success, selSubjUser, null);
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
        if (!isLogoutEventEnabled()) return;

        DomainObject selSubjUser = createSelSubjUserRecord(findPersonByLogin(login), null, null);
        createSystemEventLogRecord(LOGOUT, true, selSubjUser, null);
    }

    private boolean isLogoutEventEnabled() {
        EventLogsConfig eventLogsConfiguration = configurationExplorer.getEventLogsConfiguration();
        if (eventLogsConfiguration != null && eventLogsConfiguration.getLogoutConfig() != null) {
            return eventLogsConfiguration.getLogoutConfig().isEnable();
        }
        return false;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logDownloadAttachmentEvent(Id attachment) {
        if (!isDownloadAttachmentEventEnabled()) return;
        DomainObject selSubjUser = createSelSubjUserRecord(getCurrentUserId(), null, null);
        DomainObject selObjAttachment = crudService.createDomainObject("sel_obj_attachment");
        selObjAttachment.setReference("attachment", attachment);
        selObjAttachment = domainObjectDao.save(selObjAttachment, getEventLogAccessToken());
        createSystemEventLogRecord(DOWNLOAD_ATTACHMENT, true, selSubjUser, selObjAttachment);
    }

    private boolean isDownloadAttachmentEventEnabled() {
        EventLogsConfig eventLogsConfiguration = configurationExplorer.getEventLogsConfiguration();
        if (eventLogsConfiguration != null && eventLogsConfiguration.getDownloadAttachment() != null) {
            return eventLogsConfiguration.getDownloadAttachment().isEnable();
        }
        return false;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logAccessDomainObjectEvent(Id objectId, String accessType, boolean success) {
        logAccessDomainObject(objectId, accessType, success);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logAccessDomainObjectEvent(List<Id> objectIds, String accessType, boolean success) {
        for (Id objectId : objectIds) {
            logAccessDomainObject(objectId, accessType, success);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logAccessDomainObjectEventByDo(List<DomainObject> objects, String accessType, boolean success) {
        for (DomainObject object : objects) {
            logAccessDomainObject(object.getId(), accessType, success);
        }
    }

    private void logAccessDomainObject(Id objectId, String accessType, boolean success) {
        if (!ACCESS_OBJECT_READ.equals(accessType) && !ACCESS_OBJECT_WRITE.equals(accessType)){
            throw new IllegalArgumentException("Illegal access type '" + accessType + "' passed.");
        }

        if (objectId == null) return;

        if (!isAccessDomainObjectEventEnabled(objectId, accessType, success)) return;

        DomainObject selSubjUser = createSelSubjUserRecord(getCurrentUserId(), null, null);
        DomainObject selObjObjectAccess = crudService.createDomainObject("sel_obj_object_access");
        selObjObjectAccess.setReference("object", objectId);
        selObjObjectAccess.setString("access_type", accessType);
        selObjObjectAccess = domainObjectDao.save(selObjObjectAccess, getEventLogAccessToken());

        createSystemEventLogRecord(ACCESS_OBJECT, success, selSubjUser, selObjObjectAccess);
    }


    private boolean isAccessDomainObjectEventEnabled(Id objectId, String accessType, boolean success) {
        EventLogsConfig eventLogsConfiguration = configurationExplorer.getEventLogsConfiguration();
        if (eventLogsConfiguration != null && eventLogsConfiguration.getDomainObjectAccess() != null) {
            if (!eventLogsConfiguration.getDomainObjectAccess().isEnable()) return false;

            String typeName = domainObjectTypeIdCache.getName(objectId);

            if (typeName.startsWith("sel_")) return false;

            LogDomainObjectAccessConfig accessEventLogsConfiguration = configurationExplorer.getDomainObjectAccessEventLogsConfiguration(typeName);

            if (!accessEventLogsConfiguration.isEnable()) return false;

            if (!"*".equals(accessEventLogsConfiguration.getAccessType()) && !accessType.equals(accessEventLogsConfiguration.getAccessType())){
                return false;
            }

            String accessWasGranted = success ? ACCESS_OBJECT_WAS_GRANTED_YES : ACCESS_OBJECT_WAS_GRANTED_NO;
            if (!"*".equals(accessEventLogsConfiguration.getAccessWasGranted())
                    && !accessWasGranted.equals(accessEventLogsConfiguration.getAccessWasGranted())){
                return false;
            }

            return true;
        }
        return false;
    }


    private DomainObject createSystemEventLogRecord(String type, boolean success, DomainObject subject, DomainObject object) {
        DomainObject systemEventLog = crudService.createDomainObject("system_event_log");

        systemEventLog.setTimestamp("date", new Date());
        systemEventLog.setReference("type", findSelTypeId(type));
        systemEventLog.setReference("subject", subject);
        systemEventLog.setReference("object", object);

        systemEventLog.setBoolean("success", success);
        return domainObjectDao.save(systemEventLog, getEventLogAccessToken());
    }

    private DomainObject createSelSubjUserRecord(DomainObject person, String login, String ip) {
        DomainObject selSubjUser = crudService.createDomainObject("sel_subj_user");
        selSubjUser.setReference("person", person);
        selSubjUser.setString("user_id", login);
        selSubjUser.setString("ip_address", ip);
        selSubjUser = domainObjectDao.save(selSubjUser, getEventLogAccessToken());
        return selSubjUser;
    }


    private Id findSelTypeId(String type) {
        Map<String, Value> keyValue = new HashMap<>();
        keyValue.put("code", new StringValue(type));
        return domainObjectDao.findByUniqueKey("sel_type", keyValue, getEventLogAccessToken());
    }


    private DomainObject getCurrentUserId() {
        String currentUser = currentUserAccessor.getCurrentUser();
        return findPersonByLogin(currentUser);
    }

    private DomainObject findPersonByLogin(String login) {
        if (login == null) return null;
        try {
            return personServiceDao.findPersonByLogin(login);
        } catch (Exception ex) {
            return null;
        }
    }

    private AccessToken getEventLogAccessToken() {
        return accessControlService.createSystemAccessToken(this.getClass().getName());
    }


}
