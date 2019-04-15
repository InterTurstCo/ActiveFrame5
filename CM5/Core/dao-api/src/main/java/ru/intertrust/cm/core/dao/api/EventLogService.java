package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.List;


/**
 * Сервис ведения системного журнала событий
 */
public interface EventLogService {

    /**
     * Типы событий для журналирования.
     */
    public enum EventLogType {
        LOGIN,
        LOGOUT,
        DOWNLOAD_ATTACHMENT,
        ACCESS_OBJECT,
        CLEAR_EVENT_LOG
    }

    String ACCESS_OBJECT_READ = "R";
    String ACCESS_OBJECT_WRITE = "W";

    String ACCESS_OBJECT_WAS_GRANTED_YES = "Y";
    String ACCESS_OBJECT_WAS_GRANTED_NO = "N";

    void logLogInEvent(String login, String ip, boolean success);

    void doLogLogInEvent(String login, String ip, boolean success);

    void logLogOutEvent(String login);

    void doLogLogOutEvent(String login);

    void logDownloadAttachmentEvent(Id attachment);

    void doLogDownloadAttachmentEvent(Id attachment);

    void logAccessDomainObjectEvent(Id objectId, String accessType, boolean success);

    void logAccessDomainObjectEvent(List<Id> objectIds, String accessType, boolean success);

    void logAccessDomainObjectEventByDo(List<DomainObject> objects, String accessType, boolean success);

    void saveAccessLogObjects(List<DomainObject> objectAccessLogs);

    void doLogDomainObjectAccess(Id objectId, String accessType, boolean success);

    boolean isAccessDomainObjectEventEnabled(Id objectId, String accessType, boolean success);

    void clearEventLogs();

}
