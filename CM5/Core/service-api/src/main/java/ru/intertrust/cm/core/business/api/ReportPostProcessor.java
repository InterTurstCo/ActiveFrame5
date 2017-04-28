package ru.intertrust.cm.core.business.api;

import java.io.File;


/**
 * Интерфейс пост обработки файла отчёта
 * @author lyakin
 *
 */
public interface ReportPostProcessor {
	 /**
     * Формирование файла отчёта
     * @param reportFile
     * @return
     */
    void format(File reportFile);
}
