package ru.intertrust.cm.core.gui.impl.server.widget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.model.global.AttachmentUploadTempStorageConfig;

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
public class AttachmentUploader {
    private static Logger log = LoggerFactory.getLogger(AttachmentUploader.class);

    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @ResponseBody
    @RequestMapping("/attachment-upload")
    public String upload(@RequestParam(value = "fileUpload") MultipartFile file) {

        String fileName = file.getOriginalFilename();
        long time = System.nanoTime();
        String pathToSave = "";
        String savedFileName = time + "-_-" + fileName;
        GlobalSettingsConfig globalSettingsConfig = configurationExplorer.getGlobalSettings();
        AttachmentUploadTempStorageConfig attachmentUploadTempStorageConfig= globalSettingsConfig.
                getAttachmentUploadTempStorageConfig();
        String pathForTempFilesStore = attachmentUploadTempStorageConfig.getPath();
        pathToSave = pathForTempFilesStore + savedFileName;
        try (
            InputStream inputStream = file.getInputStream();
            OutputStream outputStream = new FileOutputStream(pathToSave)) {
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
            WritableByteChannel outputChannel = Channels.newChannel(output)) {
            ByteBuffer buffer = ByteBuffer.allocate(10240);
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.clear();
            }
        }

    }

}