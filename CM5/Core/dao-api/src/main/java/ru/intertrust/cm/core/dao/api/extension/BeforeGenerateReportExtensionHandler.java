package ru.intertrust.cm.core.dao.api.extension;

import java.util.Map;

/**
 * Точка расширения до генерации отчета. Можно здесь поменять параметры
 * @author larin
 *
 */
public interface BeforeGenerateReportExtensionHandler extends ExtensionPointHandler{

    /**
     * Точка расширения до генерации отчета
     * @param name имя отчета
     * @param parameters параметры
     */
    void onBeforeGenerateReport(String name, Map<String, Object> parameters);
}
