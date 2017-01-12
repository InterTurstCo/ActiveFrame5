package ru.intertrust.cm.core.business.api.schedule;

import java.util.concurrent.Future;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Интерфейс асинхронного класса выполняющего задания в пуле процессов
 * @author larin
 *
 */
public interface ScheduleProcessor {

    /**
     * Запуск выполнения периодического задания в пуле процессов
     * @return
     */
    public Future<String> startAsync(Id taskExecutionId);
}
