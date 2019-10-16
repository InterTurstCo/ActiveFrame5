package ru.intertrust.cm.core.business.load;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.DeployReportData;
import ru.intertrust.cm.core.business.api.dto.DeployReportItem;
import ru.intertrust.cm.core.config.module.ImportReportsConfiguration;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.config.module.ReportTemplateConfiguration;
import ru.intertrust.cm.core.config.module.ReportTemplateDirConfiguration;
import ru.intertrust.cm.core.config.module.ReportTemplateFileConfiguration;
import ru.intertrust.cm.core.model.FatalException;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Lesia Puhova
 *         Date: 28.03.14
 *         Time: 12:55
 */
@Stateless
@Local(ImportReportsData.class)
@Remote(ImportReportsData.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ImportReportsDataImpl implements ImportReportsData, ImportReportsData.Remote {

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private ReportServiceAdmin reportServiceAdmin;

    public void load() {
        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            deployReports(moduleConfiguration);
        }
    }

    private void deployReports(ModuleConfiguration moduleConfiguration) {
        String moduleName = moduleConfiguration.getName();
        ImportReportsConfiguration importReports = moduleConfiguration.getImportReports();
        URL moduleUrl = moduleConfiguration.getModuleUrl();
        if (importReports != null) {
            processDirs(importReports, moduleUrl, moduleName);
            processFiles(importReports, moduleUrl, moduleName);
        }
    }

    private void processDirs(ImportReportsConfiguration importReports, URL moduleUrl, String moduleName) {
        List<ReportTemplateDirConfiguration> templatePaths = importReports.getReportTemplateDirs();
        if (templatePaths == null) {
            return;
        }
        for (ReportTemplateDirConfiguration templatePath : templatePaths) {
            String templateFolderPath = templatePath.getTemplateDirPath();
            if (!templateFolderPath.endsWith("/")) {
                templateFolderPath = templateFolderPath + "/";
            }
            try {
                try(ZipInputStream zip = new ZipInputStream(new FileInputStream(moduleUrl.getPath()))) {
                    List<String> filePaths = new ArrayList<String>();
                    for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                        if (!entry.isDirectory() && entry.getName().startsWith(templateFolderPath)) {
                            filePaths.add(entry.getName());
                        }
                    }
                    deployReportFiles(filePaths, moduleUrl);
                }
            } catch (Exception e) {
                throw new FatalException("Cannot deploy report: module=" + moduleName + "; template path=" + templateFolderPath, e);
            }
        }
    }

    private void processFiles(ImportReportsConfiguration importReports, URL moduleUrl, String moduleName) {
        List<ReportTemplateConfiguration> reportTemplates = importReports.getReportTemplates();
        if (reportTemplates == null) {
            return;
        }
        for (ReportTemplateConfiguration reportTemplate : reportTemplates) {
            List<String> filePaths = new ArrayList<String>();
            for (ReportTemplateFileConfiguration templatePath : reportTemplate.getTemplatePaths()) {
                filePaths.add(templatePath.getTemplateFilePath());
            }
            try {
                deployReportFiles(filePaths, moduleUrl);
            } catch (Exception e) {
                throw new FatalException("Cannot deploy report: module=" + moduleName + "; template paths=" + filePaths, e);
            }
        }
    }

    private void deployReportFiles(List<String> filePaths, URL moduleUrl) throws IOException {
        if (filePaths != null && !filePaths.isEmpty()) {
            DeployReportData deployData = new DeployReportData();
            for (String filePath : filePaths) {
                DeployReportItem item = new DeployReportItem();
                Path p = Paths.get(filePath); // item.name is only file name, not the whole path
                String fileName = p.getFileName().toString();
                item.setName(fileName);

                URL fileURL = new URL(moduleUrl.toString() + filePath);
                item.setBody(readFile(fileURL));
                deployData.getItems().add(item);
            }
            reportServiceAdmin.deploy(deployData, false);
        }
    }

    /**
     * Получение файла в виде массива байт
     * @param fileUrl
     * @return
     * @throws IOException
     */
    private byte[] readFile(URL fileUrl) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream input = null;
        try {
            input = fileUrl.openStream();
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

    public void setReportServiceAdmin(ReportServiceAdmin reportServiceAdmin) {
        this.reportServiceAdmin = reportServiceAdmin;
    }

}
