package ru.intertrust.cm.core.gui.impl.server.widget;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.io.FileNotFoundException;
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
    private static final Logger log = LoggerFactory.getLogger(AttachmentDownloader.class);
    @Autowired
    protected CrudService crudService;
    @Autowired
    protected IdService idService;
    @Autowired
    protected AttachmentService attachmentService;

        @RequestMapping(value = "{id}", method = RequestMethod.GET)
        public void getFile(@PathVariable("id") String id, HttpServletResponse response) {
          try {
              Id rdmsId = idService.createId(id);
              DomainObject domainObject = crudService.find(rdmsId);
              RemoteInputStream remoteFileData = attachmentService.loadAttachment(domainObject);
              InputStream fileData = RemoteInputStreamClient.wrap(remoteFileData);

              response.setHeader("Content-Disposition", "attachment; filename=" + domainObject.getString("Name"));
              response.setContentType("application/download");
              stream (fileData, response.getOutputStream());

                } catch (FileNotFoundException e) {
                    log.error(e.getMessage());
                } catch (IOException e) {
                    log.error(e.getMessage());
          }

        }

    private long stream(InputStream input, OutputStream output) throws IOException {

        try (
            ReadableByteChannel inputChannel = Channels.newChannel(input);
            WritableByteChannel outputChannel = Channels.newChannel(output);)
        {ByteBuffer buffer = ByteBuffer.allocate(10240);
            long size = 0;

            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                size += outputChannel.write(buffer);
                buffer.clear();
            }

            return size;
        }

    }
}

