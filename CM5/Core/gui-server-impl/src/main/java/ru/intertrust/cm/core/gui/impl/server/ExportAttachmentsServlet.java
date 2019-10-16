package ru.intertrust.cm.core.gui.impl.server;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.AttachmentTypesConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.TicketService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@WebServlet(name = "ExportAttachmentsServlet", urlPatterns = { "/remote/service/export-attachments" }, asyncSupported = true)
public class ExportAttachmentsServlet extends HttpServlet {
    private static final long serialVersionUID = -8347564224364047817L;

    private static final Logger logger = LoggerFactory.getLogger(ExportAttachmentsServlet.class);

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private CrudService crudService;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private CollectionsService collectionsService;
    
    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private CurrentUserAccessor currentUserAccessor;
    
    private static List<ExportThread> exportThreads = new ArrayList<ExportThread>();

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

            if (types == null) {
                types = "";
            } else {
                ExportThread exportThread = new ExportThread(
                        attachmentService, 
                        crudService, 
                        configurationExplorer, 
                        collectionsService, 
                        domainObjectTypeIdCache, 
                        currentUserAccessor, 
                        types, 
                        ticketService.createTicket());
                exportThreads.add(exportThread);
                exportThread.start();
                resp.sendRedirect(req.getServletContext().getContextPath() + "/remote/service/export-attachments");
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
            out.print(
                    "<td>Типы ДО, перечисленные через точку с запятой (например <b>country;person</b>), так же можно указать запрос (например <b>select id from person where login like '%ivanov%'</b>)</td>");
            out.print("</tr>");
            out.print("<tr><td><input type='text' name='types' value='" + types + "' style='width: 800px;'></td></tr>");
            

            out.print("<tr>");
            out.print("<td><input type='submit' value='Выгрузить'></td>");
            out.print("</tr>");
            out.print("</table>");
            out.print("</form>");

            out.print("<table border='1'>");
            out.print("<tr><th>task</th><th>status</th><th>param</th><th>link</th></tr>");
            for (int i=0; i<exportThreads.size(); i++) {
                out.print("<tr>");
                out.print("<td>" + (i+1) + "</td>");
                out.print("<td>");
                if (exportThreads.get(i).isAlive()){
                    out.print("Run");
                }else if(exportThreads.get(i).getError() != null){
                    out.print("Error");
                }else{
                    out.print("Complete");
                }
                out.print("</td>");
                out.print("<td>" + exportThreads.get(i).getTypes() + "</td>");
                out.print("<td><a href='export-attachments?archive=" + exportThreads.get(i).getLink() + "'>Загрузить архив " + exportThreads.get(i).getLink() + "</a></td>");
                out.print("</tr>");
            }
            out.print("</table>");

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
        try (FileInputStream fis = new FileInputStream(zipFile)) {
            try (OutputStream os = resp.getOutputStream()) {
                int read = 0;
                byte[] buffer = new byte[1024];
                while ((read = fis.read(buffer)) > 0) {
                    os.write(buffer, 0, read);
                }
            }
        }
    }

    public static class ExportThread extends Thread {
        private AttachmentService attachmentService;
        private CrudService crudService;
        private ConfigurationExplorer configurationExplorer;
        private CollectionsService collectionsService;
        private DomainObjectTypeIdCache domainObjectTypeIdCache;
        private CurrentUserAccessor currentUserAccessor;
        private String ticket;
        private String types;
        private String link;
        private Exception error;
        
        public ExportThread(AttachmentService attachmentService, 
                CrudService crudService, 
                ConfigurationExplorer configurationExplorer, 
                CollectionsService collectionsService, 
                DomainObjectTypeIdCache domainObjectTypeIdCache,
                CurrentUserAccessor currentUserAccessor,
                String types, 
                String ticket){
            this.attachmentService = attachmentService;
            this.crudService = crudService; 
            this.configurationExplorer = configurationExplorer;
            this.collectionsService = collectionsService;
            this.domainObjectTypeIdCache = domainObjectTypeIdCache;
            this.types = types;
            this.currentUserAccessor = currentUserAccessor;
            this.ticket = ticket;
        }

        public void run() {
            try{
                currentUserAccessor.setTicket(ticket);
                link = exportContents(types);
            }catch(Exception ex){
                logger.error("Error export attachments", ex);
                error = ex;
                link = createErrorFile(ex);
            }finally {
                currentUserAccessor.cleanTicket();
            }
        }
        
        private String createErrorFile(Exception ex){
            try{
                String errorText = ExceptionUtils.getStackTrace(ex);
                String prefix = ThreadSafeDateFormat.format(new Date(), "yyyy-MM-dd_HH-mm-ss");
                File tempFile = File.createTempFile("export-error-" + prefix + "-", ".txt");
                try(FileOutputStream fos = new FileOutputStream(tempFile)) {
                    StreamUtils.copy(errorText, Charset.forName("utf-8"), fos);
                }
                return tempFile.getName();
            }catch(Exception ignoreEx){
                logger.error("Error export attachments", ex);
                return null;
            }
        }
        
        private String exportContents(String types) throws IOException {
            String prefix = ThreadSafeDateFormat.format(new Date(), "yyyy-MM-dd_HH-mm-ss");
            File tempFile = File.createTempFile("export-" + prefix + "-", ".zip");
            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(tempFile))) {
                String[] typeArr = types.split("[;]");
                for (String typeName : typeArr) {
                    
                    if (Case.toLower(typeName).startsWith("select")){
                        //Поиск по запросу
                        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(typeName);
                        for (IdentifiableObject identifiableObject : collection) {
                            String ownertype = domainObjectTypeIdCache.getName(identifiableObject.getId());

                            DomainObjectTypeConfig config = configurationExplorer.getDomainObjectTypeConfig(ownertype);
                            AttachmentTypesConfig attachTypesConfig = config.getAttachmentTypesConfig();
                            for (AttachmentTypeConfig attachTypeConfig : attachTypesConfig.getAttachmentTypeConfigs()) {
                                List<DomainObject> allAttach = crudService.findLinkedDomainObjects(identifiableObject.getId(), attachTypeConfig.getName(), ownertype);
                                for (DomainObject domainObject : allAttach) {
                                    byte[] content = getAttachmentContent(domainObject);
                                    if (content != null) {
                                        zipOut.putNextEntry(new ZipEntry(domainObject.getString("path").substring(1)));
                                        zipOut.write(content);
                                    }
                                }
                            }
                        }
                    }else{
                        //Поиск по типу
                        DomainObjectTypeConfig config = configurationExplorer.getDomainObjectTypeConfig(typeName);
                        AttachmentTypesConfig attachTypesConfig = config.getAttachmentTypesConfig();
                        for (AttachmentTypeConfig attachTypeConfig : attachTypesConfig.getAttachmentTypeConfigs()) {
                            List<DomainObject> allAttach = crudService.findAll(attachTypeConfig.getName());
                            for (DomainObject domainObject : allAttach) {
                                byte[] content = getAttachmentContent(domainObject);
                                if (content != null) {
                                    zipOut.putNextEntry(new ZipEntry(domainObject.getString("path").substring(1)));
                                    zipOut.write(content);
                                }
                            }
                        }
                    }
                }
                return tempFile.getName();
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
                logger.warn("Error load attachment " + attachment.getId(), ex);
                return null;
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

        public String getLink() {
            return link;
        }

        public Exception getError() {
            return error;
        }

        public String getTypes() {
            return types;
        }

    }

}
