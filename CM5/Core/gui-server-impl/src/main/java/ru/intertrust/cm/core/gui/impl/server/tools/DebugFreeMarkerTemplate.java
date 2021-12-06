package ru.intertrust.cm.core.gui.impl.server.tools;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import ru.intertrust.cm.core.business.api.NotificationTextFormer;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "DebugFreeMarkerTemplate", urlPatterns = { "/remote/service/freemarker" }, asyncSupported = true)
public class DebugFreeMarkerTemplate extends HttpServlet {

    @Autowired
    private NotificationTextFormer notificationTextFormer;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    protected void execute(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        String template = req.getParameter("template") == null ? "" : req.getParameter("template");
        String context = req.getParameter("context") == null ? "" : req.getParameter("context");
        PrintWriter out = resp.getWriter();

        out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        out.print("<html>");
        out.print("<head><title>");
        out.print("Проверка FreeMarker");
        out.print("</title>");
        out.print("<meta charset='utf-8'>");
        out.print("<body>");
        out.print("<form method='post'>");
        out.print("<table>");
        out.print("<tr>");
        out.print("<td>ID контекста</td><td><input type='text' name='context' value='" + context + "'/></td>");
        out.print("</tr>");
        out.print("<tr>");
        out.print("<td>Шаблон</td><td><textarea name='template' cols=200 rows=10>" + template + "</textarea></td>");
        out.print("</tr>");
        out.print("<tr>");
        out.print("<td><input type='submit' value='Выполнить'/></td>");
        out.print("</tr>");
        out.print("</table>");
        out.print("</form>");
        out.print("<div id='result'><pre>");
        if (!template.isEmpty() && !context.isEmpty()) {
            out.print(format(template, context));
        }
        out.print("</pre></div>");
        out.print("</body>");
        out.print("</html>");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        execute(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        execute(req, resp);
    }

    private String format(String template, String contextId){
        try {
            NotificationContext context = new NotificationContext();
            context.addContextObject("ctx", new RdbmsId(contextId));
            return notificationTextFormer.formatTemplate(template, null, context);
        }catch(Exception ex){
            return ExceptionUtils.getStackTrace(ex);
        }
    }

}

