package ru.intertrust.cm.core.gui.impl.server.widget;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */
@Controller
public class AttachmentDownloader {

    private static final long MAX_AGE = 60*60*24*365;
    private static final int BUFFER_SIZE = 1024*64;

    @Autowired
    protected CrudService crudService;
    @Autowired
    protected IdService idService;
    @Autowired
    protected AttachmentService attachmentService;
    @Autowired
    protected AttachmentService.Remote remoteAttachmentService;
    @Autowired
    private PropertyResolver propertyResolver;

    private String attachmentTempStoragePath;

    @PostConstruct
    public void init() {
        attachmentTempStoragePath = propertyResolver.resolvePlaceholders("${attachment.temp.storage}");
    }

    @RequestMapping(value = "attachment-download", method = RequestMethod.GET)
    public void getFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userAgent = request.getHeader("user-agent");
        String id = request.getParameter("id");
        String path = request.getParameter("tempName");
        String absolutePath = attachmentTempStoragePath + path;
        String filename;
        RemoteInputStream remoteFileData = null;

        if (id != null) {
            Id rdmsId = idService.createId(id);
            DomainObject domainObject = crudService.find(rdmsId);
            String mimeType = domainObject.getString("MimeType");
            response.setContentType(mimeType);
            remoteFileData = attachmentService.loadAttachment(rdmsId);
            filename = URLEncoder.encode(domainObject.getString("Name"),"UTF-8");
            response.setHeader("Content-Length", domainObject.getLong("contentLength)") + "");
        } else {
            filename = path;
            response.setHeader("Content-Length", String.valueOf(new File(absolutePath).length()));
        }
        response.addHeader("Cache-Control", "public, max-age=" + MAX_AGE);
        response.setBufferSize(BUFFER_SIZE);
        response.setCharacterEncoding("UTF-8");
        //For Firefox encoding issue
        String contentDispositionPart = userAgent.indexOf("Firefox") > 0 ? "attachment; filename*='UTF-8'"
                :"attachment; filename=\"";
        response.setHeader("Content-disposition", contentDispositionPart + filename + "\"");

        try (InputStream fileData = (id != null ?  RemoteInputStreamClient.wrap(remoteFileData)
             : new FileInputStream(absolutePath)); ) {
            Date start = new Date();
            stream(fileData, response.getOutputStream());
            Date end = new Date();
            System.out.println("Copy time: " + (end.getTime() - start.getTime()));
        }
    }

    private void stream(InputStream input, OutputStream output) throws IOException {
        try (
            ReadableByteChannel inputChannel = Channels.newChannel(input);
            WritableByteChannel outputChannel = Channels.newChannel(output)) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.clear();
            }
        }
    }

}

