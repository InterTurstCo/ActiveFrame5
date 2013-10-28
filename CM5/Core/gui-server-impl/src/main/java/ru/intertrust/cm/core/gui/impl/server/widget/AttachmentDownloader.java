package ru.intertrust.cm.core.gui.impl.server.widget;

import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */
@Controller
public class AttachmentDownloader {
    private static final Logger log = LoggerFactory.getLogger(AttachmentUploader.class);
    @Autowired
    protected CrudService crudService;

    @Autowired
    protected IdService idService;

  /*  @Controller
    @RequestMapping(value = "/somewhere/new")
    public class SomewhereController {

        @RequestMapping(method = RequestMethod.POST)
        public String post(
                @ModelAttribute("newObject") NewObject newObject) {
            // ...
        }       */
        @RequestMapping(value = "/attachments/{id}", method = RequestMethod.GET)
        public void getFile(@PathVariable("id") String id, HttpServletResponse response) {
            try {
                Id rdmsId = idService.createId(id);
                DomainObject domainObject = crudService.find(rdmsId);
                String filePath = domainObject.getString("Path");

                    InputStream is = new FileInputStream(filePath);
                    RemoteInputStreamServer remoteFileData = new SimpleRemoteInputStream(is);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

        /*        // get MIME type of the file
              String mimeType = domainObject.getString("");
                if (mimeType == null) {
                    // set to binary type if MIME mapping not found
                    mimeType = "application/octet-stream";
                }
                System.out.println("MIME type: " + mimeType);

                // set content attributes for the response
                response.setContentType(mimeType);
                response.setContentLength((int) downloadFile.length());

                // set headers for the response
                String headerKey = "Content-Disposition";
                String headerValue = String.format("attachment; filename=\"%s\"",
                        downloadFile.getName());
                response.setHeader(headerKey, headerValue);

                // get output stream of the response
                OutputStream outStream = response.getOutputStream();

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead = -1;

                // write bytes read from the input stream into the output stream
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }

                inputStream.close();
                outStream.close();


            } catch (IOException ex) {
                log.info("Error writing file to output stream. File with '" + id + "'");
                throw new RuntimeException("IOError writing file to output stream");
            }
                */
        }  }

