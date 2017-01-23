package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.DeployReportData;

/**
 * Административный интерфейс работы с отчетами
 * @author larin
 *
 */
public interface ReportServiceAdmin {
    public static final String METADATA_FILE_MAME = "template.xml";
    /**
     * Remote интерфейс
     * @author larin
     *
     */
    public interface Remote extends ReportServiceAdmin{        
    }
    
    /**
     * Установка отчета в систему
     * @param deployReportData
     */
    void deploy(DeployReportData deployReportData,  boolean lockUpdate);

    /**
     * Удаление отчета из системы
     * @param name
     */
    void undeploy(String name);

    /**
     * Перекомпиляция всех установленных отчетов
     * @param name
     */
    void recompileAll();

}
