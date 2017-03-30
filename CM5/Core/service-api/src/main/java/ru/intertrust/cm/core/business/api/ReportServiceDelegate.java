package ru.intertrust.cm.core.business.api;

import java.util.Map;
import java.util.concurrent.Future;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReportResult;

/**
 * Внутренний служебный интерфейс сервиса отчетов. Используется для создания транзакционной и нетранзакциооной версий сервиса
 * @author larin
 *
 */
public interface ReportServiceDelegate {
    public static final String FORMAT_PARAM = "FORMAT";
    
    /**
     * Remote интерфейс
     * @author larin
     *
     */
    public interface Remote extends ReportServiceDelegate {
    }
    
    /**
     * Синхронная генерация отчета
     * @param name
     * @param parameters
     * @return
     */
    ReportResult generate(String name, Map<String, Object> parameters);
    
    /**
     * Синхронная генерация отчета с принудительным сохранением сформированного отчета
     * @param name
     * @param parameters
     * @param keepDays
     * @return
     */
    ReportResult generate(String name, Map<String, Object> parameters, Integer keepDays);
    
    /**
     * Асинхронная генерация отчета
     * @param name
     * @param parameters
     * @return
     */
    Future<ReportResult> generateAsync(String name, Map<String, Object> parameters); 
    
    /**
     * 
     * @param name
     * @param parameters
     * @param queue
     * @return
     */
    Future<ReportResult> generateAsync(String name, Map<String, Object> parameters, Id queue, String ticket);    
    
}
