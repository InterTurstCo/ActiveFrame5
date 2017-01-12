package ru.intertrust.cm.core.business.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.schedule.Schedule;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

/**
 * Сервис периодических заданий
 * @author larin
 * 
 */
public interface ScheduleService {
    public static final String SCHEDULE_NAME = "name";
    public static final String SCHEDULE_TASK_CLASS = "task_class";
    public static final String SCHEDULE_TASK_TYPE = "task_type";
    public static final String SCHEDULE_YEAR = "year";
    public static final String SCHEDULE_MONTH = "month";
    public static final String SCHEDULE_DAY_OF_MONTH = "day_of_month";
    public static final String SCHEDULE_DAY_OF_WEEK = "day_of_week";
    public static final String SCHEDULE_HOUR = "hour";
    public static final String SCHEDULE_MINUTE = "minute";
    public static final String SCHEDULE_TIMEOUT = "timeout";
    public static final String SCHEDULE_PRIORITY = "priority";
    public static final String SCHEDULE_PARAMETERS = "parameters";
    public static final String SCHEDULE_RESULT = "result";
    public static final String SCHEDULE_RESULT_DESCRIPTION = "result_description";
    public static final String SCHEDULE_ACTIVE = "active";
    public static final String SCHEDULE_REDY = "redy_date";
    public static final String SCHEDULE_WAIT = "wait_date";
    public static final String SCHEDULE_RUN = "run_date";
    public static final String SCHEDULE_COMPLETE = "complete_date";
    public static final String SCHEDULE_NODE_ID = "node_id";
    public static final String SCHEDULE_ALL_NODES = "all_nodes";
    public static final String SCHEDULE_BAD_TASK = "bad_task";
    public static final String SCHEDULE_TASK_TRANSACTIONAL_MANAGEMENT = "task_transact_management";
    
    public static final String SCHEDULE_EXECUTION = "schedule_execution";
    public static final String SCHEDULE_EXECUTION_SCHEDULE = "schedule";      
    
    /**
     * Задача прошла проверку рассписания или запущена вручную
     */
    public static final String SCHEDULE_STATUS_READY = "Ready";
    /**
     * Задача отправлена асинхронному ejb на выполнение
     */
    public static final String SCHEDULE_STATUS_WAIT = "Wait";
    /**
     * Задача выполняется
     */
    public static final String SCHEDULE_STATUS_RUN = "Run";
    /**
     * Выполнение задачи завершено
     */
    public static final String SCHEDULE_STATUS_COMPLETE = "Complete";
    
    /**
     * Имя роли менеджер сервиса периодических заданий
     */
    public static final String SCHEDULE_MANAGER_ROLE_NAME = "schedule_manager";
    /**
     * Имя роли исполнитель роли периодических заданий
     */
    public static final String SCHEDULE_EXECUTOR_ROLE_NAME = "schedule_executor";    
    
    /**
     * Удаленный интерфейс
     * @author larin
     *
     */
    public interface Remote extends ScheduleService{        
    }
    
    /**
     * Получение всех задач, которые обрабатываются сервисом периодических заданий.
     * @return
     */
    List<DomainObject> getTaskList();

    /**
     * Получение классов задач которые могут существовать во множественном числе экземпляров. Используется GII дла
     * отрисовки диалога создания периодического задания
     * @return
     */
    List<String> getTaskClasses();

    /**
     * Получение расписания задачи
     * @return
     */
    Schedule getTaskSchedule(Id taskId);

    /**
     * Установка расписания задачи
     * @param taskId
     * @param schedule
     */
    void setTaskSchedule(Id taskId, Schedule schedule);

    /**
     * Получение параметров задачи
     * @return
     */
    ScheduleTaskParameters getTaskParams(Id taskId);

    /**
     * Установка параметров задачи
     * @param taskId
     * @param schedule
     */
    void setTaskParams(Id taskId, ScheduleTaskParameters parameters);

    /**
     * Активировать задание
     * @param taskId
     */
    void enableTask(Id taskId);

    /**
     * Деактивировать задание
     * @param taskId
     */
    void disableTask(Id taskId);

    /**
     * Запустить задание
     * @param taskId
     */
    void run(Id taskId);
    
    /**
     * Установка приоритета. Значения могут быть от 0 до 4
     * @param priority
     */
    void setPriority(Id taskId, int priority);
    
    /**
     * Установка таймаута в минутах
     * @param timeout
     */
    void setTimeout(Id taskId, int timeout);

    /**
     * Создание периодического задания класса multible
     * @param string
     * @return
     */
    DomainObject createScheduleTask(String className, String name);
    
}
