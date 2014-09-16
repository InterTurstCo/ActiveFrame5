package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.Id;


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

    public interface Remote extends EventLogService {
    }

    public void logLogInEvent(String login, String ip, boolean success);

    public void logLogOutEvent(String login);

    public void logDownloadAttachmentEvent(Id attachment);
}
