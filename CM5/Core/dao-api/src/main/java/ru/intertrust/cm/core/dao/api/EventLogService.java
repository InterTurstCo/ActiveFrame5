package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.List;


/**
 * Сервис ведения системного журнала событий
 */
public interface EventLogService {

    /**
     * типы системных событий для журналирования
     */
    public final static String LOGIN = "LOGIN";
    public final static String LOGOUT = "LOGOUT";
    public final static String DOWNLOAD_ATTACHMENT = "DOWNLOAD_ATTACHMENT";
    public final static String ACCESS_OBJECT = "ACCESS_OBJECT";
    public final static String ACCESS_COLLECTION = "ACCESS_COLLECTION";

    public final static String ACCESS_OBJECT_READ = "R";
    public final static String ACCESS_OBJECT_WRITE = "W";

    public final static String ACCESS_OBJECT_WAS_GRANTED_YES = "Y";
    public final static String ACCESS_OBJECT_WAS_GRANTED_NO = "N";

    public void logLogInEvent(String login, String ip, boolean success);

    public void logLogOutEvent(String login);

    public void logDownloadAttachmentEvent(Id attachment);

    public void logAccessDomainObjectEvent(Id objectId, String accessType, boolean success);

    public void logAccessDomainObjectEvent(List<Id> objectIds, String accessType, boolean success);

    public void logAccessDomainObjectEventByDo(List<DomainObject> objects, String accessType, boolean success);
}
