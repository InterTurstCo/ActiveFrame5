package ru.intertrust.cm.core.gui.impl.server.widget;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.config.ConfigurationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Ravil on 26.05.2017.
 */
@Controller
public class UploadConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(UploadConfiguration.class);

    @Autowired
    protected ConfigurationControlService configurationControlService;

    @ResponseBody
    @RequestMapping(value = "upload-configuration", method = RequestMethod.POST)
    public String upload(@RequestParam("fileselect[]") MultipartFile files[])
            throws Exception {
        logger.debug("Upload config files");

        List<File> uploadedFiles = new ArrayList<>();
        // Копируем во временный каталог
        for (MultipartFile multipartFile : files) {
            File tempFile = File.createTempFile("uploaded_","tmp");
            try(FileOutputStream fileOutStream = new FileOutputStream(tempFile)) {
                logger.debug("Prepare file " + multipartFile.getOriginalFilename());
                StreamUtils.copy(multipartFile.getInputStream(), fileOutStream);
                uploadedFiles.add(tempFile);
            }
        }

        // Активируем
        try {
            if (uploadedFiles.size() > 0) {
                logger.debug("Activate upload configs");
                configurationControlService.activateFromFiles(uploadedFiles);
                return "Success";
            }else{
                return "No config files uploaded";
            }
        } catch(ConfigurationException ex){
            logger.error("Error install config", ex);
            return ex.getMessage();
        } finally{
            // Удаляем файлы
            for (File tempFile : uploadedFiles) {
                tempFile.deleteOnExit();
            }
        }
    }
}
