package ru.intertrust.cm.core.service.api;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import ru.intertrust.cm.core.config.model.ReportMetadataConfig;

/**
 * Интерфейс кастомного генератора отчета. 
 * @author larin
 *
 */
public interface ReportGenerator {
    
    /**
     * Сформировать отчет
     * @param reportMetadata
     * @param templateFolder
     * @param parameters
     * @return
     */
    InputStream generate(ReportMetadataConfig reportMetadata, File templateFolder, Map<String, Object> parameters);

    /**
     * Получение формата отчета. Формат отчета будет далее использован как расширение в результирующем файле
     * @return
     */
    String getFormat();
}
