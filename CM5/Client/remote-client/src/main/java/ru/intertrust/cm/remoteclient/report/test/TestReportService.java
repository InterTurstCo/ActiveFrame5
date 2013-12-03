package ru.intertrust.cm.remoteclient.report.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
            
            //Генерация отчета
            ReportResult result = generateReport("all-employee", null);
            //и еще раз генерим тот же отчет
            result = generateReport("all-employee", null);

            deployReport("../reports/reports/employee-groups");
            result = generateReport("employee-groups", null);
            
            deployReport("../reports/reports/all-employee-scriptlet");
            result = generateReport("all-employee-scriptlet", null);

            deployReport("../reports/reports/all-employee-ds");
            result = generateReport("all-employee-ds", null);
            
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
        
        reportServiceAdmin.deploy(deployData);
        
    }
    /**
     * Получение файла в виде массива байт
     * @param file
     * @return
     * @throws IOException
     */
    protected byte[] readFile(File file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            input.close();
        }
    }

}
