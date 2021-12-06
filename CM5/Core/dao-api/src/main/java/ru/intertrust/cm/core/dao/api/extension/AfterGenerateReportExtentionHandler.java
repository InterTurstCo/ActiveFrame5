package ru.intertrust.cm.core.dao.api.extension;

import java.io.File;
import java.util.Map;

/**
 * Точка расширения, вызываемая после генерации отчета
 * @author larin
 *
 */
public interface AfterGenerateReportExtentionHandler extends ExtensionPointHandler{

    /**
     * Обработчик точки расширения. Файл сформированного отчета можно поменять, сохранив его в том же месте что и переданный в параметре с заменой.
     * @param name
     * @param parameters
     * @param reportFile
     */
    void onAfterGenerateReport(String name, Map<String, Object> parameters, File reportFile);
}
