package ru.intertrust.cm.core.business.api;

import java.util.Map;
import java.util.concurrent.Future;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReportResult;

/**
 * Сервис формирования отчетов
 *
 * @author larin
 */
public interface ReportService {

    String TICKET_HEADER = "Ticket";

    /**
     * Remote интерфейс
     */
    interface Remote extends ReportService {
    }

    /**
     * Синхронная генерация отчета
     */
    ReportResult generate(String name, Map<String, Object> parameters);

    /**
     * Синхронная генерация отчета
     *
     * @param dataSource Контекст источника данных, используемого для операций с данными
     */
    ReportResult generate(String name, Map<String, Object> parameters, DataSourceContext dataSource);

    /**
     * Синхронная генерация отчета с принудительным сохранением сформированного отчета
     */
    ReportResult generate(String name, Map<String, Object> parameters, Integer keepDays);

    /**
     * Синхронная генерация отчета с принудительным сохранением сформированного отчета
     *
     * @param dataSource Контекст источника данных, используемого для операций с данными
     */
    ReportResult generate(String name, Map<String, Object> parameters, Integer keepDays, DataSourceContext dataSource);

    /**
     * Асинхронная генерация отчета
     */
    @Deprecated
    Future<ReportResult> generateAsync(String name, Map<String, Object> parameters);

    /**
     * Асинхронная генерация отчета
     *
     * @param dataSource Контекст источника данных, используемого для операций с данными
     */
    @Deprecated
    Future<ReportResult> generateAsync(String name, Map<String, Object> parameters, DataSourceContext dataSource);

    @Deprecated
    Future<ReportResult> generateAsync(String name, Map<String, Object> parameters, Id queue, String ticket);
}
