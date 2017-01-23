package ru.intertrust.cm.core.gui.impl.server.widget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.healthmarketscience.rmiio.RemoteInputStreamClient;

import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.dto.ReportResult;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.util.ValueUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 19.03.14
 *         Time: 12:41
 */
@Controller
public class GenerateReportServlet {

    private static final String REPORT_NAME = "report_name";
    private static final String SEPARATOR = "~";
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
        InputStream reportStream = null;
        try {
            Map<String, Object> params = convertParameters(request.getParameterMap());

            ReportResult reportResult = reportService.generate(reportName, params, DataSourceContext.CLONE);
            response.setHeader("Content-Disposition", "attachment; filename=" + reportResult.getFileName());
            
            reportStream = RemoteInputStreamClient.wrap(reportResult.getReport());

            StreamUtils.copy(reportStream, bufferOut);
        } catch (Exception e) {
            logger.error("Ошибка при генерации отчета " + reportName, e);
            response.setHeader("Content-Disposition", "attachment; filename=REPORT_GENERATION_ERROR_" + reportName);
            bufferOut.write("Ошибка при генерации отчета ".getBytes());
            bufferOut.write(reportName.getBytes());
            bufferOut.write("\r\n".getBytes());
            bufferOut.write(e.getMessage().getBytes());
            if (e.getCause().getStackTrace() != null && e.getCause().getStackTrace().length > 0) {
                bufferOut.write(Arrays.asList(e.getCause().getStackTrace()).toString().getBytes());
            }
        }
        bufferOut.flush();
        bufferOut.close();
    }

    private Map<String, Object> convertParameters(Map<String, String[]> requestParams) {
        Map<String, Object> reportParams = new HashMap<>();
        for (String paramName : requestParams.keySet()) {
            if (REPORT_NAME.equals(paramName)) {
                continue;
            }
            String requestParamValue = (requestParams.get(paramName) != null && requestParams.get(paramName).length > 0)
                    ? requestParams.get(paramName)[0] : null;
            if (requestParamValue != null && !requestParamValue.isEmpty()) {
                Object value =  getParamValue(requestParamValue);
                if (value != null) {
                    reportParams.put(paramName, value);
                }
            }
        }
        return reportParams;
    }

    private Object getParamValue(String requestParamValue) {
        if (requestParamValue.contains(SEPARATOR)) {
            String[] parts = requestParamValue.split(SEPARATOR);
            String paramValue = parts[0];
            String paramType = parts[1];
            if (paramValue != null && !"null".equals(paramValue)) {
                Value value = ValueUtil.stringValueToObject(paramValue, paramType);
                return value.get();
            } else if ("LIST".equals(paramType)) {
                return new ArrayList<Serializable>();
            }
        }
        return null;
    }
}
