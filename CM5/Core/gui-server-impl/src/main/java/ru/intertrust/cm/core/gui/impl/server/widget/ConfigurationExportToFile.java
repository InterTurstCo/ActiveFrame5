package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.config.ConfigurationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.text.ParseException;

/**
 * Created by Ravil on 26.05.2017.
 */
@Controller
public class ConfigurationExportToFile {
    private static final String DEFAULT_ENCODING = "ANSI-1251";

    @Autowired
    protected ConfigurationControlService configurationControlService;

    @ResponseBody
    @RequestMapping(value = "configuration-export-to-file", method = RequestMethod.POST)
    public void generateCsv(HttpServletResponse response)
            throws IOException, ParseException, ServletException {
        response.setHeader("Content-Disposition", "attachment; filename=configurations.xml");
        response.setContentType("application/xml");
        response.addHeader("Access-Control-Allow-Origin", "");

        File confTemporary = File.createTempFile("expconfig", ".xml");
        OutputStream resOut = response.getOutputStream();
        OutputStream buffer = new BufferedOutputStream(resOut);
        OutputStreamWriter writer = new OutputStreamWriter(buffer, Charset.forName(DEFAULT_ENCODING));
        try {
            configurationControlService.exportActiveExtensions(confTemporary);
            FileInputStream fis = new FileInputStream(confTemporary);
            BufferedReader br =
                    new BufferedReader( new InputStreamReader(fis, DEFAULT_ENCODING ));
            StringBuilder sb = new StringBuilder();
            String line;
            while(( line = br.readLine()) != null ) {
                sb.append( line );
                sb.append( '\n' );
            }
            writer.append(sb.toString());
            writer.close();
        } catch(ConfigurationException e){
            writer.append(e.getMessage());
            writer.close();
        }

    }
}
