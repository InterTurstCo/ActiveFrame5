package ru.intertrust.cm.core.business.api.schedule;

/**
 * Интерфейс конфигурации периодического задания по умолчанию.
 * Наследники используются в анотации ScheduleTask в качестве параметра
 * @author larin
 *
 */
public interface ScheduleTaskDefaultParameters {
    /**
     * Получение параметров по умолчанию
     * @return
     */
    ScheduleTaskParameters getDefaultParameters();
}
