package ru.intertrust.cm.core.business.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

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

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;

import org.apache.log4j.Logger;
import org.jboss.vfs.VirtualFile;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.DeployReportData;
import ru.intertrust.cm.core.business.api.dto.DeployReportItem;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.model.*;
import ru.intertrust.cm.core.report.ReportServiceBase;
import ru.intertrust.cm.core.report.ScriptletClassLoader;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;

/**
 * Имплементация сервися администрирования подсистемы отчетов
 * @author larin
 *
 */
@Stateless(name = "ReportServiceAdmin")
@Local(ReportServiceAdmin.class)
@Remote(ReportServiceAdmin.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ReportServiceAdminImpl extends ReportServiceBase implements ReportServiceAdmin {

    private Logger logger = Logger.getLogger(ReportServiceAdminImpl.class);

    /**
     * Установка отчета в систему
     */
    @Override
    public void deploy(DeployReportData deployReportData, boolean lockUpdate) {
        try {

            //Получаем все новые вложения и ищем файл с метаинформацией
            ReportMetadataConfig reportMetadata = null;
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

            //Получаем все вложения из временной директории и сохраняем их как вложения
            File[] filelist = tmpFolder.listFiles();
            
            //Поиск шаблона по имени
            DomainObject reportTemplateObject = getReportTemplateObject(reportMetadata.getName());

            //Если не существует то создаем новый
            if (reportTemplateObject == null) {
                reportTemplateObject = createDomainObject("report_template");
                reportTemplateObject.setString("name", reportMetadata.getName());
                updateReportTemplate(reportTemplateObject, reportMetadata, filelist, lockUpdate);

            } else {
            	Boolean dopLockUpdate = reportTemplateObject.getBoolean("lockUpdate");
            	//Для существующих отчётов
            	if (dopLockUpdate == null){
            		dopLockUpdate = false;
            	}
            	if ((!dopLockUpdate) || (lockUpdate)){
	                //Если существует то удаляем все вложения по нему
	                List<DomainObject> attachments = getAttachments("report_template_attach", reportTemplateObject);
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
    
    private void updateReportTemplate(DomainObject reportTemplateObject,ReportMetadataConfig reportMetadata,File[] filelist, boolean lockUpdate) throws IOException{
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
        try {
            //TODO переделать на админ токен
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            //Поиск шаблона по имени
            DomainObject reportTemplateObject = getReportTemplateObject(name);
            //Удаляем сначала все связанные вложения
            List<DomainObject> attachments = domainObjectDao.findLinkedDomainObjects(reportTemplateObject.getId(), "report_template_attach", "report_template",  accessToken);
            deleteDomainObjects(attachments, accessToken);            
            //Удаляем сначала все связанные результаты генерации и их вложения
            List<DomainObject> results = (domainObjectDao.findLinkedDomainObjects(reportTemplateObject.getId(), "report_result", "template_id",  accessToken));
            for (DomainObject childObject : results) {
                List<DomainObject> resultAttachs = (domainObjectDao.findLinkedDomainObjects(childObject.getId(), "report_result_attachment", "report_result",  accessToken));
                deleteDomainObjects(resultAttachs, accessToken);
            }
            deleteDomainObjects(results, accessToken);
            
            domainObjectDao.delete(reportTemplateObject.getId(), accessToken);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in undeploy", ex);
            throw new UnexpectedException("ReportServiceAdmin", "undeploy", "name: " + name, ex);
        }
    }
    
    private void deleteDomainObjects(List<DomainObject> childObjects, AccessToken accessToken){
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

    private void compileJasper(File fileName){
        ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ScriptletClassLoader scriptletClassLoader =
                    new ScriptletClassLoader(fileName.getParent(), defaultClassLoader);
            Thread.currentThread().setContextClassLoader(scriptletClassLoader);
            logger.debug("Compile template " + fileName);
            String jasperLocation = fileName.getPath().replace(".jrxml", ".jasper");
            JasperCompileManager.compileReportToFile(fileName.getPath(), jasperLocation);
        }catch(Exception ex){
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
        String[] rootPackages = new String[] {"net", "ru", "com", "org"};
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
                    }  else { // process "file:" and other urls as usual
                        int end = url.getPath().indexOf(".jar") + 4;
                        int start = url.getPath().indexOf(":/") + 2;
                        path = url.getPath().substring(start, end);
                    }
                    if (!paths.contains(path)){
                        cp.append(path).append(File.pathSeparator);
                        paths.add(path);
                    }
                }
            }
        }

        return cp.toString();
    }

}
