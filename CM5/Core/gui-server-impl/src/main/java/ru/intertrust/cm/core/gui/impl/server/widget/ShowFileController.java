package ru.intertrust.cm.core.gui.impl.server.widget;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
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
import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.dto.ReportResult;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.action.UploadActionContext;
import ru.intertrust.cm.core.gui.model.action.UploadActionData;

@Controller
public class ShowFileController {
    public static final Logger logger = LoggerFactory.getLogger(ShowFileController.class);

    private File tempFolder;

    @Autowired
    protected ApplicationContext context;

    @PostConstruct
    public void init() throws Exception {
        File tempFile = File.createTempFile("temp", "file");
        tempFolder = tempFile.getParentFile();
    }

    @ResponseBody
    @RequestMapping(value = "view-file/{unid}/{inline}/{filename:.+}", method = RequestMethod.GET)
    public void getFile(@PathVariable("unid") String unid,
                        @PathVariable("inline") String inline,
                        @PathVariable("filename") String fileName,
                        HttpServletResponse response) throws Exception {

        OutputStream resOut = response.getOutputStream();

        if (inline.equals("true")) {
            response.setContentType(new File(fileName).toURL().openConnection().getContentType());
        } else {
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        }


        try (FileInputStream fileStram = new FileInputStream(new File(tempFolder, unid))) {
            StreamUtils.copy(fileStram, resOut);
        }
    }
}







