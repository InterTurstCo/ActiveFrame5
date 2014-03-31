package ru.intertrust.cm.core.business.load;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DeployReportData;
import ru.intertrust.cm.core.business.api.dto.DeployReportItem;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.impl.AttachmentServiceImpl;
import ru.intertrust.cm.core.config.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.config.module.ImportReportsConfiguration;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.config.module.ReportTemplateConfiguration;
import ru.intertrust.cm.core.config.module.ReportTemplateDirConfiguration;
import ru.intertrust.cm.core.config.module.ReportTemplateFileConfiguration;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.ReportServiceException;
import ru.intertrust.cm.core.model.UnexpectedException;
import ru.intertrust.cm.core.report.ScriptletClassLoader;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

/**
 * @author Lesia Puhova
 *         Date: 28.03.14
 *         Time: 12:55
 */

@ExtensionPoint
public class ImportReportsData {

    @Autowired
    private CollectionsDao collectionsDao;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private DomainObjectDao domainObjectDao;
    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private AttachmentContentDao attachmentContentDao;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    private static Logger logger = Logger.getLogger(ImportReportsData.class);

    private static File tempFolder;
    private static final String METADATA_FILE_MAME = "template.xml";

    public void onLoad() {
        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            deployReports(moduleConfiguration);
        }
    }

    private void deployReports(ModuleConfiguration moduleConfiguration) {
        String moduleName = moduleConfiguration.getName();
        ImportReportsConfiguration importReports = moduleConfiguration.getImportReports();
        URL moduleUrl = moduleConfiguration.getModuleUrl();
        if (importReports != null && importReports.getReportTemplateDirs() != null) {
            processDirs(importReports, moduleUrl, moduleName);
            processFiles(importReports,moduleUrl, moduleName);
        }
    }

    private void processDirs(ImportReportsConfiguration importReports, URL moduleUrl, String moduleName) {
        List<ReportTemplateDirConfiguration> templatePaths = importReports.getReportTemplateDirs();
        for (ReportTemplateDirConfiguration templatePath : templatePaths) {
            String templateFolderPath = templatePath.getTemplateDirPath();
            try {
                deployReportFolder(templateFolderPath, moduleUrl);
            } catch (Exception e) {
                throw new FatalException("Cannot deploy report: module=" + moduleName + "; template path=" + templateFolderPath, e);
            }
        }
    }

    private void processFiles(ImportReportsConfiguration importReports, URL moduleUrl, String moduleName) {
        List<ReportTemplateConfiguration> reportTemplates = importReports.getReportTemplates();
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

    private void deployReportFolder(String templateFolderPath, URL moduleUrl) throws IOException, URISyntaxException {
        DeployReportData deployData = new DeployReportData();
        String path = moduleUrl.toString() + templateFolderPath;
        File templateFolder = new File(path);//new File(new URL(path).toURI()); //FIXME: does not create folder correctly
        File[] fileList = templateFolder.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                DeployReportItem item = new DeployReportItem();
                item.setName(file.getName());
                URL fileURL = new URL(moduleUrl.toString() + file.getName());
                item.setBody(readFile(fileURL));

                deployData.getItems().add(item);
            }
            deploy(deployData);
        }
    }

    private void deployReportFiles(List<String> filePaths, URL moduleUrl) throws IOException {
        if (filePaths != null) {
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
            deploy(deployData);
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


    private void deploy(DeployReportData deployReportData) {
        try {
            //Получаем все новые вложения и ищем файл с метаинформацией
            ReportMetadataConfig reportMetadata = null;
            File tmpFolder = new File(getTempFolder(), "deploy_report_" + System.currentTimeMillis());
            tmpFolder.mkdirs();
            for (DeployReportItem item : deployReportData.getItems()) {
                //Ищем вложение с метаданными по имени
                if (item.getName().equalsIgnoreCase(METADATA_FILE_MAME)) {
                    reportMetadata = loadReportMetadata(item.getBody());
                }
                //Сохраняем во временную директорию для компиляции
                saveFile(item, tmpFolder);
            }

            compileReport(tmpFolder);

            if (reportMetadata == null) {
                throw new ReportServiceException("Can not find " + METADATA_FILE_MAME
                        + " in report template deploy data");
            }
            AccessToken accessToken = createSystemAccessToken();
            //Поиск шаблона по имени
            DomainObject reportTemplateObject = getReportTemplateObject(reportMetadata.getName());

            //Если не существует то создаем новый
            if (reportTemplateObject == null) {
                reportTemplateObject = createDomainObject("report_template");
                reportTemplateObject.setString("name", reportMetadata.getName());
            } else {
                //Если существует то удаляем все вложения по нему
                List<DomainObject> attachments = getAttachments("report_template_attach", reportTemplateObject);
                for (DomainObject attachment : attachments) {
                    deleteAttachment(attachment.getId());
                }
            }
            reportTemplateObject.setString("description", reportMetadata.getDescription());
            reportTemplateObject = domainObjectDao.save(reportTemplateObject, accessToken);

            //Получаем все вложения из временной директории и сохраняем их как вложения
            File[] fileList = tmpFolder.listFiles();
            for (File file : fileList) {
                DomainObject attachment =
                        createAttachmentDomainObjectFor(reportTemplateObject.getId(),
                                "report_template_attach");
                attachment.setString("Name", file.getName());
                InputStream stream = new FileInputStream(file);
                long contentLength = getContentLength(file);

                saveAttachment(stream, attachment, contentLength);

                //Удаляем, больше нам не нужен
                file.delete();
            }

            tmpFolder.delete();
        } catch (Exception ex) {
            throw new ReportServiceException("Error deploy process", ex);
        }

    }

    private DomainObject saveAttachment(InputStream inputStream, DomainObject attachmentDomainObject, long contentLength) {
        AccessToken accessToken = createSystemAccessToken();

        StringValue newFilePathValue = null;
        DomainObject savedDomainObject = null;
        try {
            String newFilePath = attachmentContentDao.saveContent(inputStream);
            //если newFilePath is null или empty не обрабатываем
            if (newFilePath == null || newFilePath.isEmpty()) {
                throw new DaoException("File isn't created");
            }
            newFilePathValue = new StringValue(newFilePath);
            StringValue oldFilePathValue = (StringValue) attachmentDomainObject.getValue("path");
            attachmentDomainObject.setValue(AttachmentServiceImpl.PATH_NAME, new StringValue(newFilePath));

            attachmentDomainObject.setLong("ContentLength", contentLength);

            savedDomainObject = domainObjectDao.save(attachmentDomainObject, accessToken);

            //предыдущий файл удаляем
            if (oldFilePathValue != null && !oldFilePathValue.isEmpty()) {
                //файл может быть и не удален, в случае если заблокирован
                attachmentDomainObject.setValue(AttachmentServiceImpl.PATH_NAME, oldFilePathValue);
                attachmentContentDao.deleteContent(attachmentDomainObject);
            }
            savedDomainObject.setValue("path", newFilePathValue);
            return savedDomainObject;
        } catch (Exception ex) {
            if (newFilePathValue != null && !newFilePathValue.isEmpty()) {
                attachmentDomainObject.setValue(AttachmentServiceImpl.PATH_NAME, newFilePathValue);
                attachmentContentDao.deleteContent(attachmentDomainObject);
            }
            throw new FatalException("Error save attachment", ex);
        }
    }

    private DomainObject createAttachmentDomainObjectFor(Id objectId, String attachmentType) {
        GenericDomainObject attachmentDomainObject = (GenericDomainObject) createDomainObject(attachmentType);

        String domainObjectType = domainObjectTypeIdCache.getName(objectId);

        String attachmentLinkedField = getAttachmentOwnerObject(attachmentType, domainObjectType);

        attachmentDomainObject.setReference(attachmentLinkedField, objectId);
        return attachmentDomainObject;
    }

    private void saveFile(DeployReportItem item, File tmpFolder) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(tmpFolder, item.getName()));
            out.write(item.getBody());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private void compileReport(File tempFolder) throws IOException, JRException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            InstantiationException, ClassNotFoundException {

        File[] fileList = tempFolder.listFiles();

        List<File> javaFiles = new ArrayList<File>();
        List<File> jrxmlFiles = new ArrayList<File>();

        // Разбираем файлы по коллекциям
        for (int i = 0; i < fileList.length; i++) {
            String fileName = fileList[i].getName();
            if (fileName.indexOf(".java") > 0) {
                javaFiles.add(fileList[i]);
            } else if (fileName.indexOf(".jrxml") > 0) {
                jrxmlFiles.add(fileList[i]);
            }
        }

        // Компиляция джава файлов
        if (javaFiles.size() > 0) {
            scriptletCompilation(javaFiles, tempFolder);
        }

        // Компиляция шаблонов
        for (File jrxmlFile : jrxmlFiles) {
            compileJasper(jrxmlFile);
        }
    }

    private void compileJasper(File fileName) throws JRException {
        ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ScriptletClassLoader scriptletClassLoader =
                    new ScriptletClassLoader(fileName.getParent(), defaultClassLoader);
            Thread.currentThread().setContextClassLoader(scriptletClassLoader);
            logger.debug("Compile template " + fileName);
            String jasperLocation = fileName.getPath().replace(".jrxml", ".jasper");
            JasperCompileManager.compileReportToFile(fileName.getPath(), jasperLocation);
        } finally {
            Thread.currentThread().setContextClassLoader(defaultClassLoader);
        }
    }

    private void scriptletCompilation(List<File> javaFiles, File tempFolder) throws IOException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            InstantiationException, ClassNotFoundException {

        for (File file : javaFiles) {
            logger.debug("Compile class " + file.getName());
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        StandardJavaFileManager stdFileManager = compiler
                .getStandardFileManager(null, Locale.getDefault(), null);

        Iterable<? extends JavaFileObject> compilationUnits = stdFileManager
                .getJavaFileObjectsFromFiles(javaFiles);
        ArrayList<String> compileOptions = new ArrayList<String>();

        compileOptions.add("-cp");
        compileOptions.add(getCompileClassPath());
        compileOptions.add("-d");
        compileOptions.add(tempFolder.getPath());
        compileOptions.add("-encoding");
        compileOptions.add("utf-8");
        compileOptions.add("-g");

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        JavaCompiler.CompilationTask compilerTask = compiler.getTask(null, stdFileManager,
                diagnostics, compileOptions, null, compilationUnits);

        boolean status = compilerTask.call();
        stdFileManager.close();

        if (!status) {
            String message = "";
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                message += diagnostic.toString() + "\n";
            }
            throw new ReportServiceException(message);
        }
    }

    private String getCompileClassPath() throws IOException {
        String[] rootPackages = new String[] { "net","ru","com","org" };
        //Получаем библиотеки для runtime компилятора
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //Список найденых jar библиотек. Необходим чтоб не дублировать jar-ы
        List<String> paths = new ArrayList<String>();
        StringBuilder cp = new StringBuilder();

        for (String rootPackage : rootPackages) {
            Enumeration<URL> urls = classLoader.getResources(rootPackage);
            while (urls.hasMoreElements()) {
                URL url = (URL) urls.nextElement();
                int end = url.getPath().indexOf(".jar");
                String path = url.getPath().substring(0, end + 4);
                if (!paths.contains(path)){
                    cp.append(path).append(File.pathSeparator);
                    paths.add(path);
                }
            }
        }

        return cp.toString();
    }

    private File getTempFolder() throws IOException {
        if (tempFolder == null) {
            File tmpFile = File.createTempFile("report_", "_service.tmp");
            tempFolder = tmpFile.getParentFile();
        }
        return tempFolder;
    }

    private List<DomainObject> getAttachments(String attachmentType, DomainObject attachmentOwner){
        String query = "select t.id from " + attachmentType + " t where t.report_template = "
                + ((RdbmsId)attachmentOwner.getId()).getId();

        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        List<DomainObject> result = new ArrayList<DomainObject>();
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 1000, accessToken);
        for (IdentifiableObject identifiableObject : collection) {
            result.add(domainObjectDao.find(identifiableObject.getId(), accessToken));
        }
        return result;
    }

    private DomainObject getReportTemplateObject(String name) {
        AccessToken accessToken = createSystemAccessToken();
        String query = "select t.id from report_template t where t.name = '" + name + "'";
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 100, accessToken);
        DomainObject result = null;
        if (collection.size() > 0) {
            IdentifiableObject row = collection.get(0);
            result = domainObjectDao.find(row.getId(), accessToken);
        }
        return result;
    }

    private DomainObject createDomainObject(String type) {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(type);
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);
        return domainObject;
    }

    private ReportMetadataConfig loadReportMetadata(byte[] body) throws Exception {
        Serializer serializer = new Persister();
        ByteArrayInputStream stream = new ByteArrayInputStream(body);
        ReportMetadataConfig config = serializer.read(ReportMetadataConfig.class, stream);
        return config;
    }

    private void deleteAttachment(Id attachmentDomainObjectId) {
        try {
            AccessToken accessToken = createSystemAccessToken();
            DomainObject attachmentDomainObject = domainObjectDao.find(attachmentDomainObjectId, accessToken);
            attachmentContentDao.deleteContent(attachmentDomainObject);
            domainObjectDao.delete(attachmentDomainObjectId, accessToken);
        } catch (DaoException ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException(ex.getMessage() + " Id:" + attachmentDomainObjectId);
        }
    }

    private AccessToken createSystemAccessToken() {
        return accessControlService.createSystemAccessToken(this.getClass().getName());
    }

    private String getAttachmentOwnerObject(String attachmentType, String domainObjectType) {
        DomainObjectTypeConfig objectConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, domainObjectType);

        String declaringAttachmentDomainObject = null;
        if (objectConfig.getAttachmentTypesConfig() != null
                && objectConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs().size() > 0) {

            for (AttachmentTypeConfig attachmentTypeConfig : objectConfig.getAttachmentTypesConfig()
                    .getAttachmentTypeConfigs()) {
                if (attachmentType.equals(attachmentTypeConfig.getName())) {
                    declaringAttachmentDomainObject = domainObjectType;
                    break;
                }
            }
        }

        if (declaringAttachmentDomainObject == null) {
            String parentType = objectConfig.getExtendsAttribute();
            if (parentType != null) {
                return getAttachmentOwnerObject(attachmentType, parentType);
            }
        }
        if (declaringAttachmentDomainObject == null) {
            throw new FatalException("Attachment declaration not found for " + attachmentType);
        }
        return declaringAttachmentDomainObject;
    }

    private long getContentLength(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        long result = 0;
        long read = 0;
        byte[] buffer = new byte[1024];
        while ((read = inputStream.read(buffer)) > 0) {
            result += read;
        }
        return result;
    }
}
