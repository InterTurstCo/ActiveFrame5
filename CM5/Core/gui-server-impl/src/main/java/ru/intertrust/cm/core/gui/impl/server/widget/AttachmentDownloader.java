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
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static ru.intertrust.cm.core.business.api.dto.util.ModelConstants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */
@Controller
public class AttachmentDownloader {

    private static final long MAX_AGE = 60*60*24*365;
    private static final int BUFFER_SIZE = 1024*64;
    public static final String ATTACHMENT_TEMP_STORAGE_PLACEHOLDER = "${attachment.temp.storage}";
    public static final String UTF_8 = "UTF-8";
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
        attachmentTempStoragePath = propertyResolver.resolvePlaceholders(ATTACHMENT_TEMP_STORAGE_PLACEHOLDER);
    }

    @RequestMapping(value = "attachment-download", method = RequestMethod.GET)
    public void getFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userAgent = request.getHeader("user-agent");
        String id = request.getParameter(DOWNLOAD_ID);
        File tempFile = null;
        String filename;
        long contentLength;
        RemoteInputStream remoteFileData = null;

        if (id != null) {
            Id rdmsId = idService.createId(id);
            DomainObject domainObject = crudService.find(rdmsId);
            String mimeType = domainObject.getString("MimeType");
            response.setContentType(mimeType);
            remoteFileData = attachmentService.loadAttachment(rdmsId);
            filename = domainObject.getString("Name");
            contentLength = remoteFileData.available();
        } else {
            filename = getTempNameFromRequestQuery(request.getQueryString());
            tempFile = new File(attachmentTempStoragePath, filename);
            contentLength = tempFile.length();
        }
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.addHeader("Cache-Control", "public, max-age=" + MAX_AGE);
        response.setBufferSize(BUFFER_SIZE);
        response.setCharacterEncoding(UTF_8);

        filename = URLEncoder.encode(filename, UTF_8).replaceAll("\\+","%20");
        //For Firefox encoding issue
        String contentDispositionPart = userAgent.indexOf("Firefox") > 0 ? "attachment; filename*=UTF-8''" + filename
                :"attachment; filename=\"" + filename + "\"";
        response.setHeader("Content-disposition", contentDispositionPart);

        try (InputStream fileData = (id != null ?  RemoteInputStreamClient.wrap(remoteFileData)
             : new FileInputStream(tempFile)); ) {
            stream(fileData, response.getOutputStream());
            response.flushBuffer();
        }
    }

    private String getTempNameFromRequestQuery(String requestQuery) throws UnsupportedEncodingException {
        String decodedRequest = URLDecoder.decode(requestQuery, UTF_8);
        String replacement = new StringBuilder(DOWNLOAD_TEMP_NAME).append(DOWNLOAD_EQUAL).toString();
        return decodedRequest.replace(replacement,"");
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

