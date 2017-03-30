package ru.intertrust.cm.core.business.api;

import java.util.Map;
import java.util.concurrent.Future;

import ru.intertrust.cm.core.business.api.dto.ReportResult;

/**
 * Сервис формирования отчетов
 * @author larin
 *
 */
public interface ReportService extends ReportServiceDelegate {
    
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
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return
     */
    ReportResult generate(String name, Map<String, Object> parameters, DataSourceContext dataSource);

    /**
     * Синхронная генерация отчета с принудительным сохранением сформированного отчета
     * @param name
     * @param parameters
     * @param keepDays
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return
     */
    ReportResult generate(String name, Map<String, Object> parameters, Integer keepDays, DataSourceContext dataSource);
    
    /**
     * Асинхронная генерация отчета
     * @param name
     * @param parameters
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return
     */
    @Deprecated
    Future<ReportResult> generateAsync(String name, Map<String, Object> parameters, DataSourceContext dataSource);

}
