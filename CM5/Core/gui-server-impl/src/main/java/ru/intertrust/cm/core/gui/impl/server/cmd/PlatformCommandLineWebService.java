package ru.intertrust.cm.core.gui.impl.server.cmd;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.ErrorPlatformWebServiceResult;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.PlatformWebServiceResult;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Vitaliy.Orlov on 25.06.2018.
 */
@Controller
public class PlatformCommandLineWebService {

    private static final Logger log = LoggerFactory.getLogger(PlatformCommandLineWebService.class);

    @Autowired
    private PlatformCommandLineService platformCommandLineService;

    @ResponseBody
    @RequestMapping(value = "/execAction", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<PlatformWebServiceResult> upload(HttpServletRequest req, HttpSession session)
            throws IOException, ServletException, FileUploadException {
        PlatformWebServiceResult result = null;
        try{
            List<FileItem> files = new ArrayList<>();

            req.setCharacterEncoding("UTF-8");

            if (req.getContentType()!= null && req.getContentType().startsWith("multipart/form-data")) {
                DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
                // Parse the request to get file items.
                List fileItems = upload.parseRequest(req);
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
            }

            Map<String, String[]> params = req.getParameterMap();
            String[] beanNames = params.get("beanName");

            if(beanNames == null || beanNames.length!= 1 && beanNames[0].trim().isEmpty()){
                throw new RuntimeException("Parameter [beanName] is required");
            }

            String beanName = beanNames[0].trim();
            log.debug("Exec bean {}", beanName);

            result = platformCommandLineService.execute(beanName, files, params);
        }catch (Exception ex){
            log.error("Error process request", ex);
            result = new ErrorPlatformWebServiceResult(ex.getMessage());
        }
        return result instanceof ErrorPlatformWebServiceResult ? new ResponseEntity<PlatformWebServiceResult>(result, HttpStatus.INTERNAL_SERVER_ERROR) :  ResponseEntity.ok(result);
    }
}
