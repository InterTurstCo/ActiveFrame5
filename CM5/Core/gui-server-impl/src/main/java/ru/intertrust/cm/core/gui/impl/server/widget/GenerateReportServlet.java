package ru.intertrust.cm.core.gui.impl.server.widget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.dto.ReportResult;
import ru.intertrust.cm.core.config.CollectionViewLogicalValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Lesia Puhova
 *         Date: 19.03.14
 *         Time: 12:41
 */
@Controller
public class GenerateReportServlet {

    @Autowired
    private ReportService reportService;

    final static Logger logger = LoggerFactory.getLogger(GenerateReportServlet.class);

    @ResponseBody
    @RequestMapping(value = "generate-report", method = RequestMethod.GET)
    public void generateReport(HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {

        String reportName = request.getParameter("report_name");
        OutputStream resOut = response.getOutputStream();
        OutputStream bufferOut = new BufferedOutputStream(resOut);
        try {
            response.setHeader("Content-Disposition", "attachment; filename=" + reportName);
            ReportResult reportResult = reportService.generate(reportName, new HashMap<String, Object>());
            bufferOut.write(reportResult.getReport());
        } catch (Exception e) {
            bufferOut.write("Ошибка при генерации отчета ".getBytes());
            bufferOut.write(reportName.getBytes());
            bufferOut.write("\r\n".getBytes());
            bufferOut.write(e.getMessage().getBytes());
            bufferOut.write(Arrays.asList(e.getStackTrace()).toString().getBytes());
            logger.error("Ошибка при генерации отчета " + reportName, e);
        }
        bufferOut.flush();
        bufferOut.close();
    }

}
