package ru.intertrust.cm.core.business.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.ReportExportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StreamUtils;
import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.GlobalServerSettingsService;
import ru.intertrust.cm.core.business.api.ReportParameterResolver;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.business.impl.report.ExporterProvider;
import ru.intertrust.cm.core.business.impl.report.ExporterProviderFactory;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.model.ReportServiceException;
import ru.intertrust.cm.core.report.ReportServiceBase;
import ru.intertrust.cm.core.report.ScriptletClassLoader;
import ru.intertrust.cm.core.service.api.ReportDS;
import ru.intertrust.cm.core.service.api.ReportGenerator;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

/**
 * @author Denis Mitavskiy
 * Date: 09.01.2017
 */
@Stateless
@Interceptors({SpringBeanAutowiringInterceptor.class, ReportsDataSourceSetter.class})
public class ReportResultBuilder extends ReportServiceBase {

    private static final String DATE_PATTERN = "dd_MM_yyyy HH_mm_ss";
    private static final String FORMAT_PARAM = "FORMAT";

    private static final String MASK_NAME = "{name}";  //имя отчёта(из метаданных)
    private static final String MASK_DESCR = "{description}"; //описание отчёта(из метаданных)
    private static final String MASK_LONG_DATE = "{long-date}"; //дата и время построения
    private static final String MASK_SHORT_DATE = "{short-date}"; //дата отчёта
    private static final String MASK_CREATOR = "{creator}"; //логин текущего пользователя
    private static final String DATE_LONG_PATTERN = "dd-MM-yyyy HH:mm:ss";
    private static final String DATE_SHORT_PATTERN = "dd-MM-yyyy";
    private static final Pattern fileNamePattern = Pattern.compile("\\{P\\$([^}]*)}");
    private static final String JDBC_DRIVER = "ru.intertrust.cm.core.jdbc.JdbcDriver";
    private static final String JDBC_SOCHI_LOCAL = "jdbc:sochi:local";

    @Value("${default.report.format:PDF}")
    private String defaultReportFormat;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GlobalServerSettingsService globalServerSettingsService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private ExporterProviderFactory exporterProviderFactory;

