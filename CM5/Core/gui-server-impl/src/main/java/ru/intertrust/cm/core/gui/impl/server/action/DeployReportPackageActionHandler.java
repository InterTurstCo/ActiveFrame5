package ru.intertrust.cm.core.gui.impl.server.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.DeployReportData;
import ru.intertrust.cm.core.business.api.dto.DeployReportItem;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.DeployReportPackageActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@ComponentName("deploy-report-package.action")
public class DeployReportPackageActionHandler extends ActionHandler<DeployReportPackageActionContext, ActionData> {
    private static final Logger logger = LoggerFactory.getLogger(DeployReportPackageActionHandler.class);

    @Autowired
    private ReportServiceAdmin reportServiceAdmin;

    @Autowired
    private PropertyResolver propertyResolver;

    @Autowired
    private ImportDataService importDataService;

    private static final String TEMP_STORAGE_PATH = "${attachment.temp.storage}";
    private static final String CONFIG_FILE = "report-import-config.xml";

    @Override
    public ActionData executeAction(DeployReportPackageActionContext deployContext) {
        logMessage("executeAction >>>");
        AttachmentItem attachmentItem = deployContext.getAttachmentItem();
        if (attachmentItem != null) {
            try {
                importReportPackage(attachmentItem);
            } catch (UnmarshalException e) {
                logger.error("Ошибка при загрузке и импорте пакета отчетов (Unmarshal).", e);
                String errorMessage = e.getLinkedException() != null && e.getLinkedException().getMessage() != null ?
                        e.getLinkedException().getMessage() : e.getCause() != null && e.getCause().getMessage() != null ?
                        e.getCause().getMessage() : e.getMessage();
                throw new RuntimeException("Ошибка при загрузке и импорте пакета отчетов (" + errorMessage + ").",
                        e.getLinkedException() != null ? e.getLinkedException() : e.getCause() != null ? e.getCause() : e);
            } catch (Exception e) {
                logger.error("Ошибка при загрузке и импорте пакета отчетов (Exception).", e);
                String errorMessage = e.getCause() != null && e.getCause().getMessage() != null ?
                        e.getCause().getMessage() : e.getMessage();
                throw new RuntimeException("Ошибка при загрузке и импорте пакета отчетов (" + errorMessage + ").",
                        e.getCause() != null ? e.getCause() : e);
            } catch (Throwable e) {
                logger.error("Ошибка при загрузке и импорте пакета отчетов (Throwable).", e);
                throw new RuntimeException("Ошибка при загрузке и импорте пакета отчетов (" + e.getMessage() + ").", e);
            }
        } else {
            throw new RuntimeException("Не указан пакет отчетов.");
        }
        logMessage("executeAction <<<");
        return new SimpleActionData();
    }

    @Override
    public DeployReportPackageActionContext getActionContext(final ActionConfig actionConfig) {
        return new DeployReportPackageActionContext(actionConfig);
    }

