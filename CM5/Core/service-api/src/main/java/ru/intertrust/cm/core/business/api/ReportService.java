package ru.intertrust.cm.core.business.api;

import java.util.Map;
import java.util.concurrent.Future;

import ru.intertrust.cm.core.business.api.dto.ReportResult;

/**
 * Сервис формирования отчетов
 *
 * @author larin
 */
public interface ReportService extends ReportServiceDelegate {

    String TICKET_HEADER = "Ticket";

    /**
     * Remote интерфейс
     *
     * @author larin
     */
    interface Remote extends ReportService {
    }

    /**
     * Синхронная генерация отчета
     *
     * @param dataSource контекст источника данных, используемого для операций с данными
     */
    ReportResult generate(String name, Map<String, Object> parameters, DataSourceContext dataSource);

    /**
     * Синхронная генерация отчета с принудительным сохранением сформированного отчета
     *
     * @param dataSource контекст источника данных, используемого для операций с данными
     */
    ReportResult generate(String name, Map<String, Object> parameters, Integer keepDays, DataSourceContext dataSource);

    /**
     * Асинхронная генерация отчета
     *
     * @param dataSource контекст источника данных, используемого для операций с данными
     */
    @Deprecated
    Future<ReportResult> generateAsync(String name, Map<String, Object> parameters, DataSourceContext dataSource);
}
