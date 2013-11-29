package ru.intertrust.cm.remoteclient.report.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.DeployReportData;
import ru.intertrust.cm.core.business.api.dto.DeployReportItem;
import ru.intertrust.cm.remoteclient.ClientBase;
import ru.intertrust.cm.remoteclient.process.test.CreateTestData;

public class TestReportService extends ClientBase {
    private CrudService crudService;
    private CollectionsService collectionService;
    private ReportService reportService;
    private ReportServiceAdmin reportServiceAdmin;

    public static void main(String[] args) {
        try {
            CreateTestData test = new CreateTestData();
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
            reportService.generate("all-employee", null);
            
            
        } finally {
            writeLog();
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
