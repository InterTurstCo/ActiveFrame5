package ru.intertrust.cm.core.dao.api;

public interface EventLogCleaner {

    /**
     * Удаляет данные audit_log и event_log (CMFIVE-30153) 
     */
    void clearEventLogs() throws Exception;

}
