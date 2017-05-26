package ru.intertrust.cm.core.gui.impl.server.widget;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
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

    private static final String DEFAULT_ENCODING = "UTF-8";

    @Autowired
    protected ConfigurationControlService configurationControlService;

    @ResponseBody
    @RequestMapping(value = "upload-configuration", method = RequestMethod.POST)
    public void upload(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        OutputStream resOut = response.getOutputStream();
        OutputStream buffer = new BufferedOutputStream(resOut);
        OutputStreamWriter writer = new OutputStreamWriter(buffer, Charset.forName(DEFAULT_ENCODING));
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
        List<FileItem> fileItems = upload.parseRequest(request);
        List<File> uploadedFiles = new ArrayList<>();
        Iterator iterator = fileItems.iterator();
        while (iterator.hasNext()) {
            File fT = File.createTempFile("uploaded_","tmp");
            FileItem item = (FileItem) iterator.next();
            item.write(fT);
            uploadedFiles.add(fT);
        }
        try {
            if (uploadedFiles.size() > 0) {
                configurationControlService.activateFromFiles(uploadedFiles);
                writer.append("Success");
                writer.close();
            }
        } catch(ConfigurationException e){
            writer.append(e.getMessage());
            writer.close();
        }
    }
}
