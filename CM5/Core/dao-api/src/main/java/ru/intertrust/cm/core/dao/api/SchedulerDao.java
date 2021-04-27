package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;

/**
 * DAO для работы с расписанием задач
 *
 */
public interface SchedulerDao {

    /**
     * Получение идентификаторов подвисших задач
     * @return
     */
    IdentifiableObjectCollection getDeadScheduleExecution(); 

    /**
     * Получение всех активных задач
     * @return
     */
    IdentifiableObjectCollection getActiveTask(); 

    /**
     * Получение запущенных задач у конкретной ноды
     * @param taskId
     * @param nodeId
     * @return
     */
    IdentifiableObjectCollection getRunningScheduleExecution(Id taskId, String nodeId);

    /**
     * Получение задач на выполнение, готовых к выполнению
     * @param taskId
     * @param nodeId
     * @return
     */
    IdentifiableObjectCollection getReadyScheduleExecution(String nodeId);

    /**
     * Создание заданий на исполнение
     * задания создаются или во всех нодах, или в одной очередной, в зависимости от настроек задания
     * @param taskId
     */
    void createTaskExecution(Id taskId);

    /**
     * Проверка есть ли хотя бы одна запущенная задача этого типа на любой из нод
     * @param taskId
     * @return
     */
    boolean isRunningTask(Id taskId);
}
