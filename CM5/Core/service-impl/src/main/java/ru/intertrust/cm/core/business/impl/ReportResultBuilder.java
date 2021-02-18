package ru.intertrust.cm.core.business.impl;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.export.ooxml.SochiJRDocxExporter;
import net.sf.jasperreports.export.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.GlobalServerSettingsService;
import ru.intertrust.cm.core.business.api.ReportParameterResolver;
import ru.intertrust.cm.core.business.api.ReportServiceDelegate;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.model.ReportServiceException;
import ru.intertrust.cm.core.report.ReportServiceBase;
import ru.intertrust.cm.core.report.ScriptletClassLoader;
import ru.intertrust.cm.core.service.api.ReportDS;
import ru.intertrust.cm.core.service.api.ReportGenerator;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static final String SOCHI_DOCX_FORMAT = "SOCHIDOCX";

    public static final String MASK_NAME = "{name}";  //имя отчёта(из метаданных)
    public static final String MASK_DESCR = "{description}"; //описание отчёта(из метаданных)
    public static final String MASK_LONG_DATE = "{long-date}"; //дата и время построения
    public static final String MASK_SHORT_DATE = "{short-date}"; //дата отчёта
    public static final String MASK_CREATOR = "{creator}"; //логин текущего пользователя
    private static final String DATE_LONG_PATTERN = "dd-MM-yyyy HH:mm:ss";
    private static final String DATE_SHORT_PATTERN = "dd-MM-yyyy";

    @org.springframework.beans.factory.annotation.Value("${default.report.format:PDF}")
    private String defaultReportFormat;
    
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GlobalServerSettingsService globalServerSettingsService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    private Pattern fileNamePattern = Pattern.compile("\\{P\\$([^\\}]*)\\}");

    /**
     * Генерация отчета
     * @param reportMetadata
     * @param templateFolder
     * @param inParams
     * @param dataSource
     * @return
     * @throws Exception
     */
    public File generateReport(ReportMetadataConfig reportMetadata, File templateFolder, Map<String, Object> inParams, DataSourceContext dataSource)
            throws Exception {
        ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();
        Map<String, Object> params = new HashMap<String, Object>();
        //Копируем, чтоб не изменять исходный объект
        if (inParams != null){
            params.putAll(inParams);
        }
        try {

            File templateFile = new File(templateFolder, reportMetadata.getMainTemplate() + ".jasper");
            ScriptletClassLoader scriptletClassLoader =
                    new ScriptletClassLoader(templateFile.getParentFile().getPath(), defaultClassLoader);
            Thread.currentThread().setContextClassLoader(scriptletClassLoader);
            File resultFile = null;
            File resultFolder = getResultFolder();
            
            //Если задан кастомный класс генератора используем его
            if (reportMetadata.getReportGeneratorClass() != null){
                Class<?> generatorClass = scriptletClassLoader.loadClass(reportMetadata.getReportGeneratorClass());
                ReportGenerator reportGenerator =
                        (ReportGenerator) applicationContext.getAutowireCapableBeanFactory().createBean(
                                generatorClass, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
                
                try (InputStream reportStream = reportGenerator.generate(reportMetadata, templateFolder, params)) {
                    resultFile = new File(resultFolder, getReportName(reportMetadata, reportGenerator.getFormat(), inParams));
                    StreamUtils.copy(reportStream, new FileOutputStream(resultFile));
                }
                
            }else{
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
                Exporter exporter = null;
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
                    exporter = new HtmlExporter();
                    extension = HTML_FORMAT;
                } else if (XLSX_FORMAT.equalsIgnoreCase(format)) {
                    exporter = new JRXlsxExporter();
                    extension = XLSX_FORMAT;
                } else if (SOCHI_DOCX_FORMAT.equalsIgnoreCase(format)) {
                    exporter = new SochiJRDocxExporter();
                    extension = DOCX_FORMAT;
                } else {
                    // По умолчанию PDF
                    exporter = new JRPdfExporter();
                    extension = PDF_FORMAT;
                }
    
                String reportName = getReportName(reportMetadata, extension, inParams);
    
                resultFile = new File(resultFolder, reportName);


                SimpleExporterInput simpleExporterInput = new SimpleExporterInput(print);
                exporter.setExporterInput(simpleExporterInput);

                try(FileOutputStream fos = new FileOutputStream(resultFile.getPath())) {
                    ExporterOutput output = null;
                    if (HTML_FORMAT.equalsIgnoreCase(format)){
                        output = new SimpleHtmlExporterOutput(fos);
                    }else {
                        output = new SimpleOutputStreamExporterOutput(fos);
                    }
                    exporter.setExporterOutput(output);
                    exporter.exportReport();
                }
            }

            return resultFile;
        } finally {
            Thread.currentThread().setContextClassLoader(defaultClassLoader);
        }
    }

    private String getReportName(ReportMetadataConfig reportMetadata, String extension, Map<String, Object> inParams){
        String mask = reportMetadata.getFileNameMask();
        if( mask == null || mask.isEmpty() ) {
            mask = globalServerSettingsService.getString("report.global.fileMask");
            if( mask == null || mask.isEmpty() ) {
                // Имя файла отчета по умолчанию
                return reportMetadata.getName() + " " + ThreadSafeDateFormat.format(new Date(), DATE_PATTERN) + "." + extension;
            }
        }

        // Стандартные замены
        String result = mask.replace(MASK_NAME, reportMetadata.getName()).
                replace(MASK_DESCR, reportMetadata.getDescription()).
                replace(MASK_LONG_DATE, ThreadSafeDateFormat.format(new Date(), DATE_LONG_PATTERN)).
                replace(MASK_SHORT_DATE, ThreadSafeDateFormat.format(new Date(), DATE_SHORT_PATTERN)).
                replace(MASK_CREATOR, currentUserAccessor.getCurrentUser());

        // Нужно ли заменять параметры
        if (!StringUtils.isEmpty(reportMetadata.getReportParameterResolver())){
            ReportParameterResolver resolver = (ReportParameterResolver)applicationContext.getBean(reportMetadata.getReportParameterResolver());
            // Нужно подставлять параметры в имя отчета, выполняем поиск параметров в маске
            List<String> paramNames = getParamsInMask(mask);
            for (String paramName: paramNames) {
                result = result.replace("{P$" + paramName + "}", resolver.resolve(reportMetadata.getName(), inParams, paramName));
            }
        }

        result += "." + extension;

        return result;
    }

    /**
     * Получение списка параметров в маске в формате
     * @param mask
     * @return
     */
    private List<String> getParamsInMask(String mask) {
        List<String> result = new ArrayList();
        Matcher matcher = fileNamePattern.matcher(mask);
        while(matcher.find()){
            result.add(matcher.group(1));
        }
        return result;
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
        //Чтобы небыло конфликтов генерим отчеты каждый в своей папке
        File tmpFolder = new File(getTempFolder(), UUID.randomUUID().toString());
        tmpFolder.mkdirs();
        tmpFolder.deleteOnExit();
        return tmpFolder;
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
