package ru.intertrust.cm.core.gui.impl.server.cmd;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.intertrust.cm.core.business.api.dto.AttachmentUploadPercentage;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.*;
import ru.intertrust.cm.core.gui.impl.server.widget.AttachmentUploadProgressListener;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Vitaliy.Orlov on 25.06.2018.
 */
@Controller
public class PlatformCommandLineWebService {

    private static final Logger log = Logger.getLogger(PlatformCommandLineWebService.class.getName());

    @Autowired
    private ApplicationContext context;


    @ResponseBody
    @RequestMapping(value = "/execAction")
    public ResponseEntity<PlatformWebServiceResult> upload(HttpServletRequest req, HttpSession session)
            throws IOException, ServletException, FileUploadException {
        PlatformWebServiceResult result = null;
        try{
            req.setCharacterEncoding("UTF-8");
            DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
            // Parse the request to get file items.
            List fileItems = upload.parseRequest(req);
            List<FileItem> files = new ArrayList<>();
            // Process the uploaded file items
            Iterator iterator = fileItems.iterator();
            while (iterator.hasNext()) {
                FileItem item = (FileItem) iterator.next();
                if (!item.isFormField()) {
                    log.info("Got an uploaded file: " + item.getFieldName() +
                            ", name = " + item.getName());
                    files.add(item);
                }
            }

            Map<String, String[]> params = req.getParameterMap();
            String[] beanNames = params.get("beanName");

            if(beanNames == null || beanNames.length!= 1 && beanNames[0].trim().isEmpty()){
                throw new RuntimeException("Parameter [beanName] is required");
            }

            String beanName = beanNames[0];
            Object bean = context.getBean(beanName);
            if(bean == null){
                throw new RuntimeException("Can`t find bean with name : "+beanName);
            }

            if(bean instanceof PlatformWebService){
                PlatformWebService execBean = (PlatformWebService) bean;
                result = execBean.execute(files, params);
            }else{
                throw new RuntimeException("Found bean wiht name ["+beanName+"] is not instance of PlatformWebService");
            }
        }catch (Exception e){
            result = new ErrorPlatformWebServiceResult(e.getMessage());

        }
        return result instanceof ErrorPlatformWebServiceResult ? new ResponseEntity<PlatformWebServiceResult>(result, HttpStatus.INTERNAL_SERVER_ERROR) :  ResponseEntity.ok(result);
    }
}
