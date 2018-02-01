package ru.intertrust.cm.core.business.impl;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import org.apache.log4j.Logger;
import org.jboss.vfs.VirtualFile;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.*;
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

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Имплементация сервися администрирования подсистемы отчетов
 *
 * @author larin
 */
@Stateless(name = "ReportServiceAdmin")
@Local(ReportServiceAdmin.class)
@Remote(ReportServiceAdmin.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ReportServiceAdminImpl extends ReportServiceBase implements ReportServiceAdmin {

    private Logger logger = Logger.getLogger(ReportServiceAdminImpl.class);
    private static Collection<DomainObjectTypeConfig> configurations;


    private int getReportHash(DeployReportData deployReportData) throws Exception{
        Map<Integer, Integer> hashMap = new HashMap<>();
        List<Integer> nameHash = new ArrayList<>();
        for (DeployReportItem item : deployReportData.getItems()) {
            hashMap.put(item.getName().hashCode(), Arrays.hashCode(item.getBody()));
            nameHash.add(item.getName().hashCode());
        }

        Collections.sort(nameHash);
        StringBuilder builder = new StringBuilder();
        for(Integer item : nameHash){
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
            ReportMetadataConfig reportMetadata = null;
            for (DeployReportItem item : deployReportData.getItems()) {
                //Ищем вложение с метаданными по имени
                if (item.getName().equalsIgnoreCase(METADATA_FILE_MAME)) {
                    reportMetadata = loadReportMetadata(item.getBody());
                }
            }
            DomainObject reportTemplateObject = getReportTemplateObject(reportMetadata.getName());
            if(reportTemplateObject != null && Long.valueOf(reportHash).equals(reportTemplateObject.getLong("reportHash"))){
                return;
            }



            //Получаем все новые вложения и ищем файл с метаинформацией
            File tmpFolder = new File(getTempFolder(), "deploy_report_" + System.currentTimeMillis());
            //File tmpFolder = File.createTempFile("report_", "_template");
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

            logger.info("Deploy report " + reportMetadata.getName());

            //Получаем все вложения из временной директории и сохраняем их как вложения
            File[] filelist = tmpFolder.listFiles();

            //Поиск шаблона по имени
            reportTemplateObject = getReportTemplateObject(reportMetadata.getName());

            //Если не существует то создаем новый
            if (reportTemplateObject == null) {
                reportTemplateObject = createDomainObject("report_template");
                reportTemplateObject.setString("name", reportMetadata.getName());
                reportTemplateObject.setLong("reportHash", Long.valueOf(reportHash));
                updateReportTemplate(reportTemplateObject, reportMetadata, filelist, lockUpdate);

            } else {
                Boolean dopLockUpdate = reportTemplateObject.getBoolean("lockUpdate");
                reportTemplateObject.setLong("reportHash", Long.valueOf(reportHash));
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
                        if (results.size() == 0)
                            continue;
                        // Если есть типы ссылающиеся на этот ДО, идем глубже
                        if (results.size() > 0 && hasRef) {
                            for (DomainObject r : results) {
                                deleteCascade(dObject.getName(), r.getId());
                            }
                        }
                        //удаляем строки со ссылкой на исходный ДО
                        if (results.size() > 0) {
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

    private void compileReport(File tempFolder) throws IOException, JRException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            InstantiationException, ClassNotFoundException, URISyntaxException {

        File[] filelist = tempFolder.listFiles();

        List<File> javaFiles = new ArrayList<File>();
        List<File> jrxmlFiles = new ArrayList<File>();

        // Разбираем файлы по коллекциям
        for (int i = 0; i < filelist.length; i++) {
            String fileName = filelist[i].getName();
            if (fileName.indexOf(".java") > 0) {
                javaFiles.add(filelist[i]);
            } else if (fileName.indexOf(".jrxml") > 0) {
                jrxmlFiles.add(filelist[i]);
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

    private void compileJasper(File fileName) {
        ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ScriptletClassLoader scriptletClassLoader =
                    new ScriptletClassLoader(fileName.getParent(), defaultClassLoader);
            Thread.currentThread().setContextClassLoader(scriptletClassLoader);
            logger.debug("Compile template " + fileName);
            String jasperLocation = fileName.getPath().replace(".jrxml", ".jasper");
            JasperCompileManager.compileReportToFile(fileName.getPath(), jasperLocation);
        } catch (Exception ex) {
            throw new ReportServiceException("Error compile " + fileName, ex);
        } finally {
            Thread.currentThread().setContextClassLoader(defaultClassLoader);
        }
    }

    private void scriptletCompilation(List<File> javaFiles, File tempFolder) throws IOException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            InstantiationException, ClassNotFoundException, URISyntaxException {

        for (File file : javaFiles) {
            logger.debug("Compile class " + file.getName());
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null){
        	throw new FatalException("Application start with JRE, but need JDK");
        }
        

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

        CompilationTask compilerTask = compiler.getTask(null, stdFileManager,
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

    private String getCompileClassPath() throws IOException, URISyntaxException {
        String[] rootPackages = new String[]{"net", "ru", "com", "org", "lotus"};
        //Получаем библиотеки для runtime компилятора
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //Список найденых jar библиотек. Необходим чтоб не дублировать jar-ы
        List<String> paths = new ArrayList<String>();
        StringBuilder cp = new StringBuilder();

        String path;
        for (String rootPackage : rootPackages) {
            Enumeration<URL> urls = classLoader.getResources(rootPackage);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url.getPath().contains(".jar")) {
                    if ("vfs".equals(url.getProtocol())) {
                        int end = url.toString().indexOf(".jar") + 4;
                        URL urlToJar = new URL(url.toString().substring(0, end));
                        URLConnection conn = urlToJar.openConnection();
                        VirtualFile vf = (VirtualFile) conn.getContent();
                        File physicalFile = vf.getPhysicalFile();
                        String dirName = physicalFile.getParent();
                        String fileName = vf.getName();
                        path = new File(dirName, fileName).getPath();
                    } else { // process "file:" and other urls as usual
                        int end = url.getPath().indexOf(".jar") + 4;
                        int start = url.getPath().indexOf(":/") + 2;
                        path = url.getPath().substring(start, end);
                    }
                    if (!paths.contains(path)) {
                        cp.append(path).append(File.pathSeparator);
                        paths.add(path);
                    }
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
            DeployReportData deployReportData = getReportData(identifiableObject.getId(), accessToken);
            if (deployReportData.getItems().size() > 0) {
                logger.info("Recompile report " + identifiableObject.getString("name"));
                deploy(deployReportData, identifiableObject.getBoolean("lockupdate") != null && identifiableObject.getBoolean("lockupdate"));
            }
        }
        logger.info("End recompile all reports");
    }

    private DeployReportData getReportData(Id templateId, AccessToken accessToken) {
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

    protected byte[] getAttachmentContent(DomainObject attachment) {
        InputStream contentStream = null;
        RemoteInputStream inputStream = null;
        try {
            inputStream = attachmentService.loadAttachment(attachment.getId());
            contentStream = RemoteInputStreamClient.wrap(inputStream);
            ByteArrayOutputStream attachmentBytes = new ByteArrayOutputStream();

            int read = 0;
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
                inputStream.close(true);
            } catch (IOException ignoreEx) {
            }
        }
    }


}
