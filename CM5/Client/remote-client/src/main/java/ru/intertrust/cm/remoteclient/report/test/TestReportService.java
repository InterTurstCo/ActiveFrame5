package ru.intertrust.cm.remoteclient.report.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.DeployReportData;
import ru.intertrust.cm.core.business.api.dto.DeployReportItem;
import ru.intertrust.cm.core.business.api.dto.ReportResult;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestReportService extends ClientBase {
    private CrudService crudService;
    private CollectionsService collectionService;
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

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            crudService = (CrudService) getService(
                    "CrudServiceImpl", CrudService.Remote.class);

            collectionService = (CollectionsService) getService(
                    "CollectionsServiceImpl", CollectionsService.Remote.class);
            
            reportService = (ReportService) getService(
                    "ReportService", ReportService.Remote.class);

            reportServiceAdmin = (ReportServiceAdmin) getService(
                    "ReportServiceAdmin", ReportServiceAdmin.Remote.class);
            
            
            //Установка отчетов
            deployReport("../reports/reports/all-employee");
            ReportResult result = null;
            //Генерация отчета
            result = generateReport("all-employee", null);
            //и еще раз генерим тот же отчет
            result = generateReport("all-employee", null);
            //Проверка точки расширения
            Map params = new HashMap();
            params.put("REPLACE_RESULT", Boolean.TRUE);
            result = generateReport("all-employee", params);

            deployReport("../reports/reports/employee-groups");
            result = generateReport("employee-groups", null);
            
            deployReport("../reports/reports/all-employee-scriptlet");
            result = generateReport("all-employee-scriptlet", null);

            deployReport("../reports/reports/all-employee-ds");
            result = generateReport("all-employee-ds", null);

            deployReport("../reports/reports/test-resource-service");
            result = generateReport("test-resource-service", null);

            //TODO асинхронная генерация, временно закоментарино, до перехода на eap 6.1 
            /*Future<ReportResult> acyncResult = reportService.generateAsync("all-employee-ds", null);
            while (!acyncResult.isDone())
            {
               log("Wait generation report");
               Thread.sleep(100);
            }
            writeToFile(acyncResult.get().getReport(), new File(acyncResult.get().getFileName()));*/
            
            log("Test complete");
            
            
            
        } finally {
            writeLog();
        }
    }

    private ReportResult generateReport(String reportName, Map params) throws IOException {
        ReportResult result = reportService.generate(reportName, params);
        
        writeToFile(result.getReport(), new File(result.getFileName()));
        
        
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
            outStream.close();
        }
    }    

    private void deployReport(String templateFolderPath) throws IOException {
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
