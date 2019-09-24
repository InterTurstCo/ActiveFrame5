package ru.intertrust.cm.core.gui.impl.server.tools;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.FieldConfig;

@WebServlet(name = "ExecuteQueryServlet", urlPatterns = { "/remote/service/query" }, asyncSupported = true)
public class ExecuteQueryServlet extends HttpServlet {

    @Autowired
    private CollectionsService collectionsService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        execute(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        execute(req, resp);
    }

    private void execute(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        String query = req.getParameter("query");
        String collectionName = req.getParameter("collection");
        IdentifiableObjectCollection collection = null;
        Exception executionError = null;

        if (query != null && !query.isEmpty()) {
            try {
                collection = collectionsService.findCollectionByQuery(query);
            } catch (Exception ex) {
                executionError = ex;
            }
        }else if(collectionName != null && !collectionName.isEmpty()) {
            try {
                collection = collectionsService.findCollection(collectionName);
            } catch (Exception ex) {
                executionError = ex;
            }
        }

        PrintWriter out = resp.getWriter();

        out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        out.print("<html>");
        out.print("<head><title>");
        out.print("Выполнение запроса");
        out.print("</title>");
        out.print("<meta charset='utf-8'>");
        out.print("</head>");
        out.print("<body>");
        out.print("<form>");
        out.print("<table>");
        out.print("<tr>");
        String queryText = query == null ? "" : query;
        out.print("<td><textarea name='query' cols=200 rows=10>" + queryText + "</textarea></td>");
        out.print("</tr>");
        out.print("<tr>");
        String collectionText = collectionName == null ? "" : collectionName;
        out.print("<td><div>Имя коллекции</div><input type='text' name='collection' value='" + collectionText + "'/></td>");
        out.print("</tr>");
        out.print("<tr>");
        out.print("<td><input type='submit' value='Выполнить'/></td>");
        out.print("</tr>");
        out.print("</table>");
        out.print("</form>");

        if (collection != null) {
            out.print("<table border=1>");
            out.print("<tr>");
            for (FieldConfig fieldConfig : collection.getFieldsConfiguration()) {
                out.print("<th>");
                out.print(fieldConfig.getName() + " (" + fieldConfig.getFieldType() + ")");
                out.print("</th>");
            }
            out.print("</tr>");
            for (IdentifiableObject identifiableObject : collection) {
                out.print("<tr>");
                for (FieldConfig fieldConfig : collection.getFieldsConfiguration()) {
                    out.print("<td>");
                    out.print(identifiableObject.getValue(fieldConfig.getName()));
                    out.print("</td>");
                }
                out.print("</tr>");
            }
            out.print("</table>");
        }else if(executionError != null) {
            String errorText = ExceptionUtils.getStackTrace(executionError);
            out.print("<pre>" + errorText + "</pre>");
        }

        out.print("</body>");
        out.print("</html>");

    }

}
