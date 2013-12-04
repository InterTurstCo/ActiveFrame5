package ru.intertrust.cm.core.business.api;

import java.util.Map;
import java.util.concurrent.Future;

import ru.intertrust.cm.core.business.api.dto.GenerateReportStatus;
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
     * Асинхронная генерация отчета
     * @param name
     * @param parameters
     * @return
     */
    Future<ReportResult> generateAsync(String name, Map<String, Object> parameters);
}
