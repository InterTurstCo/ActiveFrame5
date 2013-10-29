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

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */
@Controller
public class AttachmentUploader implements ServletConfigAware{
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
            OutputStream outputStream = new FileOutputStream(pathToSave); )
        {
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

        } catch (IOException e) {
            log.error("Error while uploading: " + e);
            return "";
        }
         return savedFileName;
    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }
}