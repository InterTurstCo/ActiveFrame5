package ru.intertrust.cm.core.business.api.schedule;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;


import java.util.List;

/**
 * Business interface for ScheduleTaskLoader
 * Created by vmatsukevich on 6/17/14.
 */
public interface ScheduleTaskLoader {

    interface Remote extends ScheduleTaskLoader {}

    void load();

    /**
     * Проверка состояния службы на готовность к работе
     * @return
     */
    boolean isLoaded();
    
    List<SheduleTaskReestrItem> getSheduleTaskReestrItems(boolean singletonOnly);

    DomainObject createTaskDomainObject(SheduleTaskReestrItem item);

    SheduleTaskReestrItem getSheduleTaskReestrItem(String className);

    DomainObject createTaskDomainObject(SheduleTaskReestrItem item, String name);

    ScheduleTaskHandle getSheduleTaskHandle(String className);

    /**
     * Получает статус сервиса
     * @return
     */
    boolean isEnable();

    /**
     * Устанавливает статус сервиса
     * @param isEnable
     */
    void setEnable(boolean isEnable);
    
    /**
     * Получение следующей ноды из списка серверов имеющих роль
     * schedule_executor
     * @return
     */
    String getNextNodeId();
}
