package ru.intertrust.cm.core.business.api;

import java.io.File;
import javax.annotation.Nonnull;

import ru.intertrust.cm.core.business.api.dto.DeployReportData;

/**
 * Административный интерфейс работы с отчетами
 *
 * @author larin
 */
public interface ReportServiceAdmin {

    String METADATA_FILE_MAME = "template.xml";

    /**
     * Remote интерфейс
     *
     * @author larin
     */
    interface Remote extends ReportServiceAdmin {

    }

    /**
     * Установка отчета в систему
     */
    void deploy(DeployReportData deployReportData, boolean lockUpdate);

    /**
     * Удаление отчета из системы
     */
    void undeploy(String name);

    /**
     * Пере-компиляция всех установленных отчетов
     */
    void recompileAll();

    /**
     * Импорт пакета отчетов
     *
     * @param reportPackageFile - файл пакета отчетов
     */
    void importReportPackage(@Nonnull File reportPackageFile) throws Exception;
}
