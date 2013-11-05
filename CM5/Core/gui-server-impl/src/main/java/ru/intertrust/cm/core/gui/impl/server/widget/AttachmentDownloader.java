package ru.intertrust.cm.core.gui.impl.server.widget;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */
@Controller
public class AttachmentDownloader {

    @Autowired
    protected CrudService crudService;
    @Autowired
    protected IdService idService;
    @Autowired
    protected AttachmentService attachmentService;

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public void getFile(@PathVariable("id") String id, HttpServletResponse response) throws IOException {
        Id rdmsId = idService.createId(id);
        DomainObject domainObject = crudService.find(rdmsId);
        String mimeType = domainObject.getString("MimeType");
        RemoteInputStream remoteFileData = null;
        try {
            remoteFileData = attachmentService.loadAttachment(domainObject);
            InputStream fileData = RemoteInputStreamClient.wrap(remoteFileData);
            response.setHeader("Content-Disposition", "attachment; filename=" + domainObject.getString("Name"));
            response.setContentType(mimeType);
            stream(fileData, response.getOutputStream());
        } finally {
            close(remoteFileData);
        }

    }

    private void stream(InputStream input, OutputStream output) throws IOException {

        try (
                ReadableByteChannel inputChannel = Channels.newChannel(input);
                WritableByteChannel outputChannel = Channels.newChannel(output)) {
            ByteBuffer buffer = ByteBuffer.allocate(10240);
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.clear();
            }
        }

    }

    private void close(RemoteInputStream stream) throws IOException {
        if (stream == null) {
            return;
        }

        stream.close(true);
    }

}

