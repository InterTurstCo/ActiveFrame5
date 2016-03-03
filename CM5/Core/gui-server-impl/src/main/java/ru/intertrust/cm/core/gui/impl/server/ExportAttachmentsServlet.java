package ru.intertrust.cm.core.gui.impl.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.AttachmentTypesConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.model.FatalException;

@WebServlet(name = "ExportAttachmentsServlet", urlPatterns = { "/remote/service/export-attachments" }, asyncSupported = true)
public class ExportAttachmentsServlet extends HttpServlet {
    private static final long serialVersionUID = -8347564224364047817L;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private CrudService crudService;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

        String types = req.getParameter("types");
        String archive = req.getParameter("archive");
        if (archive != null) {
            sendArchive(resp, archive);
        } else {
            PrintWriter out = resp.getWriter();

            String link = null;
            if (types == null) {
                types = "";
            } else {
                link = exportContents(types);
            }

            out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
            out.print("<html>");
            out.print("<head><title>");
            out.print("Выгрузка вложений");
            out.print("</title>");
            out.print("<meta charset='utf-8'>");
            out.print("</head>");
            out.print("<body>");
            out.print("<p>Сервис выгрузки всех вложений всех типов вложений, прикрпленных к определенному типу доменного объекта.");
            out.print("<form>");
            out.print("<table>");
            out.print("<tr>");
            out.print("<td>Типы ДО, перечисленные через запятую (например <b>country</b>)</td>");
            out.print("<td><input type='text' name='types' value='" + types + "' style='width: 300px;'></td>");
            out.print("</tr>");

            out.print("<tr>");
            out.print("<td><input type='submit' value='Выгрузить'></td>");
            out.print("</tr>");
            out.print("</table>");
            out.print("</form>");

            if (link != null) {
                out.print("<a href='export-attachments?archive=" + link + "'>Загрузить архив</a>");
            }

            out.print("</body>");
            out.print("</html>");
        }

    }

    private void sendArchive(HttpServletResponse resp, String archive) throws IOException {
        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition",
                 String.format("attachment; filename=\"%s\"", archive));
        
        File tempDir = File.createTempFile("test-", ".zip").getParentFile();
        File zipFile = new File(tempDir, archive);
        try(FileInputStream fis = new FileInputStream(zipFile)){
            try(OutputStream os = resp.getOutputStream()){
                int read = 0;
                byte[] buffer = new byte[1024];
                while((read = fis.read(buffer)) > 0){
                    os.write(buffer, 0, read);
                }
            }
        }
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
            throw new FatalException("Error on get attachment body", ex);
        } finally {
            try {
                if (contentStream != null) {
                    contentStream.close();
                }
                if (inputStream != null) {
                    inputStream.close(true);
                }
            } catch (IOException ignoreEx) {
            }
        }
    }

    private String exportContents(String types) throws IOException {
        String prefix =ThreadSafeDateFormat.format(new Date(), "yyyy-MM-dd_HH-mm-ss");
        File tempFile = File.createTempFile("export-" + prefix + "-", ".zip");
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(tempFile))) {
            String[] typeArr = types.split(",");
            for (String typeName : typeArr) {
                DomainObjectTypeConfig config = configurationExplorer.getDomainObjectTypeConfig(typeName);
                AttachmentTypesConfig attachTypesConfig = config.getAttachmentTypesConfig();
                for (AttachmentTypeConfig attachTypeConfig : attachTypesConfig.getAttachmentTypeConfigs()) {
                    List<DomainObject> allAttach = crudService.findAll(attachTypeConfig.getName());
                    for (DomainObject domainObject : allAttach) {
                        byte[] content = getAttachmentContent(domainObject);
                        zipOut.putNextEntry(new ZipEntry(domainObject.getString("path").substring(1)));
                        zipOut.write(content);
                    }
                }
            }
            return tempFile.getName();
        }
    }

}
