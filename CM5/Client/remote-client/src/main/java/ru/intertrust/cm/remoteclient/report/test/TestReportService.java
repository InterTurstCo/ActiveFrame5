package ru.intertrust.cm.remoteclient.report.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.springframework.util.StreamUtils;

import com.healthmarketscience.rmiio.RemoteInputStreamClient;

import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.DeployReportData;
import ru.intertrust.cm.core.business.api.dto.DeployReportItem;
import ru.intertrust.cm.core.business.api.dto.ReportResult;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestReportService extends ClientBase {
    private ReportService reportService;
    private ReportServiceAdmin reportServiceAdmin;

    public static void main(String[] args) {
        try {
            TestReportService test = new TestReportService();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void initServices() throws NamingException {

        reportService = (ReportService) getService(
                "ReportService", ReportService.Remote.class);

        reportServiceAdmin = (ReportServiceAdmin) getService(
                "ReportServiceAdmin", ReportServiceAdmin.Remote.class);
    }

    public void execute(String[] args) throws Exception {
        try {
            long start = System.currentTimeMillis();

            super.execute(args);

            initServices();

            //Установка отчетов
            deployReport("../reports/reports/all-employee");
            ReportResult result = null;
            //Генерация отчета
            Map params = new HashMap();
            params.put("ID_PARAMETER", new RdbmsId(100, 1000));
            params.put("DATE_PARAMETER", new Date());
            params.put("test1", "Любое значение");


            List listParam = new ArrayList();
            listParam.add(new RdbmsId(200, 2000));
            listParam.add(new RdbmsId(300, 3000));
            params.put("LIST_PARAMETER", listParam);
            result = generateReport("all-employee", params, "first");
            //и еще раз генерим тот же отчет
            result = generateReport("all-employee", null, "second");
            //Проверка точки расширения
            params.put("REPLACE_RESULT", Boolean.TRUE);
            result = generateReport("all-employee", params, "theard");

            deployReport("../reports/reports/employee-groups");
            result = generateReport("employee-groups", null);

            deployReport("../reports/reports/all-employee-scriptlet");
            result = generateReport("all-employee-scriptlet", null);

            deployReport("../reports/reports/all-employee-ds");
            result = generateReport("all-employee-ds", null);

            deployReport("../reports/reports/test-resource-service");
            result = generateReport("test-resource-service", null);

            params.clear();
            params.put("FORMAT", "PDF");
            result = generateReport("test-resource-service", params);

            //TODO асинхронная генерация, временно закоментарино, до перехода на eap 6.1 
            /*Future<ReportResult> acyncResult = reportService.generateAsync("all-employee-ds", null);
            while (!acyncResult.isDone())
            {
               log("Wait generation report");
               Thread.sleep(100);
            }
            writeToFile(acyncResult.get().getReport(), new File(acyncResult.get().getFileName()));*/

            deployReport("../reports/reports/test-xml-to-html");
            result = generateReport("test-xml-to-html", null);

            log("Test complete at " + (System.currentTimeMillis() - start));

        } finally {
            writeLog();
        }
    }

    protected ReportResult generateReport(String reportName, Map params, String... namePrefix) throws IOException {
        ReportResult result = reportService.generate(reportName, params);
        InputStream reportStream = RemoteInputStreamClient.wrap(result.getReport());
        File resultFolder = new File("report-result");
        if (!resultFolder.exists()) {
            resultFolder.mkdirs();
        }

        String fileName = result.getFileName();
        if (namePrefix != null && namePrefix.length > 0) {
            fileName = namePrefix[0] + "-" + fileName;
        }

        File reportResultFile = new File("report-result", fileName);

        StreamUtils.copy(reportStream, new FileOutputStream(reportResultFile));
        return result;
    }

    /**
     * Запись массива байт в файл
     * @param content
     * @param file
     * @throws IOException
     */
    protected void writeToFile(byte[] content, File file) throws IOException {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            outStream.write(content);
        } finally {
            if (outStream != null) {
                outStream.close();
            }
        }
    }

    protected void deployReport(String templateFolderPath) throws IOException {
        DeployReportData deployData = new DeployReportData();

        File templateFolder = new File(templateFolderPath);
        File[] filelist = templateFolder.listFiles();
        for (File file : filelist) {
            DeployReportItem item = new DeployReportItem();
            item.setName(file.getName());
            item.setBody(readFile(file));

            deployData.getItems().add(item);
        }

        reportServiceAdmin.deploy(deployData, true);
    }

}
