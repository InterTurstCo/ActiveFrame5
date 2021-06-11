package ru.intertrust.cm.core.service.api;

import java.sql.Connection;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;

/**
 * Интерфейс источника данных для отчета
 *
 * @author larin
 */
public interface ReportDS {

    /**
     * Возвращает источник данных для отчета
     */
    JRDataSource getJRDataSource(Connection connection, Map<String, Object> params) throws Exception;
}