    private void importReportPackage(AttachmentItem reportPackage) throws Exception {
        logMessage("importReportPackage >>>");
        String pathForTempFilesStore = propertyResolver.resolvePlaceholders(TEMP_STORAGE_PATH);
        logMessage("importReportPackage === 1", "pathForTempFilesStore=", pathForTempFilesStore);
        File jarFile = new File(pathForTempFilesStore, reportPackage.getTemporaryName());
        logMessage("importReportPackage === 2", "jarFile=", jarFile != null ? jarFile.getAbsolutePath() : null);
        File packageFilesPath = this.unzipFile(pathForTempFilesStore, jarFile);
        logMessage("importReportPackage === 3", "packageFilesPath=", packageFilesPath != null ? packageFilesPath.getAbsolutePath() : null);
        ReportImportConfig reportImportConfig = loadImportConfig(packageFilesPath, CONFIG_FILE);
        if (reportImportConfig == null) {
            throw new Exception("Ошибка при загрузки конфигурации импорта отчетов.");
        }
        List<CsvFile> csvFiles = reportImportConfig.getCsvFiles();
        logMessage("importReportPackage === 4", "csvFiles.count=", csvFiles != null ? csvFiles.size() : null);
        if (csvFiles != null) {
            for (CsvFile csvFile : csvFiles) {
                logMessage("importReportPackage === 4.1", "csvFile=", csvFile);
                File file = new File(packageFilesPath, csvFile != null ? csvFile.getFilePath() : null);
                logMessage("importReportPackage === 4.2", "file=", file != null ? file.getAbsolutePath() : null);
                importDataService.importData(readFile(file), csvFile.getCodePage(), csvFile.getOverwrite());
                logMessage("importReportPackage === 4.3", "import of csvFile=", csvFile, "ok");
            }
        }
        List<ReportTemplate> reportTemplates = reportImportConfig.getReportTemplates();
        logMessage("importReportPackage === 5", "reportTemplates.count=", reportTemplates != null ? reportTemplates.size() : null);
        if (reportTemplates != null) {
            for (ReportTemplate reportTemplate : reportTemplates) {
                List<TemplateFilePath> templateFiles = reportTemplate != null ? reportTemplate.getTemplateFiles() : null;
                logMessage("importReportPackage === 5.1", "reportTemplate.files.count=", templateFiles != null ? templateFiles.size() : null);
                if (templateFiles != null && !templateFiles.isEmpty()) {
                    DeployReportData deployData = new DeployReportData();
                    for (TemplateFilePath templateFile : templateFiles) {
                        if (templateFile != null) {
                            logMessage("importReportPackage === 5.2", "templateFile=", templateFile);
                            File file = new File(packageFilesPath, templateFile.getFilePath());
                            DeployReportItem deployItem = new DeployReportItem();
                            deployItem.setName(file.getName());
                            deployItem.setBody(readFile(file));
                            deployData.getItems().add(deployItem);
                        }
                    }
                    logMessage("importReportPackage === 5.3", "deploying report template ...");
                    reportServiceAdmin.deploy(deployData, true);
                    logMessage("importReportPackage === 5.4", "deploy of report template ok");
                }
            }
        }
        try {
            logMessage("importReportPackage === 6", "deleting catalog", packageFilesPath.getAbsolutePath());
            boolean b = deleteFile(packageFilesPath);
            logMessage("importReportPackage === 6.1", "deleted", b);
        } catch (Throwable e) {
            logMessage("importReportPackage === 6.2", "Error deleting tmp files", e.getMessage());
        }
        logMessage("importReportPackage <<<");
    }

