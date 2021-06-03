package ru.intertrust.cm.core.business.api;

import java.util.Map;
import java.util.concurrent.Future;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReportResult;

/**
 * Внутренний служебный интерфейс сервиса отчетов. Используется для создания транзакционной и не-транзакционной версий сервиса.
 *
 * @author larin
 */
public interface ReportServiceDelegate {

    String FORMAT_PARAM = "FORMAT";

    /**
     * Remote интерфейс
     *
     * @author larin
     */
    interface Remote extends ReportServiceDelegate {

    }

    /**
     * Синхронная генерация отчета
     */
    ReportResult generate(String name, Map<String, Object> parameters);

    /**
     * Синхронная генерация отчета с принудительным сохранением сформированного отчета
     */
    ReportResult generate(String name, Map<String, Object> parameters, Integer keepDays);

    /**
     * Асинхронная генерация отчета
     */
    @Deprecated
    Future<ReportResult> generateAsync(String name, Map<String, Object> parameters);

    /**
     *
     */
    @Deprecated
    Future<ReportResult> generateAsync(String name, Map<String, Object> parameters, Id queue, String ticket);
}
