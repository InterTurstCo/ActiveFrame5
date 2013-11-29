package ru.intertrust.cm.core.business.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
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
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.DeployReportData;
import ru.intertrust.cm.core.business.api.dto.DeployReportItem;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.model.ReportServiceException;
import ru.intertrust.cm.core.report.ReportServiceBase;
import ru.intertrust.cm.core.report.ScriptletClassLoader;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

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
    public void deploy(DeployReportData deployReportData) {
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

            //TODO переделать на админ токен
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            //Поиск шаблона по имени
            DomainObject reportTemplateObject = getReportTemplateObject(reportMetadata.getName());

            //Если не существует то создаем новый
            if (reportTemplateObject == null) {
                reportTemplateObject = createDomainObject("report_template");
                reportTemplateObject.setString("name", reportMetadata.getName());
            } else {
                //Если существует то удаляем все вложения по нему
                List<DomainObject> attachments = getAttachments("report_template_attachment", reportTemplateObject);
                for (DomainObject attachment : attachments) {
                    attachmentService.deleteAttachment(attachment);
                }
            }
            reportTemplateObject.setString("description", reportMetadata.getDescription());
            reportTemplateObject = domainObjectDao.save(reportTemplateObject, accessToken);

            //Получаем все вложения из временной директории и сохраняем их как вложения
            File[] filelist = tmpFolder.listFiles();
            for (File file : filelist) {
                DomainObject attachment =
                        attachmentService.createAttachmentDomainObjectFor(reportTemplateObject.getId(), "report_template_attachment");
                attachment.setString("Name", file.getName());
                ByteArrayInputStream bis = new ByteArrayInputStream(readFile(file));
                SimpleRemoteInputStream simpleRemoteInputStream = new SimpleRemoteInputStream(bis);

                RemoteInputStream remoteInputStream;
                remoteInputStream = simpleRemoteInputStream.export();
                attachmentService.saveAttachment(remoteInputStream, attachment);
                
                //Удаляем, больше нам не нужен
                file.delete();
            }
            
            tmpFolder.delete();
        } catch (Exception ex) {
            throw new ReportServiceException("Error deploy process", ex);
        }

    }

    private void saveFile(DeployReportItem item, File tmpFolder) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(tmpFolder, item.getName()));
            out.write(item.getBody());
        } finally {
            if (out != null){
                out.close();
            }
        }
    }

    /**
     * Удаление шаблона отчета по имени
     */
    @Override
    public void undeploy(String name) {
        //TODO переделать на админ токен
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        //Поиск шаблона по имени
        DomainObject reportTemplateObject = getReportTemplateObject(name);
        domainObjectDao.delete(reportTemplateObject.getId(), accessToken);
    }

    private void compileReport(File tempFolder) throws IOException, JRException {

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

    private void scriptletCompilation(List<File> javaFiles, File tempFolder) throws IOException {

        for (File file : javaFiles) {
            logger.debug("Compile class " + file.getName());
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        StandardJavaFileManager stdFileManager = compiler
                .getStandardFileManager(null, Locale.getDefault(), null);

        Iterable<? extends JavaFileObject> compilationUnits = stdFileManager
                .getJavaFileObjectsFromFiles(javaFiles);
        ArrayList<String> compileOptions = new ArrayList<String>();

        URLClassLoader defaultClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        StringBuilder cp = new StringBuilder();
        for (URL url : defaultClassLoader.getURLs()) {
            cp.append(url.getFile()).append(File.pathSeparator);
        }
        compileOptions.add("-cp");
        compileOptions.add(cp.toString());
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

}
