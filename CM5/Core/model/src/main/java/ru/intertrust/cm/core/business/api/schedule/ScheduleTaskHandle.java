package ru.intertrust.cm.core.business.api.schedule;

import javax.ejb.SessionContext;

/**
 * Интерфейс, который должны имплиментить все классы периодических заданий 
 * @author larin
 *
 */
public interface ScheduleTaskHandle {
    /**
     * Запуск выполнения периодического задания
     * @param parameters
     * @return возвращает результат работы периодического задания в виде строки. Строка будет хранится в доменном обете задания
     */
    String execute(SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException;
}
