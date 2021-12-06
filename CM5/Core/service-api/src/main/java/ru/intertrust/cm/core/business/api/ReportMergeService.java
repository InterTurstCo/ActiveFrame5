package ru.intertrust.cm.core.business.api;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Сервис слияния отчетов
 */
public interface ReportMergeService {
    /**
     * Remote интерфейс
     */
    public interface Remote extends ReportMergeService {
    }

    /**
     * Слияние отчетов
     * @param formats список форматов (в текущей версии - DOCX)
     * @param reportFiles список файлов для слияния
     * @param outFile выходной файл
     * @return поток связанный с выходным файлом
     * @throws Exception
     */
    InputStream mergeReports(List<String> formats, List<File> reportFiles, File outFile) throws Exception;

}
