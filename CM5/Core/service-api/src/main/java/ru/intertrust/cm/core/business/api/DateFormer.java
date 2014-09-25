package ru.intertrust.cm.core.business.api;

import java.util.Date;

import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;

/**
 * Интерфейс формирования даты в виде строки с учетом локализации той персоны, идентификатор которой передается
 * @author larin
 *
 */
public interface DateFormer {
    /**
     * Формирование строкового представления даты из объекта TimelessDate 
     * @param date
     * @param personId
     * @return
     */
    String format(TimelessDate date, Id personId);
    
    /**
     * Формирование строкового представления даты из объекта Date
     * @param date
     * @param personId
     * @return
     */
    String format(Date date, Id personId);
    
    /**
     * Формирование строкового представления даты из объекта DateTimeWithTimeZone
     * @param date
     * @param personId
     * @return
     */
    String format(DateTimeWithTimeZone date, Id personId);    
}
