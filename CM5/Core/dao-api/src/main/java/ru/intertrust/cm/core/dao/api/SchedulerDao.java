package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

import java.util.List;

/**
 * DAO для работы с расписанием задач
 *
 */
public interface SchedulerDao {

    static final String SCHEDULE_STATUS_SLEEP = "Sleep";

    /**
     * Получение задач в определенном статусе
     * @param status
     * @return
     */
    List<DomainObject> getTasksByStatus(String status, boolean activeOnly, String nodeId);

    /**
     * Получение всех задач у которых статус отличен от ScheduleService.SCHEDULE_STATUS_SLEEP
     * @return
     */
    List<DomainObject> getNonSleepTasks();

}
