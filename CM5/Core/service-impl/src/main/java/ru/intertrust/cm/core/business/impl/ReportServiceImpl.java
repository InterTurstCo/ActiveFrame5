package ru.intertrust.cm.core.business.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.annotation.Resource;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.GlobalServerSettingsService;
import ru.intertrust.cm.core.business.api.ReportPostProcessor;
import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.ReportResult;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.config.model.ReportParameter;
import ru.intertrust.cm.core.config.model.ReportParameterData;
import ru.intertrust.cm.core.config.model.ReportParametersData;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.TicketService;
import ru.intertrust.cm.core.dao.api.extension.AfterGenerateReportExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.BeforeGenerateReportExtensionHandler;
import ru.intertrust.cm.core.model.ReportServiceException;
import ru.intertrust.cm.core.report.ReportServiceBase;
import ru.intertrust.cm.core.rest.api.GenerateReportParam;
import ru.intertrust.cm.core.service.api.ReportTemplateCache;

/**
 * Реализация сервиса генерации отчетов
 *
 * @author larin
 */
public abstract class ReportServiceImpl extends ReportServiceBase implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    protected ReportTemplateCache templateCache;

    @Autowired
    private ExtensionService extensionService;

    @Resource
    private EJBContext ejbContext;

    @Value("${default.report.format:PDF}")
    private String defaultReportFormat;

    @Value("${report.server:false}")
    private boolean reportServer;

    //Таймаут генерации отчета на сервере отчетов. По умолчанию 1 час
    @Value("${report.server.generation.timeout:3600}")
    private long reportServerTimeout;

    @EJB
    private ReportResultBuilder resultBuilder;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private GlobalServerSettingsService globalServerSettingsService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public ReportResult generate(String name, Map<String, Object> parameters, DataSourceContext dataSource) {
        return generate(name, parameters, null, dataSource);
    }

    @Override
    @Asynchronous
    public Future<ReportResult> generateAsync(String name, Map<String, Object> parameters, DataSourceContext dataSource) {
        return generateAsync(name, parameters);
    }

    @Override
    @Asynchronous
    public Future<ReportResult> generateAsync(String name, Map<String, Object> parameters) {
        ReportResult result = generate(name, parameters);
        return new AsyncResult<>(result);
    }

    @Override
    @Asynchronous
    public Future<ReportResult> generateAsync(String name, Map<String, Object> parameters, Id queueId, String ticket) {
        ReportResult result = generate(name, parameters);
        return new AsyncResult<>(result);
    }

    @Override
    public ReportResult generate(String name, Map<String, Object> parameters) {
        return generate(name, parameters, (DataSourceContext) null);
    }

    /**
     * Формирование отчета
     */
    @Override
    public ReportResult generate(String name, Map<String, Object> parameters, Integer keepDays) {
        return generate(name, parameters, keepDays, null);
    }

    /**
     * Формирование отчета
     */
    @Override
    public ReportResult generate(String name, Map<String, Object> callParameters, Integer keepDays, DataSourceContext dataSource) {
        //Создаем копию параметров, так как они могут модифицироваться
        Map<String, Object> parameters;
        if (callParameters == null) {
            parameters = new HashMap<>();
        } else {
            parameters = new HashMap<>(callParameters);
        }
        //Для отладки =================
        /*boolean server = false;
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            if (stackTraceElement.getMethodName().contains("generateAsync")) {
                server = true;
                break;
            }
        }
        reportServer = server;*/
        //Для отладки =================

        String reportServerUrl = globalServerSettingsService.getString("report.server.url");
        if (!reportServer && reportServerUrl != null && !reportServerUrl.equalsIgnoreCase("local")) {
            return processByReportServer(name, parameters, reportServerUrl);
        }
        return processLocally(name, parameters, keepDays, dataSource);
    }

    private ReportResult processByReportServer(String name, Map<String, Object> parameters, String reportServerUrl) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(getClass().getName());
        GenerateReportParam generateReportParam = new GenerateReportParam();
        generateReportParam.setName(name);
        generateReportParam.setParams(parameters);

        HttpHeaders headers = new HttpHeaders();
        headers.add(ReportService.TICKET_HEADER, ticketService.createTicket());

        HttpEntity<GenerateReportParam> entity = new HttpEntity<>(generateReportParam, headers);

        Id queueId = restTemplate.postForObject(reportServerUrl + "/af5-ws/report/generate", entity, Id.class);

        //Ждем смены статуса, но не более
        IdentifiableObject queue = null;
        final List<ReferenceValue> params = Collections.singletonList(new ReferenceValue(queueId));
        final Id runId = statusDao.getStatusIdByName("Run");
        final long timeout = reportServerTimeout * 1000;
        long start = System.currentTimeMillis();
        while ((System.currentTimeMillis() - start) < timeout) {
            //Получаем объект очереди
            queue = collectionsDao.findCollectionByQuery("select id, status, file_name, name, result_id, error from generate_report_queue where id = {0}",
                    params, 0, 1, accessToken).get(0);
            if (queue.getReference("status").equals(runId)) {
                logger.debug("Status queue {} is Run. Waiting...", queue.getId().toStringRepresentation());
            } else {
                logger.debug("Status queue {} is {}. End wait.",
                        queue.getId().toStringRepresentation(), statusDao.getStatusNameById(queue.getReference("status")));
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                logger.error("Error wait generate report", ex);
            }
        }
        if (queue == null) {
            String message = "Error on generate report on report server. Queue is null";
            logger.error(message);
            throw new ReportServiceException(message);
        }
        final Id reportStatus = queue.getReference("status");
        if (reportStatus.equals(statusDao.getStatusIdByName("Fault"))) {
            String message = "Error on generate report on report server " + queue.getString("error");
            logger.error(message);
            throw new ReportServiceException(message);
        }
        if (!reportStatus.equals(statusDao.getStatusIdByName("Complete"))) { //Таймаут
            String message = "Error on generate report on report server. Timeout";
            logger.error(message);
            throw new ReportServiceException(message);
        }
        final ReportResult reportResult = new ReportResult();
        reportResult.setFileName(queue.getString("file_name"));
        reportResult.setTemplateName(queue.getString("name"));
        reportResult.setResultId(queue.getReference("result_id"));

        RequestCallback requestCallback = request -> request.getHeaders().add(ReportService.TICKET_HEADER, ticketService.createTicket());

        ResponseExtractor<Void> responseExtractor = response -> {
            File tempFile = File.createTempFile("report_", "_file");
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                StreamUtils.copy(response.getBody(), out);
            }
            reportResult.setReport(getReportStream(new FileInputStream(tempFile)));
            return null;
        };
        restTemplate.execute(reportServerUrl + "/af5-ws/report/content/{reportId}",
                HttpMethod.GET, requestCallback, responseExtractor, reportResult.getResultId().toStringRepresentation());
        return reportResult;
    }

    private ReportResult processLocally(String name, Map<String, Object> parameters, Integer keepDays, DataSourceContext dataSource) {
        Map<String, Object> originalParams = parameters;
        long t1 = 0;
        long heap = 0;
        LoggingThread loggingThread = null;
        if (logger.isDebugEnabled()) {
            heap = Runtime.getRuntime().totalMemory();
            t1 = System.currentTimeMillis();
            originalParams = new HashMap<>(parameters);
            logger.debug("Executing Report, name: {}. Params: {}, User: {}", name, originalParams, currentUserAccessor.getCurrentUser());
            loggingThread = new LoggingThread(name, originalParams, heap);
            loggingThread.start();
        }
        final UserTransaction userTransaction = ejbContext.getUserTransaction();
        try {
            userTransaction.begin();
            // Получение доменного объекта шаблона отчета
            DomainObject reportTemplate = getReportTemplateObject(name);

            //Получение директории с шаблонами отчета
            File templateFolder = templateCache.getTemplateFolder(reportTemplate);

            //Получение метаинформацию отчета
            ReportMetadataConfig reportMetadata = loadReportMetadata(
                    readFile(new File(templateFolder, ReportServiceAdmin.METADATA_FILE_MAME)));

            //Применяем параметры по умолчанию
            if (reportMetadata.getParameters() != null) {
                for (ReportParameter defaultParameter : reportMetadata.getParameters()) {
                    parameters.put(defaultParameter.getName(), defaultParameter.getValue());
                }
            }
            //Вызов точки расширения до генерации отчета
            //Сначала для точек расширения у которых указан фильтр
            BeforeGenerateReportExtensionHandler beforeExtentionHandler =
                    extensionService.getExtensionPoint(BeforeGenerateReportExtensionHandler.class, name);
            beforeExtentionHandler.onBeforeGenerateReport(name, parameters);
            //Вызов точек расширения у кого не указан фильтр
            beforeExtentionHandler = extensionService.getExtensionPoint(BeforeGenerateReportExtensionHandler.class, "");
            beforeExtentionHandler.onBeforeGenerateReport(name, parameters);

            //Формирование отчета
            // todo: this method should accept DataSource and if it's MASTER - support transaction
            ReportResultBuilder.ReportFile result = resultBuilder.generateReport(reportMetadata, templateFolder, parameters, dataSource);
            if (logger.isDebugEnabled()) {
                logger.debug("Generated Report, name: {}. Params: {}", name, originalParams);
            }
            //Вызов пост-обработчиков, указанных в конфигурации отчёта
            String format = reportMetadata.getFormats() != null && !reportMetadata.getFormats().isEmpty() ?
                    reportMetadata.getFormats().get(0) : defaultReportFormat;
            if (format.equals("SOCHIDOCX")) {
                List<String> postProcessorsList = reportMetadata.getPostProcessors();
                if (postProcessorsList != null) {
                    for (String postProcName : postProcessorsList) {
                        ReportPostProcessor postProcClass = (ReportPostProcessor) applicationContext.getBean(postProcName);
                        postProcClass.format(result.getReportFile());
                    }
                }
            }
            //Вызов точки расширения после генерации отчета
            //Сначала для точек расширения у которых указан фильтр
            AfterGenerateReportExtentionHandler extensionHandler =
                    extensionService.getExtensionPoint(AfterGenerateReportExtentionHandler.class, name);
            extensionHandler.onAfterGenerateReport(name, parameters, result.getReportFile());
            //После для точек расширения у которых не указан фильтр
            extensionHandler = extensionService.getExtensionPoint(AfterGenerateReportExtentionHandler.class, "");
            extensionHandler.onAfterGenerateReport(name, parameters, result.getReportFile());

            if (logger.isDebugEnabled()) {
                logger.debug("Saving Report, name: {}. Params: {}", name, originalParams);
            }
            //Сохранение результата в хранилище
            Id resultId = saveResult(reportMetadata, result, reportTemplate, parameters, keepDays);

            //Формирование результата
            if (logger.isDebugEnabled()) {
                logger.debug("Creating Report result, name: {}. Params: {}", name, originalParams);
            }
            ReportResult reportResult = new ReportResult();
            reportResult.setFileName(result.getReportFileName());
            reportResult.setReport(getReportStream(new FileInputStream(result.getReportFile())));
            reportResult.setTemplateName(name);
            reportResult.setResultId(resultId);

            //Удаляем временный файл
            result.getReportFile().delete();
            userTransaction.commit();

            return reportResult;
        } catch (Throwable ex) {
            try {
                if (userTransaction.getStatus() == Status.STATUS_ACTIVE) {
                    userTransaction.rollback();
                }
            } catch (Exception ignoreEx) {
                logger.warn("Error rollback transaction", ignoreEx);
            }
            logger.error("Error generate report", ex);
            throw new ReportServiceException("Error on generate report", ex);
        } finally {
            if (loggingThread != null) {
                loggingThread.cancel();
            }
            if (logger.isDebugEnabled()) {
                long time = System.currentTimeMillis() - t1;
                long heapDelta = (Runtime.getRuntime().totalMemory() - heap) / 1024 / 1024;
                logger.debug("Report built, name: {}. Params: {}. Time: {} ms. Heap delta: {} MB ", name, originalParams, time, heapDelta);
            }
        }
    }

    protected abstract RemoteInputStream getReportStream(InputStream report);

    private Id saveResult(ReportMetadataConfig reportMetadata, ReportResultBuilder.ReportFile result, DomainObject template,
                          Map<String, Object> params, Integer keepDays) throws Exception {
        Id resultId = null;
        if (result != null) {
            if ((keepDays != null && keepDays > 0) || (reportMetadata.getKeepDays() != null && reportMetadata.getKeepDays() > 0)) {
                AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

                //Создаем объект
                DomainObject reportResult = createDomainObject("report_result");
                reportResult.setString("name", result.getReportFileName());
                reportResult.setReference("template_id", template.getId());
                reportResult.setReference("owner", currentUserAccessor.getCurrentUserId());
                Calendar calendar = Calendar.getInstance();
                if (keepDays != null && keepDays > 0) {
                    calendar.add(Calendar.DAY_OF_MONTH, keepDays);
                } else {
                    calendar.add(Calendar.DAY_OF_MONTH, reportMetadata.getKeepDays());
                }
                reportResult.setTimestamp("keep_to", calendar.getTime());
                reportResult = domainObjectDao.save(reportResult, accessToken);
                resultId = reportResult.getId();

                //Сохраняем результат как вложение вложения
                DomainObject reportAttachment =
                        attachmentService.createAttachmentDomainObjectFor(reportResult.getId(), "report_result_attachment");
                reportAttachment.setString("name", "report");

                DirectRemoteInputStream directRemoteInputStream = new DirectRemoteInputStream(new FileInputStream(result.getReportFile()), false);

                attachmentService.saveAttachment(directRemoteInputStream, reportAttachment);

                //Сохраняем параметры как вложение
                if (params != null) {
                    DomainObject paramAttachment =
                            attachmentService.createAttachmentDomainObjectFor(reportResult.getId(), "report_result_attachment");
                    paramAttachment.setString("name", "params");

                    ByteArrayInputStream bis = new ByteArrayInputStream(getParametersAsByteArray(params));
                    directRemoteInputStream = new DirectRemoteInputStream(bis, false);

                    attachmentService.saveAttachment(directRemoteInputStream, paramAttachment);
                }
            }
        }
        return resultId;
    }

    private byte[] getParametersAsByteArray(Map<String, Object> params) throws Exception {
        ReportParametersData data = new ReportParametersData();
        data.setParameters(new ArrayList<>());

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

    private static class LoggingThread extends Thread {

        private static final int TO_MB = 1024 * 1024;
        private boolean stopped = false;
        private final long t1;
        private final String name;
        private final Map<?, ?> params;
        private final long heap;

        LoggingThread(String name, Map<?, ?> params, long heap) {
            this.name = name;
            this.params = params;
            this.heap = heap;
            this.t1 = System.currentTimeMillis();
        }

        @Override
        public void run() {
            while (!stopped) {
                long heapDelta = (Runtime.getRuntime().totalMemory() - this.heap) / TO_MB;
                logger.debug("Report, name: {}. Params: {} is running for {} seconds. Heap delta: {} MB ",
                        this.name, this.params, (System.currentTimeMillis() - this.t1) / 1000, heapDelta);
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Thread is Interrupted", e);
                }
            }
        }

        public void cancel() {
            stopped = true;
        }
    }
}
