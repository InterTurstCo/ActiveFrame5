package ru.intertrust.cm.core.gui.impl.server.widget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletConfig;
import java.io.FileOutputStream;
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
public class AttachmentUploader implements ServletConfigAware {
    private static Logger log = LoggerFactory.getLogger(AttachmentUploader.class);
    private String pathForTempFilesStore;
    private ServletConfig servletConfig;

    @ResponseBody
    @RequestMapping("/attachment-upload")
    public String upload(@RequestParam(value = "fileUpload") MultipartFile file) {

        String fileName = file.getOriginalFilename();
        long time = System.nanoTime();
        String pathToSave = "";
        String savedFileName = fileName + "-_-" + time;
        pathForTempFilesStore = servletConfig.getInitParameter("pathForTempFilesStore");
        pathToSave = pathForTempFilesStore + savedFileName;
        try (
            InputStream inputStream = file.getInputStream();
            OutputStream outputStream = new FileOutputStream(pathToSave);) {
            stream(inputStream, outputStream);
        } catch (IOException e) {
            log.error("Error while uploading: " + e);
            return "";
        }
        return savedFileName;
    }

    private void stream(InputStream input, OutputStream output) throws IOException {

        try (
            ReadableByteChannel inputChannel = Channels.newChannel(input);
            WritableByteChannel outputChannel = Channels.newChannel(output);) {
            ByteBuffer buffer = ByteBuffer.allocate(10240);
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.clear();
            }
        }

    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }
}