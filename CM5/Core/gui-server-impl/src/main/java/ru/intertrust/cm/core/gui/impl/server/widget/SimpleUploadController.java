package ru.intertrust.cm.core.gui.impl.server.widget;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.action.UploadActionContext;
import ru.intertrust.cm.core.gui.model.action.UploadActionData;

@Controller
public class SimpleUploadController {
    public static final Logger logger = LoggerFactory.getLogger(SimpleUploadController.class);

    @Autowired
    protected ApplicationContext context;

    @ResponseBody
    @RequestMapping(value = "simple-upload/{actionHandler}/execute", method = RequestMethod.POST)
    public String upload(@RequestParam("fileselect[]") List<MultipartFile> files, @PathVariable String actionHandler)
            throws Exception {

        try {
            Map<String, File> uploadedFiles = new HashMap<>();
            for (MultipartFile file : files) {
                File tempFile = File.createTempFile("uploaded_","tmp");

                StreamUtils.copy(file.getInputStream(), new FileOutputStream(tempFile));
                uploadedFiles.put(file.getOriginalFilename(), tempFile);
            }
            if (uploadedFiles.size() > 0) {

                // Execute action handler
                UploadActionContext uploadContext = new UploadActionContext();
                uploadContext.setUploadedFiles(uploadedFiles);

                ActionHandler<?, ?> handler = context.getBean(actionHandler, ActionHandler.class);
                handler.executeAction(uploadContext);

            }
            return "Success " + uploadedFiles.size();
        } catch(Exception ex){
            logger.error("Error execute upload action", ex);
            return "Error: " + ex.getMessage();
        }
    }
}
