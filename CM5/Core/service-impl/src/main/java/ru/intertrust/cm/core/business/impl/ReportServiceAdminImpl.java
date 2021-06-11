package ru.intertrust.cm.core.business.impl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import net.sf.jasperreports.engine.JasperCompileManager;
import org.jboss.vfs.VirtualFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.DeployReportData;
import ru.intertrust.cm.core.business.api.dto.DeployReportItem;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.RemoteSuitableException;
import ru.intertrust.cm.core.model.ReportServiceException;
import ru.intertrust.cm.core.model.SystemException;
import ru.intertrust.cm.core.report.ReportServiceBase;
import ru.intertrust.cm.core.report.ScriptletClassLoader;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

/**
 * Реализация сервиса администрирования подсистемы отчетов.
 *
 * @author larin
 */
@Stateless(name = "ReportServiceAdmin")
@Local(ReportServiceAdmin.class)
@Remote(ReportServiceAdmin.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ReportServiceAdminImpl extends ReportServiceBase implements ReportServiceAdmin {

    private final Logger logger = LoggerFactory.getLogger(ReportServiceAdminImpl.class);
    private static Collection<DomainObjectTypeConfig> configurations;
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
    private static final int PATH_PART = isWindows ? 2 : 1;

    private static final String TEMP_STORAGE_PATH = "${attachment.temp.storage}";
    private static final String CONFIG_FILE = "report-import-config.xml";

    @Autowired
    private PropertyResolver propertyResolver;

    @Autowired
    private ImportDataService importDataService;

    private int getReportHash(DeployReportData deployReportData) {
        Map<Integer, Integer> hashMap = new HashMap<>();
        List<Integer> nameHash = new ArrayList<>();
        for (DeployReportItem item : deployReportData.getItems()) {
            hashMap.put(item.getName().hashCode(), Arrays.hashCode(item.getBody()));
            nameHash.add(item.getName().hashCode());
        }

        Collections.sort(nameHash);
        StringBuilder builder = new StringBuilder();
        for (Integer item : nameHash) {
            builder.append(item).append(hashMap.get(item));
        }

        return builder.toString().hashCode();
    }

    /**
     * Установка отчета в систему
     */
    @Override
    public void deploy(DeployReportData deployReportData, boolean lockUpdate) {

        try {
            int reportHash = getReportHash(deployReportData);
            ReportMetadataConfig reportMetadata = deployReportData.getItems().stream()
                    .filter(item -> METADATA_FILE_MAME.equalsIgnoreCase(item.getName())) // Ищем вложение с метаданными по имени
                    .map(DeployReportItem::getBody)
                    .map(this::loadReportMetadata)
                    .findAny()
                    .orElseThrow(() -> new ReportServiceException("Can not find " + METADATA_FILE_MAME + " in report template deploy data"));
            logger.info("Deploy report {}", reportMetadata.getName());

            DomainObject reportTemplateObject = getReportTemplateObject(reportMetadata.getName());
            if (reportTemplateObject != null && Long.valueOf(reportHash).equals(reportTemplateObject.getLong("reportHash"))) {
                return;
            }
            //Получаем все новые вложения и ищем файл с метаинформацией
            File tmpFolder = new File(getTempFolder(), "deploy_report_" + System.currentTimeMillis());
            tmpFolder.mkdirs();
            for (DeployReportItem item : deployReportData.getItems()) {
                //Сохраняем во временную директорию для компиляции
                saveFile(item, tmpFolder);
            }

            compileReport(tmpFolder);

            //Получаем все вложения из временной директории и сохраняем их как вложения
            File[] filelist = tmpFolder.listFiles();
            assert filelist != null;

            //Поиск шаблона по имени
            reportTemplateObject = getReportTemplateObject(reportMetadata.getName());

            //Если не существует то создаем новый
            if (reportTemplateObject == null) {
                reportTemplateObject = createDomainObject("report_template");
                reportTemplateObject.setString("name", reportMetadata.getName());
                reportTemplateObject.setLong("reportHash", (long) reportHash);
                reportTemplateObject.setString("constructor", reportMetadata.getConstructor());
                updateReportTemplate(reportTemplateObject, reportMetadata, filelist, lockUpdate);
            } else {
                Boolean dopLockUpdate = reportTemplateObject.getBoolean("lockUpdate");
                reportTemplateObject.setLong("reportHash", (long) reportHash);
                //Для существующих отчётов
                if (dopLockUpdate == null) {
                    dopLockUpdate = false;
                }
                if ((!dopLockUpdate) || (lockUpdate)) {
                    //Если существует то удаляем все вложения по нему
                    List<DomainObject> attachments = getAttachments("report_template_attach", reportTemplateObject.getId());
                    for (DomainObject attachment : attachments) {
                        attachmentService.deleteAttachment(attachment.getId());
                    }
                    List<DomainObject> attachmentsDX = getAttachments("report_template_attach_dx", reportTemplateObject.getId());
                    for (DomainObject attachmentDX : attachmentsDX) {
                        attachmentService.deleteAttachment(attachmentDX.getId());
                    }
                    updateReportTemplate(reportTemplateObject, reportMetadata, filelist, lockUpdate);
                }
            }
            tmpFolder.delete();
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in deploy", ex);
            throw new ReportServiceException("Error deploy process", ex);
        }
    }

    private void updateReportTemplate(DomainObject reportTemplateObject, ReportMetadataConfig reportMetadata, File[] filelist, boolean lockUpdate) throws IOException {
        //TODO переделать на админ токен
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        reportTemplateObject.setString("description", reportMetadata.getDescription());
        reportTemplateObject.setBoolean("lockUpdate", lockUpdate);
        reportTemplateObject = domainObjectDao.save(reportTemplateObject, accessToken);


        for (File file : filelist) {
            DomainObject attachment =
                    attachmentService.createAttachmentDomainObjectFor(reportTemplateObject.getId(),
                            "report_template_attach");
            attachment.setString("Name", file.getName());
            ByteArrayInputStream bis = new ByteArrayInputStream(readFile(file));
            DirectRemoteInputStream directRemoteInputStream = new DirectRemoteInputStream(bis, false);

            attachmentService.saveAttachment(directRemoteInputStream, attachment);

            //Удаляем, больше нам не нужен
            file.delete();
        }
    }

    private void saveFile(DeployReportItem item, File tmpFolder) throws IOException {
        try (FileOutputStream out = new FileOutputStream(new File(tmpFolder, item.getName()))) {
            out.write(item.getBody());
        }
    }

    /**
     * Удаление шаблона отчета по имени
     */
    @Override
    public void undeploy(String name) {
        configurations = configurationService.getConfigs(DomainObjectTypeConfig.class);
        try {
            deleteCascade("report_template", getReportTemplateObject(name).getId());
            //TODO переделать на админ токен
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            //Поиск шаблона по имени
            DomainObject reportTemplateObject = getReportTemplateObject(name);
            //Удаляем сначала все связанные вложения
            List<DomainObject> attachments = domainObjectDao.findLinkedDomainObjects(reportTemplateObject.getId(), "report_template_attach", "report_template", accessToken);
            deleteDomainObjects(attachments, accessToken);
            List<DomainObject> attachmentsDX = domainObjectDao.findLinkedDomainObjects(reportTemplateObject.getId(), "report_template_attach_dx", "report_template", accessToken);
            deleteDomainObjects(attachmentsDX, accessToken);
            //Удаляем сначала все связанные результаты генерации и их вложения
            List<DomainObject> results = (domainObjectDao.findLinkedDomainObjects(reportTemplateObject.getId(), "report_result", "template_id", accessToken));
            for (DomainObject childObject : results) {
                List<DomainObject> resultAttachs = (domainObjectDao.findLinkedDomainObjects(childObject.getId(), "report_result_attachment", "report_result", accessToken));
                deleteDomainObjects(resultAttachs, accessToken);
            }
            deleteDomainObjects(results, accessToken);

            domainObjectDao.delete(reportTemplateObject.getId(), accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    private void deleteCascade(String doName, Id objectId) {
        if (configurations != null) {
            for (DomainObjectTypeConfig dObject : configurations) {
                for (FieldConfig fieldConfig : dObject.getFieldConfigs()) {
                    if (fieldConfig instanceof ReferenceFieldConfig && Case.toLower(((ReferenceFieldConfig) fieldConfig).getType()).equals(Case.toLower(doName))) {
                        Boolean hasRef = hasReferences(dObject.getName());

                        List<DomainObject> results = (domainObjectDao.findLinkedDomainObjects(objectId, dObject.getName(),
                                fieldConfig.getName(),
                                accessControlService.createSystemAccessToken(this.getClass().getName())));
                        if (results.isEmpty()) {
                            continue;
                        }
                        // Если есть типы ссылающиеся на этот ДО, идем глубже
                        if (hasRef) {
                            for (DomainObject r : results) {
                                deleteCascade(dObject.getName(), r.getId());
                            }
                        }
                        //удаляем строки со ссылкой на исходный ДО
                        if (!results.isEmpty()) {
                            deleteDomainObjects(results,
                                    accessControlService.createSystemAccessToken(this.getClass().getName()));
                        }
                    }
                }
            }
        }
    }

    private Boolean hasReferences(String doName) {
        for (DomainObjectTypeConfig dObject : configurations) {
            for (FieldConfig fieldConfig : dObject.getFieldConfigs()) {
                if (fieldConfig instanceof ReferenceFieldConfig && Case.toLower(((ReferenceFieldConfig) fieldConfig).getType()).equals(Case.toLower(doName))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void deleteDomainObjects(List<DomainObject> childObjects, AccessToken accessToken) {
        for (DomainObject childObject : childObjects) {
            domainObjectDao.delete(childObject.getId(), accessToken);
        }
    }

    private void compileReport(File tempFolder) throws IOException, SecurityException, IllegalArgumentException {

        File[] filelist = tempFolder.listFiles();
        assert filelist != null;

        List<File> javaFiles = new ArrayList<>();
        List<File> jrxmlFiles = new ArrayList<>();

        // Разбираем файлы по коллекциям
        for (File file : filelist) {
            String fileName = file.getName();
            if (fileName.indexOf(".java") > 0) {
                javaFiles.add(file);
            } else if (fileName.indexOf(".jrxml") > 0) {
                jrxmlFiles.add(file);
            }
        }
        // Компиляция джава файлов
        if (!javaFiles.isEmpty()) {
            scriptletCompilation(javaFiles, tempFolder);
        }
        // Компиляция шаблонов
        for (File jrxmlFile : jrxmlFiles) {
            compileJasper(jrxmlFile);
        }
    }

    private void compileJasper(File fileName) {
        ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ScriptletClassLoader scriptletClassLoader =
                    new ScriptletClassLoader(fileName.getParent(), defaultClassLoader);
            Thread.currentThread().setContextClassLoader(scriptletClassLoader);
            logger.debug("Compile template {}", fileName);
            String jasperLocation = fileName.getPath().replace(".jrxml", ".jasper");
            JasperCompileManager.compileReportToFile(fileName.getPath(), jasperLocation);
        } catch (Exception ex) {
            throw new ReportServiceException("Error compile " + fileName, ex);
        } finally {
            Thread.currentThread().setContextClassLoader(defaultClassLoader);
        }
    }

    private void scriptletCompilation(List<File> javaFiles, File tempFolder)
            throws IOException, SecurityException, IllegalArgumentException {

        for (File file : javaFiles) {
            logger.debug("Compile class {}", file.getName());
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new FatalException("Application start with JRE, but need JDK");
        }
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, Locale.getDefault(), null);

        Iterable<? extends JavaFileObject> compilationUnits = stdFileManager.getJavaFileObjectsFromFiles(javaFiles);
        ArrayList<String> compileOptions = new ArrayList<>();

        compileOptions.add("-cp");
        compileOptions.add(getCompileClassPath());
        compileOptions.add("-d");
        compileOptions.add(tempFolder.getPath());
        compileOptions.add("-encoding");
        compileOptions.add("utf-8");
        compileOptions.add("-g");

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        CompilationTask compilerTask = compiler.getTask(null, stdFileManager,
                diagnostics, compileOptions, null, compilationUnits);

        boolean status = compilerTask.call();
        stdFileManager.close();

        if (!status) {
            StringBuilder message = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                message.append(diagnostic.toString()).append('\n');
            }
            throw new ReportServiceException(message.toString());
        }
    }

    private String getCompileClassPath() throws IOException {
        String[] rootPackages = new String[]{"net", "ru", "com", "org", "lotus"};
        //Получаем библиотеки для runtime компилятора
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //Список найденных jar библиотек. Необходим чтоб не дублировать jar-ы
        Set<String> paths = new HashSet<>();
        StringBuilder cp = new StringBuilder();

        String path;
        for (String rootPackage : rootPackages) {
            Enumeration<URL> urls = classLoader.getResources(rootPackage);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String urlPath = url.getPath();
                if (!urlPath.contains(".jar")) {
                    continue;
                }
                if ("vfs".equals(url.getProtocol())) {
                    int end = url.toString().indexOf(".jar") + 4;
                    URL urlToJar = new URL(url.toString().substring(0, end));
                    VirtualFile vf = (VirtualFile) urlToJar.openConnection().getContent();
                    String dirName = vf.getPhysicalFile().getParent();
                    path = new File(dirName, vf.getName()).getPath();
                } else { // process "file:" and other urls as usual
                    int end = urlPath.indexOf(".jar") + 4;
                    // In Windows: "file:/D:/programs..."
                    // In *nix: "file:/opt/wildfly..."
                    int start = urlPath.indexOf(":/") + PATH_PART;
                    path = urlPath.substring(start, end);
                }
                if (paths.add(path)) {
                    cp.append(path).append(File.pathSeparatorChar);
                }
            }
        }
        return cp.toString();
    }

    @Override
    public void recompileAll() {
        logger.info("Start recompile all reports");
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery("select id, name, lockupdate from report_template", 0, 0, accessToken);
        for (IdentifiableObject identifiableObject : collection) {
            DeployReportData deployReportData = getReportData(identifiableObject.getId());
            if (!deployReportData.getItems().isEmpty()) {
                logger.info("Recompile report {}", identifiableObject.getString("name"));
                deploy(deployReportData, identifiableObject.getBoolean("lockupdate") != null && identifiableObject.getBoolean("lockupdate"));
            }
        }
        logger.info("End recompile all reports");
    }

    @Override
    public void importReportPackage(@Nonnull File reportPackageFile) throws Exception {
        logMessage("importReportPackage >>>", "reportPackageFile=", reportPackageFile.getAbsolutePath());
        String pathForTempFilesStore = propertyResolver.resolvePlaceholders(TEMP_STORAGE_PATH);
        logMessage("importReportPackage === 1", "pathForTempFilesStore=", pathForTempFilesStore);
        File packageFilesPath = this.unzipFile(pathForTempFilesStore, reportPackageFile);
        logMessage("importReportPackage === 2", "packageFilesPath=", packageFilesPath.getAbsolutePath());
        ReportImportConfig reportImportConfig = loadImportConfig(packageFilesPath);
        if (reportImportConfig == null) {
            throw new Exception("Ошибка при загрузки конфигурации импорта отчетов.");
        }
        List<CsvFile> csvFiles = reportImportConfig.getCsvFiles();
        logMessage("importReportPackage === 3", "csvFiles.count=", csvFiles != null ? csvFiles.size() : null);
        if (csvFiles != null) {
            for (CsvFile csvFile : csvFiles) {
                logMessage("importReportPackage === 3.1", "csvFile=", csvFile);
                File file = new File(packageFilesPath, csvFile.getFilePath());
                logMessage("importReportPackage === 3.2", "file=", file.getAbsolutePath());
                importDataService.importData(readFile(file), csvFile.getCodePage(), csvFile.getOverwrite());
                logMessage("importReportPackage === 3.3", "import of csvFile=", csvFile, "ok");
            }
        }
        List<ReportTemplate> reportTemplates = reportImportConfig.getReportTemplates();
        logMessage("importReportPackage === 4", "reportTemplates.count=", reportTemplates != null ? reportTemplates.size() : null);
        if (reportTemplates != null) {
            for (ReportTemplate reportTemplate : reportTemplates) {
                List<TemplateFilePath> templateFiles = reportTemplate != null ? reportTemplate.getTemplateFiles() : null;
                logMessage("importReportPackage === 4.1", "reportTemplate.files.count=", templateFiles != null ? templateFiles.size() : null);
                if (templateFiles != null && !templateFiles.isEmpty()) {
                    DeployReportData deployData = new DeployReportData();
                    for (TemplateFilePath templateFile : templateFiles) {
                        if (templateFile != null) {
                            logMessage("importReportPackage === 4.2", "templateFile=", templateFile);
                            File file = new File(packageFilesPath, templateFile.getFilePath());
                            DeployReportItem deployItem = new DeployReportItem();
                            deployItem.setName(file.getName());
                            deployItem.setBody(readFile(file));
                            deployData.getItems().add(deployItem);
                        }
                    }
                    logMessage("importReportPackage === 4.3", "deploying report template ...");
                    this.deploy(deployData, true);
                    logMessage("importReportPackage === 4.4", "deploy of report template ok");
                }
            }
        }
        try {
            logMessage("importReportPackage === 5", "deleting catalog", packageFilesPath.getAbsolutePath());
            boolean b = deleteFile(packageFilesPath);
            logMessage("importReportPackage === 5.1", "deleted", b);
        } catch (Throwable e) {
            logMessage("importReportPackage === 5.2", "Error deleting tmp files", e.getMessage());
        }
        logMessage("importReportPackage <<<");
    }


    private DeployReportData getReportData(Id templateId) {
        DeployReportData result = new DeployReportData();
        List<DomainObject> attachments = getAttachments("report_template_attach", templateId);
        for (DomainObject attachment : attachments) {
            String name = attachment.getString("name");
            if (!name.endsWith(".jasper")) {
                DeployReportItem item = new DeployReportItem();
                item.setBody(getAttachmentContent(attachment));
                item.setName(name);
                result.getItems().add(item);
            }
        }
        return result;
    }

    private byte[] getAttachmentContent(DomainObject attachment) {
        InputStream contentStream = null;
        RemoteInputStream inputStream = null;
        try {
            inputStream = attachmentService.loadAttachment(attachment.getId());
            contentStream = RemoteInputStreamClient.wrap(inputStream);
            ByteArrayOutputStream attachmentBytes = new ByteArrayOutputStream();

            int read;
            byte[] buffer = new byte[1024];
            while ((read = contentStream.read(buffer)) > 0) {
                attachmentBytes.write(buffer, 0, read);
            }
            return attachmentBytes.toByteArray();
        } catch (Exception ex) {
            throw new ReportServiceException("Error on get attachment body", ex);
        } finally {
            try {
                if (contentStream != null) {
                    contentStream.close();
                }
                if (inputStream != null) {
                    inputStream.close(true);
                }
            } catch (IOException ignored) {
            }
        }
    }

    private ReportImportConfig loadImportConfig(File dirPath) throws Exception {
        logMessage("loadImportConfig >>>", "dirPath=", dirPath != null ? dirPath.getAbsolutePath() : null, "configFile=", ReportServiceAdminImpl.CONFIG_FILE);
        JAXBContext jc = JAXBContext.newInstance(ReportImportConfig.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        File configFilePath = new File(dirPath, ReportServiceAdminImpl.CONFIG_FILE);
        logMessage("loadImportConfig === 1", "configFilePath=", configFilePath.getAbsolutePath(), "configFile=", ReportServiceAdminImpl.CONFIG_FILE);
        ReportImportConfig reportImportConfig = (ReportImportConfig) unmarshaller.unmarshal(configFilePath);
        logMessage("loadImportConfig <<<", "configFilePath=", configFilePath.getAbsolutePath(), "configFile=", ReportServiceAdminImpl.CONFIG_FILE);
        return reportImportConfig;
    }

    private File unzipFile(String path, @Nonnull File jarFile) throws Exception {
        logMessage("unzipFile >>>", "path=", path, "jarFile=", jarFile.getAbsolutePath());
        File destDir = new File(path, this.getUniqueName());
        logMessage("unzipFile === 1", "destDir=", destDir.getAbsolutePath());
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
        logMessage("unzipFile <<<", "destDir=", destDir.getAbsolutePath());
        return destDir;
    }

    private String getUniqueName() {
        return UUID.randomUUID().toString().replace("-", "") + "_report_package";
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

    private void logMessage(Object... data) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        String logMsg;
        if (data != null) {
            StringBuilder sb = new StringBuilder();
            for (Object msg : data) {
                sb.append(msg).append(" ");
            }
            logMsg = sb.toString();
        } else {
            logMsg = "null";
        }
        logger.debug(logMsg);
    }

    @XmlRootElement(name = "report-import-config")
    @XmlAccessorType(XmlAccessType.PROPERTY)
    private static class ReportImportConfig implements Serializable {

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
        public void setCodePage(String codePage) {
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
            return overwrite == null || overwrite;
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

        @Override
        public String toString() {
            return filePath != null ? filePath : "null";
        }
    }
}