    /**
     * Генерация отчета
     */
    @Nonnull
    public ReportFile generateReport(ReportMetadataConfig reportMetadata, File templateFolder, Map<String, Object> inParams, DataSourceContext dataSource)
            throws Exception {
        ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();
        Map<String, Object> params = new HashMap<>();
        //Копируем, чтоб не изменять исходный объект
        if (inParams != null) {
            params.putAll(inParams);
        }
        try {
            File templateFile = new File(templateFolder, reportMetadata.getMainTemplate() + ".jasper");
            ScriptletClassLoader scriptletClassLoader =
                    new ScriptletClassLoader(templateFile.getParentFile().getPath(), defaultClassLoader);
            Thread.currentThread().setContextClassLoader(scriptletClassLoader);

            File resultFolder = getResultFolder();
            ReportFile reportFile;

            //Если задан кастомный класс генератора используем его
            if (reportMetadata.getReportGeneratorClass() != null) {
                Class<?> generatorClass = scriptletClassLoader.loadClass(reportMetadata.getReportGeneratorClass());
                ReportGenerator reportGenerator =
                        (ReportGenerator) applicationContext.getAutowireCapableBeanFactory().createBean(
                                generatorClass, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
                // Имя файла на серверной стороне
                String tmpFileName = getTmpReportName(reportMetadata, reportGenerator.getFormat());
                // Имя файла для записи в базу и веб-клиента
                String reportName = getReportName(reportMetadata, reportGenerator.getFormat(), inParams);
                try (InputStream reportStream = reportGenerator.generate(reportMetadata, templateFolder, params)) {
                    File resultFile = new File(resultFolder, tmpFileName);
                    StreamUtils.copy(reportStream, new FileOutputStream(resultFile));
                    reportFile = new ReportFile(reportName, resultFile);
                }
            } else {
                JasperPrint print = getJasperPrint(reportMetadata, params, templateFile);

                String format = getFormat(reportMetadata, params);
                ExporterProvider exporterProvider = exporterProviderFactory.createExporterProvider(format);
                // Имя файла на серверной стороне
                String tmpFileName = getTmpReportName(reportMetadata,exporterProvider.getExtension());
                // Имя файла для записи в базу и веб-клиента
                String reportName = getReportName(reportMetadata, exporterProvider.getExtension(), inParams);

                ExporterConfiguration configuration = exporterProvider.getConfiguration(params);
                Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> exporter
                        = exporterProvider.getExporter(configuration);

                SimpleExporterInput simpleExporterInput = new SimpleExporterInput(print);
                exporter.setExporterInput(simpleExporterInput);

                File resultFile = new File(resultFolder, tmpFileName);
                try (FileOutputStream fos = new FileOutputStream(resultFile.getPath())) {
                    exporterProvider.setExporterOutput(exporter, fos);
                    exporter.exportReport();
                }
                reportFile = new ReportFile(reportName, resultFile);
            }

            return reportFile;
        } finally {
            Thread.currentThread().setContextClassLoader(defaultClassLoader);
        }
    }

    private JasperPrint getJasperPrint(ReportMetadataConfig reportMetadata, Map<String, Object> params, File templateFile) throws Exception {
        JasperPrint print;
        try (Connection connection = getConnection()) {
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
        }
        return print;
    }

    private String getReportName(ReportMetadataConfig reportMetadata, String extension, Map<String, Object> inParams) {
        String mask = reportMetadata.getFileNameMask();
        if (mask == null || mask.isEmpty()) {
            mask = globalServerSettingsService.getString("report.global.fileMask");
            if (mask == null || mask.isEmpty()) {
                // Имя файла отчета по умолчанию
                return reportMetadata.getName() + ' ' + ThreadSafeDateFormat.format(new Date(), DATE_PATTERN) + '.' + extension;
            }
        }

        // Стандартные замены
        String result = mask.replace(MASK_NAME, reportMetadata.getName()).
                replace(MASK_DESCR, reportMetadata.getDescription()).
                replace(MASK_LONG_DATE, ThreadSafeDateFormat.format(new Date(), DATE_LONG_PATTERN)).
                replace(MASK_SHORT_DATE, ThreadSafeDateFormat.format(new Date(), DATE_SHORT_PATTERN)).
                replace(MASK_CREATOR, currentUserAccessor.getCurrentUser());

        // Нужно ли заменять параметры
        String reportParameterResolver = reportMetadata.getReportParameterResolver();
        if (reportParameterResolver != null && !reportParameterResolver.isEmpty()) {
            ReportParameterResolver resolver = (ReportParameterResolver) applicationContext.getBean(reportMetadata.getReportParameterResolver());
            // Нужно подставлять параметры в имя отчета, выполняем поиск параметров в маске
            List<String> paramNames = getParamsInMask(mask);
            for (String paramName : paramNames) {
                result = result.replace("{P$" + paramName + '}', resolver.resolve(reportMetadata.getName(), inParams, paramName));
            }
        }

        result += '.' + extension;

        return result;
    }

    private String getTmpReportName(ReportMetadataConfig reportMetadata, String extension){
        return reportMetadata.getName() + " " + ThreadSafeDateFormat.format(new Date(), DATE_PATTERN) + "." + extension;
    }

    /**
     * Получение списка параметров в маске в формате
     */
    private List<String> getParamsInMask(String mask) {
        List<String> result = new ArrayList<>();
        Matcher matcher = fileNamePattern.matcher(mask);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        //Загрузка драйвера
        Class.forName(JDBC_DRIVER);
        // Получение соединения с базой данных
        return DriverManager.getConnection(JDBC_SOCHI_LOCAL);
    }

    private File getResultFolder() throws IOException {
        //Чтобы не было конфликтов генерим отчеты каждый в своей папке
        File tmpFolder = new File(getTempFolder(), UUID.randomUUID().toString());
        tmpFolder.mkdirs();
        tmpFolder.deleteOnExit();
        return tmpFolder;
    }

    /**
     * Получение формата отчета
     */
    private String getFormat(ReportMetadataConfig reportMetadata, Map<String, Object> params) {
        //Если формат задан в шаблоне и он только один - то применяем его
        final List<String> formats = reportMetadata.getFormats();
        if (formats != null && formats.size() == 1) {
            return formats.get(0);
        }
        //Если задано несколько форматов, то сначала применяем формат из параметра, а если там не задан берем формат по умолчанию
        String format = null;
        if (params != null) {
            format = (String) params.get(FORMAT_PARAM);
        }
        //Берем формат по умолчанию
        if (format == null) {
            format = defaultReportFormat;
        }
        //Проверяем есть ли такой формат в списке поддерживаемых форматов
        if (formats != null && !formats.contains(format)) {
            throw new ReportServiceException("FORMAT parameter or default report format is not admissible. Need " + formats);
        }
        return format;
    }

    public static class ReportFile {
        // Имя файла для записи в базу и веб-клиента
        private String reportFileName;
        // Файл на серверной стороне
        private File reportFile;

        public ReportFile(String reportFileName, File reportFile) {
            setReportFileName(reportFileName);
            setReportFile(reportFile);
        }

        public String getReportFileName() {
            return reportFileName;
        }

        public void setReportFileName(String reportFileName) {
            this.reportFileName = correctFileName(reportFileName);
        }

        public File getReportFile() {
            return reportFile;
        }

        public void setReportFile(File reportFile) {
            this.reportFile = reportFile;
        }

        private String correctFileName(String fileName) {
            // Замена недопустимых символов в имени файла
            // NUL, \, /, :, *, ", <, >, |
            String outFileName = fileName != null ?
                    fileName.replaceAll("[\\\\/\\:\\*\\\"\\<\\>\\|\\n\\r\\t]+", "-") :
                    "rpt-file";
            return "NUL".equals(outFileName) ? "rpt-file" : outFileName;
        }
    }
}
