package ru.intertrust.cm.core.business.api.schedule;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Анотация класса периодического задания
 * @author larin
 *
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.TYPE })
public @interface ScheduleTask {
    String name();
    String year() default "*";
    String month() default "*";
    String dayOfWeek() default "*";
    String dayOfMonth() default "*";
    String hour() default "*";
    String minute() default "*";
    Class<? extends ScheduleTaskDefaultParameters> configClass() default ScheduleTaskDefaultParameters.class;
    SheduleType type() default SheduleType.Singleton; 
    long timeout() default 5;
    long priority() default 4;
    boolean active() default false;
    /**
     * Флаг что данная задача должна выполняется на всех нодах кластера, иначе выполнится только на одной случайно выбранной ноде
     * @return
     */
    boolean allNodes() default false;
    /**
     * Флаг что задача сама управляет транзакцией
     * @return
     */
    boolean taskTransactionalManagement() default false;
}
