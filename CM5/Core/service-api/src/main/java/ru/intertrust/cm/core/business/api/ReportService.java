package ru.intertrust.cm.core.business.api;

import java.util.Map;
import java.util.concurrent.Future;

import ru.intertrust.cm.core.business.api.dto.ReportResult;

/**
 * Сервис формирования отчетов
 * @author larin
 *
 */
public interface ReportService {
    public static final String FORMAP_PARAM = "FORMAT";
    
    /**
     * Remote интерфейс
     * @author larin
     *
     */
    public interface Remote extends ReportService{        
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
}
