package ru.intertrust.cm.core.service.api;

import java.sql.Connection;
import java.util.Map;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * Интерфейс истояника данных для отчета
 * @author larin
 *
 */
public interface ReportDS {
    
    /**
     * Врзвращает источник данных для отчета
     * @param connection
     * @param params
     * @return
     * @throws Exception
     */
	JRBeanCollectionDataSource getJRDataSource(Connection connection, Map params) throws Exception;
}
