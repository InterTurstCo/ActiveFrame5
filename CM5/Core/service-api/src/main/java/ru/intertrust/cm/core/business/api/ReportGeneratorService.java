package ru.intertrust.cm.core.business.api;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Сервис генератора отчетов
 */
public interface ReportGeneratorService {

    /**
     * Remote интерфейс
     */
    public interface Remote extends ReportGeneratorService {
    }

    /**
     * Генерация отчета в формате XLS
     * @param title - заголовок для всей таблицы
     * @param columns - коллекция колонок в формате идентификатор колонки - заголовок колонки, порядок следования колонок важен
     * @param data - данные по строкам в формате идентификатор колонки - данные для колонки и строки
     * @return поток связанный со сформированным xls-файлом
     */
    InputStream generateXLS(String title, Map<String, String> columns, List<Map<String, Object>> data) throws Exception;

}
