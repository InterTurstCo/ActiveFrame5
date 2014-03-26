package ru.intertrust.cm.core.business.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.ReportResult;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.config.model.ReportParameterData;
import ru.intertrust.cm.core.config.model.ReportParametersData;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.model.ReportServiceException;
import ru.intertrust.cm.core.report.ReportServiceBase;
import ru.intertrust.cm.core.report.ScriptletClassLoader;
import ru.intertrust.cm.core.service.api.ReportDS;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

/**
 * Имплементация сервиса генерации отчетов
 * @author larin
 * 
 */
@Stateless(name = "ReportService")
@Local(ReportService.class)
@Remote(ReportService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ReportServiceImpl extends ReportServiceBase implements ReportService {

    private static final String TEMPLATES_FOLDER_NAME = "report-service-templates";
    private static final String RESULT_FOLDER_NAME = "report-service-results";

    public static final String PDF_FORMAT = "PDF";
    public static final String RTF_FORMAT = "RTF";
    public static final String XLS_FORMAT = "XLS";
    public static final String HTML_FORMAT = "HTML";
    public static final String DOCX_FORMAT = "DOCX";
    public static final String XLSX_FORMAT = "XLSX";

    @Autowired
    private CurrentUserAccessor currentUserAccessor;
    
    @Resource
    private EJBContext ejbContext;

    /**
     * Формирование отчета
     */
    @Override
    public ReportResult generate(String name, Map<String, Object> parameters) {
        try {
            // Получение доменного объекта шаблона отчета
            DomainObject reportTemplate = getReportTemplateObject(name);

            //Получение директории с шаблонами отчета
            File templateFolder = getTemplateFolder(reportTemplate);

            //Получение метаинформацию отчета
            ReportMetadataConfig reportMetadata = loadReportMetadata(
                    readFile(new File(templateFolder, ReportServiceAdmin.METADATA_FILE_MAME)));

            //Формирование отчета
            File result = generateReport(reportMetadata, templateFolder, parameters);

            //Сохранеие результата в хранилище
            saveResult(reportMetadata, result, reportTemplate, parameters);

            //Формироание результата
            ReportResult reportResult = new ReportResult();
            reportResult.setFileName(result.getName());
            reportResult.setReport(readFile(result));
            reportResult.setTemplateName(name);

            //Удаляем временный файл
            result.delete();
            
            return reportResult;
        } catch (Exception ex) {
            throw new ReportServiceException("Error on generate report", ex);
        }
    }

    private void saveResult(ReportMetadataConfig reportMetadata, File result, DomainObject template,
            Map<String, Object> params) throws Exception {
        if (reportMetadata.getKeepDays() != null && reportMetadata.getKeepDays() > 0) {
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

            //Создаем объект
            DomainObject reportResult = createDomainObject("report_result");
            reportResult.setString("name", result.getName());
            reportResult.setReference("template_id", template.getId());
            reportResult.setReference("owner", currentUserAccessor.getCurrentUserId());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, reportMetadata.getKeepDays());
            reportResult.setTimestamp("keep_to", null);
            domainObjectDao.save(reportResult, accessToken);

            //Сохраняем результат как вложение вложения
            DomainObject reportAttachment =
                    attachmentService.createAttachmentDomainObjectFor(reportResult.getId(), "Attachment");
            ByteArrayInputStream bis = new ByteArrayInputStream(readFile(result));
            SimpleRemoteInputStream simpleRemoteInputStream = new SimpleRemoteInputStream(bis);

            RemoteInputStream remoteInputStream;
            remoteInputStream = simpleRemoteInputStream.export();
            attachmentService.saveAttachment(remoteInputStream, reportAttachment);

            //Сохраняем параметры как вложение
            DomainObject paramAttachment =
                    attachmentService.createAttachmentDomainObjectFor(reportResult.getId(), "Attachment");
            bis = new ByteArrayInputStream(getParametersAsByteArray(params));
            simpleRemoteInputStream = new SimpleRemoteInputStream(bis);

            remoteInputStream = simpleRemoteInputStream.export();
            attachmentService.saveAttachment(remoteInputStream, paramAttachment);
        }
    }

    private byte[] getParametersAsByteArray(Map<String, Object> params) throws Exception {
        ReportParametersData data = new ReportParametersData();
        data.setParameters(new ArrayList<ReportParameterData>());

        for (String name : params.keySet()) {
            //TODO в зависимости от того как будут использовать файл с параметрами возможно
            //потребуется сохранять значения не как строки а как конкретные объекты
            data.getParameters().add(new ReportParameterData(name, params.get(name).toString()));
        }

        Serializer serializer = new Persister();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        serializer.write(data, stream);

        return stream.toByteArray();
    }

    private File getTemplateFolder(DomainObject reportTemplateDo) throws IOException {
        //Проверка есть директория для данного отчета в файловой системе, и если есть то проверка даты ее создания
        //Получение temp директории
        File tempFolder = getTempFolder();

        //Получение директории с шаблонами
        File templatesFolder = new File(tempFolder, TEMPLATES_FOLDER_NAME);
        File templateFolder = new File(templatesFolder, reportTemplateDo.getString("name"));
        boolean dirCreated = false; 
        if (!templateFolder.exists()) {
            templateFolder.mkdirs();
            dirCreated = true;
        }

        //Сравнение даты изменения директории и даты создания доменного объекта шаблонов отчета 
        if (dirCreated || templateFolder.lastModified() < reportTemplateDo.getModifiedDate().getTime()) {
            //Шаблоны требуют перезачитывания
            //Удаляем все содержимое папки
            File[] files = templateFolder.listFiles();
            for (File file : files) {
                file.delete();
            }
            
            //Получение всех вложений
            List<DomainObject> attachments = getAttachments("report_template_attach", reportTemplateDo);
            for (DomainObject attachment : attachments) {
                byte[] content = getAttachmentContent(attachment);
                //Запись файла на диск
                writeToFile(content, new File(templateFolder, attachment.getString("Name")));
            }
            templateFolder.setLastModified(System.currentTimeMillis());
        }
        return templateFolder;

    }

    @Override
    @Asynchronous
    public Future<ReportResult> generateAsync(String name, Map<String, Object> parameters) {
        String user = ejbContext.getCallerPrincipal().getName();
        ReportResult result = generate(name, parameters);
        return new AsyncResult<ReportResult>(result);
    }

    /**
     * Генерация отчета
     * @param templatePath
     * @param format
     * @param params
     * @param resultFolder
     * @param reportNameTemplate
     * @param reportDSClassName
     * @return
     * @throws Exception
     */
    private File generateReport(ReportMetadataConfig reportMetadata, File templateFolder, Map<String, Object> params)
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

            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "dd_MM_yyyy HH_mm_ss");
            String reportName = reportMetadata.getName() + " ";
            reportName += dateFormat.format(new Date()) + "." + extension;

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

    private File getResultFolder() throws IOException {
        File resultFolder = new File(getTempFolder(), RESULT_FOLDER_NAME);
        if (!resultFolder.exists()) {
            resultFolder.mkdirs();
        }
        return resultFolder;
    }

    private String getFormat(ReportMetadataConfig reportMetadata, Map<String, Object> params) {
        String formatParam = null;
        if (params != null){
            formatParam = (String) params.get(FORMAP_PARAM);
        }
        String format = null;
        if (formatParam == null && reportMetadata.getFormats().size() > 1) {
            throw new ReportServiceException("FORMAT parameter is required");
        }

        if (formatParam != null) {
            if (!reportMetadata.getFormats().contains(formatParam)) {
                throw new ReportServiceException("FORMAT parameter is not admissible. Need "
                        + reportMetadata.getFormats());
            }
        }

        if (formatParam != null) {
            format = formatParam;
        } else {
            format = reportMetadata.getFormats().get(0);
        }

        return format;
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        String connectionString = null;
        connectionString = "jdbc:sochi:local";

        //Загрузка драйвера
        Class.forName("ru.intertrust.cm.core.jdbc.JdbcDriver");
        // Получение соединения с базой данных
        return DriverManager.getConnection(connectionString);
    }
}
