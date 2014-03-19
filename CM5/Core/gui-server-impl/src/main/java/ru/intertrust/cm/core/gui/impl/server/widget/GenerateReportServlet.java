package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.dto.ReportResult;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    @ResponseBody
    @RequestMapping(value = "generate-report", method = RequestMethod.GET)
    public void generateReport(HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {

        String reportName = request.getParameter("report_name");

        ReportResult reportResult = reportService.generate(reportName, new HashMap<String, Object>());

        response.setHeader("Content-Disposition", "attachment; filename=" + reportName);
        OutputStream resOut = response.getOutputStream();
        OutputStream bufferOut = new BufferedOutputStream(resOut);
        bufferOut.write(reportResult.getReport());

        bufferOut.flush();
        bufferOut.close();
    }

}
