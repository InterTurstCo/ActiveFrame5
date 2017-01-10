package ru.intertrust.cm.core.business.impl;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.ReportServiceDelegate;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.model.ReportServiceException;
import ru.intertrust.cm.core.report.ReportServiceBase;
import ru.intertrust.cm.core.report.ScriptletClassLoader;
import ru.intertrust.cm.core.service.api.ReportDS;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 09.01.2017
 *         Time: 18:53
 */
@Stateless(name = "NonTransactionalReportService")
@Interceptors({SpringBeanAutowiringInterceptor.class, ReportsDataSourceSetter.class})
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ReportResultBuilder extends ReportServiceBase {
    private static final String DATE_PATTERN = "dd_MM_yyyy HH_mm_ss";
    public static final String PDF_FORMAT = "PDF";
    public static final String RTF_FORMAT = "RTF";
    public static final String XLS_FORMAT = "XLS";
    public static final String HTML_FORMAT = "HTML";
    public static final String DOCX_FORMAT = "DOCX";
    public static final String XLSX_FORMAT = "XLSX";

    @org.springframework.beans.factory.annotation.Value("${default.report.format:PDF}")
    private String defaultReportFormat;

    /**
     * Генерация отчета
     * @param templatePath
     * @param format
     * @param resultFolder
     * @param reportNameTemplate
     * @param reportDSClassName
     * @param params
     * @param dataSource
     * @return
     * @throws Exception
     */
    public File generateReport(ReportMetadataConfig reportMetadata, File templateFolder, Map<String, Object> params, DataSourceContext dataSource)
            throws Exception {
        ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();
        try {

            File templateFile = new File(templateFolder, reportMetadata.getMainTemplate() + ".jasper");
            ScriptletClassLoader scriptletClassLoader =
                    new ScriptletClassLoader(templateFile.getParentFile().getPath(), defaultClassLoader);
            Thread.currentThread().setContextClassLoader(scriptletClassLoader);

            Connection connection = getConnection();
            JasperPrint print = null;
            if (reportMetadata.getDataSourceClass() == null) {
                print = JasperFillManager.fillReport(templateFile.getPath(), params, connection);
            } else {
                Class<?> reportDSClass = Thread.currentThread()
                        .getContextClassLoader().loadClass(reportMetadata.getDataSourceClass());
                ReportDS reportDS = (ReportDS) reportDSClass.newInstance();
                JRDataSource ds = reportDS.getJRDataSource(
                        connection, params);
                params.put(JRParameter.REPORT_CONNECTION, connection);
                print = JasperFillManager.fillReport(templateFile.getPath(), params, ds);
            }
            connection.close();
            JRExporter exporter = null;
            String extension = null;

            String format = getFormat(reportMetadata, params);

            if (RTF_FORMAT.equalsIgnoreCase(format)) {
                exporter = new JRRtfExporter();
                extension = RTF_FORMAT;
            } else if (DOCX_FORMAT.equalsIgnoreCase(format)) {
                exporter = new JRDocxExporter();
                extension = DOCX_FORMAT;
            } else if (XLS_FORMAT.equalsIgnoreCase(format)) {
                exporter = new JRXlsExporter();
                extension = XLS_FORMAT;
            } else if (HTML_FORMAT.equalsIgnoreCase(format)) {
                exporter = new JRHtmlExporter();
                extension = HTML_FORMAT;
            } else if (XLSX_FORMAT.equalsIgnoreCase(format)) {
                exporter = new JRXlsxExporter();
                extension = XLSX_FORMAT;
            } else {
                // По умолчанию PDF
                exporter = new JRPdfExporter();
                extension = PDF_FORMAT;
            }

            File resultFolder = getResultFolder();

            String reportName = reportMetadata.getName() + " ";
            reportName += ThreadSafeDateFormat.format(new Date(), DATE_PATTERN) + "." + extension;

            /*if (HTML_FORMAT.equalsIgnoreCase(format)) {
                exporter.setParameter(
                        JRHtmlExporterParameter.IMAGES_URI,
                        "image?report="
                                + URLEncoder.encode(reportName, "UTF-8")
                                + "&image=");
            }*/

            File resultFile = new File(resultFolder, reportName);

            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
            exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME,
                    resultFile.getPath());
            exporter.exportReport();

            return resultFile;
        } finally {
            Thread.currentThread().setContextClassLoader(defaultClassLoader);
        }
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        String connectionString = null;
        connectionString = "jdbc:sochi:local";

        //Загрузка драйвера
        Class.forName("ru.intertrust.cm.core.jdbc.JdbcDriver");
        // Получение соединения с базой данных
        return DriverManager.getConnection(connectionString);
    }

    private File getResultFolder() throws IOException {
        /*File resultFolder = new File(getTempFolder(), RESULT_FOLDER_NAME);
        if (!resultFolder.exists()) {
            resultFolder.mkdirs();
        }
        return resultFolder;*/
        return getTempFolder();
    }

    /**
     * Получение формата отчета
     * @param reportMetadata
     * @param params
     * @return
     */
    private String getFormat(ReportMetadataConfig reportMetadata, Map<String, Object> params) {

        String format = null;
        //Если формат задан в шаблоне и он только один - то применяем его
        if (reportMetadata.getFormats() != null && reportMetadata.getFormats().size() == 1){
            format = reportMetadata.getFormats().get(0);
        }else{
            //Если задано несколько форматов то сначала применяем формат из параметра а если там не задан берем формат по умолчанию
            if (params != null) {
                format = (String) params.get(ReportServiceDelegate.FORMAT_PARAM);
            }

            //Берем формат по умолчанию
            if (format == null){
                format = defaultReportFormat;
            }

            //Проверяем есть ли такой формат в списке поддерживаемых форматов
            if (!reportMetadata.getFormats().contains(format)) {
                throw new ReportServiceException("FORMAT parameter or default report format is not admissible. Need "
                        + reportMetadata.getFormats());
            }
        }

        return format;
    }
}
