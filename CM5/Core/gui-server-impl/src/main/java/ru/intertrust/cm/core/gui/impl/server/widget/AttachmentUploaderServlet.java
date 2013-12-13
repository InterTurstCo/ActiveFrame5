package ru.intertrust.cm.core.gui.impl.server.widget;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 12.12.13
 *         Time: 13:15
 */

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.intertrust.cm.core.business.api.dto.AttachmentUploadPercentage;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.global.AttachmentUploadTempStorageConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 12.12.13
 *         Time: 13:15
 */
@Controller
public class AttachmentUploaderServlet {
    private static final Logger log = Logger.getLogger(FileUpload.class.getName());
    private static final String SESSION_ATTRIBUTE_UPLOAD_PROGRESS = "uploadProgress";
    private static final String SESSION_ATTRIBUTE_UPLOAD_IS_CANCELED = "uploadIsCanceled";
    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @ResponseBody
    @RequestMapping(value = "/attachment-upload")
    public ResponseEntity<String> upload(HttpServletRequest req, HttpSession session)
            throws IOException, ServletException, FileUploadException {
        //clean percentage of uploaded file
        zeroizePreviousUploadProgress(session);
        req.setCharacterEncoding("UTF-8");
        String savedFilename = null;
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
        AttachmentUploadPercentage uploadProgress = getUploadProgress(session);
        upload.setProgressListener(new AttachmentUploadProgressListener(uploadProgress));
        // set limit of uploaded file, -1 mean no limit
        upload.setSizeMax(-1);
        // Parse the request to get file items.
        List fileItems = upload.parseRequest(req);
        // Process the uploaded file items
        Iterator iterator = fileItems.iterator();
        while (iterator.hasNext()) {
            FileItem item = (FileItem) iterator.next();

            if (!item.isFormField()) {

                log.info("Got an uploaded file: " + item.getFieldName() +
                        ", name = " + item.getName());

                String filename = FilenameUtils.getName(((DiskFileItem) item).getName());
                long time = System.nanoTime();
                savedFilename = time + "-_-" + filename;
                GlobalSettingsConfig globalSettingsConfig = configurationExplorer.getGlobalSettings();
                AttachmentUploadTempStorageConfig attachmentUploadTempStorageConfig = globalSettingsConfig.
                        getAttachmentUploadTempStorageConfig();
                String pathForTempFilesStore = attachmentUploadTempStorageConfig.getPath();
                String pathToSave = pathForTempFilesStore + savedFilename;
                try (
                        InputStream inputStream = item.getInputStream();
                        OutputStream outputStream = new FileOutputStream(pathToSave)) {
                    stream(inputStream, outputStream, session);
                }

            }
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/html; charset=utf-8");
        return new ResponseEntity<String>(savedFilename, headers, HttpStatus.OK);
    }


    private void stream(InputStream input, OutputStream output, HttpSession session)
            throws IOException {

        try (
                ReadableByteChannel inputChannel = Channels.newChannel(input);
                WritableByteChannel outputChannel = Channels.newChannel(output)) {
            ByteBuffer buffer = ByteBuffer.allocate(10240);
            while (inputChannel.read(buffer) != -1) {
                Object isUploadCanceled = session.getAttribute(SESSION_ATTRIBUTE_UPLOAD_IS_CANCELED);
                if (isUploadCanceled != null && (Boolean) isUploadCanceled) {
                    session.setAttribute(SESSION_ATTRIBUTE_UPLOAD_IS_CANCELED, false);
                    AttachmentUploadPercentage uploadProgress = new AttachmentUploadPercentage();
                    session.setAttribute(SESSION_ATTRIBUTE_UPLOAD_PROGRESS, uploadProgress);
                    return;
                }
                buffer.flip();
                outputChannel.write(buffer);
                buffer.clear();

            }

        }
    }

    public static AttachmentUploadPercentage getUploadProgress(HttpSession session) {
        Object attribute = session.getAttribute(SESSION_ATTRIBUTE_UPLOAD_PROGRESS);
        if (null == attribute) {
            attribute = new AttachmentUploadPercentage();
            session.setAttribute(SESSION_ATTRIBUTE_UPLOAD_PROGRESS, attribute);
        }

        return (AttachmentUploadPercentage) attribute;
    }

    private void zeroizePreviousUploadProgress(HttpSession session) {
        getUploadProgress(session).setPercentage(0);

    }
}