    private byte[] readFile(File file) throws IOException {
        logMessage("readFile >>>", "file=", file != null ? file.getAbsolutePath() : null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            int read = 0;
            byte[] buffer = new byte[4096];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            logMessage("readFile <<<", "file=", file != null ? file.getAbsolutePath() : null);
            return out.toByteArray();
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    private File unzipFile(String path, File jarFile) throws Exception {
        logMessage("unzipFile >>>", "path=", path, "jarFile=", jarFile != null ? jarFile.getAbsolutePath() : null);
        File destDir = new File(path, this.getUniqueName());
        logMessage("unzipFile === 1", "destDir=", destDir != null ? destDir.getAbsolutePath() : null);
        destDir.mkdirs();
        InputStream zipFileStream = new FileInputStream(jarFile);
        // TODO кодировка
        try (ZipInputStream zis = new ZipInputStream(zipFileStream, Charset.defaultCharset())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(destDir, entry.getName());

                if (!file.toPath().normalize().startsWith(destDir.toPath())) {
                    throw new IOException("Файл (" + file.toPath() + ") находится вне каталога: " + destDir.toPath());
                }

                if (entry.isDirectory()) {
                    file.mkdirs();
                    continue;
                }

                byte[] buffer = new byte[4096];
                file.getParentFile().mkdirs();
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                int count;

                while ((count = zis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.close();
            }
        } catch (IOException e) {
            throw new Exception(e);
        }
        logMessage("unzipFile <<<", "destDir=", destDir != null ? destDir.getAbsolutePath() : null);
        return destDir;
    }

    private String getUniqueName() {
        return UUID.randomUUID().toString().replace("-", "") + "_report_package";
    }

    private ReportImportConfig loadImportConfig(File dirPath, String configFile) throws Exception {
        logMessage("loadImportConfig >>>", "dirPath=", dirPath != null ? dirPath.getAbsolutePath() : null, "configFile=", configFile);
        JAXBContext jc = JAXBContext.newInstance(ReportImportConfig.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        File configFilePath = new File(dirPath, configFile);
        logMessage("loadImportConfig === 1", "configFilePath=", configFilePath != null ? configFilePath.getAbsolutePath() : null, "configFile=", configFile);
        ReportImportConfig reportImportConfig = (ReportImportConfig) unmarshaller.unmarshal(configFilePath);
        logMessage("loadImportConfig <<<", "configFilePath=", configFilePath != null ? configFilePath.getAbsolutePath() : null, "configFile=", configFile);
        return reportImportConfig;
    }

    private boolean deleteFile(File path) {
        boolean retVal = false;
        if (path != null && path.exists()) {
            retVal = true;
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        retVal &= deleteFile(file);
                    } else {
                        retVal &= file.delete();
                    }
                }
            }
            retVal &= path.delete();
        }
        return retVal;
    }

    private static void logMessage(Object... data) {
        if (logger.isDebugEnabled()) {
            String logMsg = "";
            if (data != null) {
                for (Object msg : data) {
                    logMsg += (msg != null ? msg.toString() : "null") + " ";
                }
            } else {
                logMsg = "null";
            }
            logger.debug(logMsg);
        }
    }

    @XmlRootElement(name = "report-import-config")
    @XmlAccessorType(XmlAccessType.PROPERTY)
    private static class ReportImportConfig implements Serializable {
        private static final long serialVersionUID = 1L;

        @XmlElementWrapper(name = "csv")
        @XmlElement(name = "file-path")
        private List<CsvFile> csvFiles;

        @XmlElementWrapper(name = "templates")
        @XmlElement(name = "template")
        private List<ReportTemplate> reportTemplates;

        public ReportImportConfig() {
            super();
        }

        public List<CsvFile> getCsvFiles() {
            return csvFiles;
        }

        public List<ReportTemplate> getReportTemplates() {
            return reportTemplates;
        }
    }

    @XmlAccessorType(XmlAccessType.PROPERTY)
    private static class CsvFile implements Serializable {
        private static final long serialVersionUID = 2L;

        private String filePath;

        @XmlAttribute(name = "overwrite")
        private Boolean overwrite;

        private String codePage;

        public CsvFile() {
            super();
        }

        public String getFilePath() {
            return filePath;
        }

        @XmlValue
        public void setFilePath(String filePath) {
            if (filePath != null) {
                filePath = filePath.replaceAll("\n", "")
                        .replaceAll("\r", "").trim();
            }
            this.filePath = filePath;
        }

        public String getCodePage() {
            return codePage;
        }

        @XmlAttribute(name = "code-page")
        public void setCodePage(String codePage) throws Exception {
            if (codePage == null) {
                this.codePage = Charset.forName("cp1251").name();
            } else {
                try {
                    this.codePage = Charset.forName(codePage).name();
                } catch (UnsupportedCharsetException e) {
                    this.codePage = Charset.forName("cp1251").name();
                    throw new RuntimeException("Неверно указана кодировка csv-файла в конфигурации " + CONFIG_FILE + ": " + codePage);
                }
            }
        }

        public boolean getOverwrite() {
            return overwrite != null ? overwrite.booleanValue() : true;
        }

        @Override
        public String toString() {
            return "CsvFile:{file-path=" + (filePath != null ? filePath : "null") + "; " +
                    "code-page=" + getCodePage() + "; " +
                    "overwrite=" + getOverwrite() + "}";
        }
    }

    @XmlAccessorType(XmlAccessType.PROPERTY)
    private static class ReportTemplate implements Serializable {
        private static final long serialVersionUID = 3L;

        @XmlElement(name = "file-path")
        private List<TemplateFilePath> templateFiles;

        public ReportTemplate() {
            super();
        }

        public List<TemplateFilePath> getTemplateFiles() {
            return templateFiles;
        }
    }

    @XmlAccessorType(XmlAccessType.PROPERTY)
    private static class TemplateFilePath implements Serializable {
        private static final long serialVersionUID = 4L;

        private String filePath;

        public TemplateFilePath() {
            super();
        }

        public String getFilePath() {
            return filePath;
        }

        @XmlValue
        public void setFilePath(String filePath) {
            if (filePath != null) {
                filePath = filePath.replaceAll("\n", "")
                        .replaceAll("\r", "").trim();
            }
            this.filePath = filePath;
        }

        @Override public String toString() {
            return filePath != null ? filePath : "null";
        }
    }

}
