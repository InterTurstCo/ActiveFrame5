package ru.intertrust.cm.core.gui.impl.server.widget;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest;
import ru.intertrust.cm.core.business.api.dto.AttachmentUploadPercentage;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.File;
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
    private static final int BUFFER_SIZE = 1024*64;
    public static final String FILE_NAME_PARAMS_DELIMITER = "-_-";

    private String pathForTempFilesStore;

    @Autowired
    private PropertyResolver propertyResolver;

    @PostConstruct
    public void init() {
        pathForTempFilesStore = propertyResolver.resolvePlaceholders("${attachment.temp.storage}");
    }

    @ResponseBody
    @RequestMapping(value = "/attachment-upload", method = RequestMethod.POST)
    public ResponseEntity<String> upload(HttpServletRequest req, HttpSession session)
            throws IOException, ServletException, FileUploadException {
        //clean percentage of uploaded file
        zeroizePreviousUploadProgress(session);
        req.setCharacterEncoding("UTF-8");
        String savedFilename = null;
        String savedFileNames = "";

        AbstractMultipartHttpServletRequest multipartRequest = (AbstractMultipartHttpServletRequest)req;
        Iterator<String> fileNameIterator = multipartRequest.getFileNames();
        while (fileNameIterator.hasNext()){
            String name = fileNameIterator.next();
            List<MultipartFile> files = ((AbstractMultipartHttpServletRequest)req).getFiles(name);

            //upload.setProgressListener(new AttachmentUploadProgressListener(uploadProgress));
            for (MultipartFile item : files) {
                log.info("Got an uploaded file: " + item.getOriginalFilename() +
                        ", name = " + item.getName());

                String filename = FilenameUtils.getName(item.getOriginalFilename());
                long time = System.nanoTime();
                final long fileSize = item.getSize();

                savedFilename = time + FILE_NAME_PARAMS_DELIMITER + filename + FILE_NAME_PARAMS_DELIMITER + fileSize;
                File fileToSave = new File(pathForTempFilesStore, savedFilename);
                try (
                        InputStream inputStream = item.getInputStream();
                        OutputStream outputStream = new FileOutputStream(fileToSave)) {
                    stream(inputStream, outputStream);
                }
                savedFileNames = savedFileNames + savedFilename + "*";
            }

        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/html; charset=utf-8");
        return new ResponseEntity<String>(savedFileNames, headers, HttpStatus.OK);
    }

    private void stream(InputStream input, OutputStream output)
            throws IOException {

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

    public static AttachmentUploadPercentage getUploadProgress(HttpSession session) {
        AttachmentUploadPercentage attribute = (AttachmentUploadPercentage)session.getAttribute(SESSION_ATTRIBUTE_UPLOAD_PROGRESS);
        if (attribute == null) {
            attribute = new AttachmentUploadPercentage();
            session.setAttribute(SESSION_ATTRIBUTE_UPLOAD_PROGRESS, attribute);
        }

        return attribute;
    }

    private void zeroizePreviousUploadProgress(HttpSession session) {
        getUploadProgress(session).setPercentage(0);

    }

}
