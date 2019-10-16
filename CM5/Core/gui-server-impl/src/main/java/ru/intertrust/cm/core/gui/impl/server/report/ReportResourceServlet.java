package ru.intertrust.cm.core.gui.impl.server.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.service.api.ReportTemplateCache;

@WebServlet(name = "ReportResourceServlet", urlPatterns = { "/remote/report/resource/*" }, asyncSupported = true)
public class ReportResourceServlet extends HttpServlet{
    private static final long serialVersionUID = -8266715903807003342L;

    @Autowired
    protected ReportTemplateCache templateCache;
    
    @Autowired
    protected CrudService crudService;

    @Autowired
    protected CollectionsService collectionsService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }    
    
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

        Pattern pattern = Pattern.compile("/([^/]+)/([^/]+)");
        Matcher matchet = pattern.matcher(req.getPathInfo());
        String report = null;
        String resource = null;
        if (matchet.find()){
            report = matchet.group(1);
            resource = matchet.group(2);
        }else{
            resp.sendError(500, "Report resource url is not correct");
            return;
        }
        
        
        DomainObject reportDomainObject = getReportTemplateObject(report);
        
        File templateFolder = templateCache.getTemplateFolder(reportDomainObject);

        File resourceFile = new File(templateFolder, resource);
        if (!resourceFile.exists()){
            resp.sendError(404, "Report resource " + resource + " not found");
            return;
        }
        
        resp.setContentType(resourceFile.toURL().openConnection().getContentType());
        resp.setContentLength(new Long(resourceFile.length()).intValue());
        try(FileInputStream fs = new FileInputStream(resourceFile)) {
            StreamUtils.copy(fs, resp.getOutputStream());
        }
        
        resp.flushBuffer();
    }

    /**
     * Получение доменного объекта отчета по имени
     * @param name
     * @return
     */
    protected DomainObject getReportTemplateObject(String name) {

        String query = "select t.id from report_template t where t.name = '" + name + "'";
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(query);
        DomainObject result = null;
        if (collection.size() > 0) {
            IdentifiableObject row = collection.get(0);
            result = crudService.find(row.getId());
        }
        return result;
    }
}
