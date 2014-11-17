package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Lesia Puhova
 *         Date: 21.10.14
 *         Time: 18:34
 */
@Controller
public class ImagePreviewServlet {

    private static final long MAX_AGE = 60*60*24*365;
    private static final int BUFFER_SIZE = 1024*64;
    @Autowired
    private PropertyResolver propertyResolver;

    @Autowired
    private CrudService crudService;

    private String attachmentStoragePath;
    private String attachmentTempStoragePath;

    @PostConstruct
    public void init() {
        attachmentStoragePath = propertyResolver.resolvePlaceholders("${attachment.storage}");
        attachmentTempStoragePath = propertyResolver.resolvePlaceholders("${attachment.temp.storage}");
    }

    @ResponseBody
    @RequestMapping(value = "image-preview", method = RequestMethod.GET)
    public void getImagePreview(HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {

        String attachmentObjectId = request.getParameter("id");
        String path;
        String absolutePath;

        if (attachmentObjectId != null) {
            Id id = new RdbmsId(attachmentObjectId);
            DomainObject attachmentObject = crudService.find(id);
            path = attachmentObject.getString("path");
            absolutePath = attachmentStoragePath + path;
        } else {
            path = request.getParameter("tempName");
            absolutePath = attachmentTempStoragePath + path;
        }
        if (path != null) {
            response.addHeader("Cache-Control", "public, max-age=" + MAX_AGE);
            response.setHeader("Content-Length", String.valueOf(new File(absolutePath).length()));
            response.setBufferSize(BUFFER_SIZE);
            InputStream in = new FileInputStream(absolutePath);
            OutputStream out = response.getOutputStream();

            copyStream(in, out);
            in.close();
            out.close();
        }
    }

    private static void copyStream(InputStream in, OutputStream out) {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        try {
            out.flush();
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
