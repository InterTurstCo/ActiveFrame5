package ru.intertrust.cm.core.rest.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.api.TicketService;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.rest.api.GenerateReportParam;

@RestController
public class ReportRestService {
    private static final Logger logger = LoggerFactory.getLogger(ReportRestService.class);

    @PostConstruct
    public void init() {
        logger.info("Init ReportRestService controller");
    }

    @Autowired
    private ReportService reportservice;
    @Autowired
    private CrudService crudService;
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private IdService idService;
    @Autowired
    private CollectionsService collectionsService;
    @Autowired
    private TicketService ticketService;

    @RequestMapping(value = "/report/generate", method = RequestMethod.POST)
    public Id generateReport(
            @RequestBody(required = true) GenerateReportParam param) {

        //Создаем объект очереди генерации отчета
        DomainObject queue = crudService.createDomainObject("generate_report_queue");
        queue.setString("name", param.getName());
        queue.setTimestamp("start", new Date());
        queue = crudService.save(queue);
        
        reportservice.generateAsync(param.getName(), param.getParams(), queue.getId(), ticketService.createTicket());

        return queue.getId();
    }

    @RequestMapping(value = "/report/content/{reportId}", method = RequestMethod.GET)
    public InputStreamResource getReportContent(
            @PathVariable(value = "reportId") String reportId) {
        try {
            Id reportResultId = idService.createId(reportId);

            List<Value> params = new ArrayList<Value>();
            params.add(new ReferenceValue(reportResultId));
            IdentifiableObjectCollection reportResultAttachmentCollection =
                    collectionsService.findCollectionByQuery("select id from report_result_attachment where name='report' and report_result = {0}", params);

            if (reportResultAttachmentCollection.size() > 0) {
                RemoteInputStream stream = attachmentService.loadAttachment(reportResultAttachmentCollection.get(0).getId());
                InputStream reportStream;
                reportStream = RemoteInputStreamClient.wrap(stream);
                InputStreamResource result = new InputStreamResource(reportStream);
                return result;
            } else {
                throw new FatalException("Report result " + reportResultId + " not contains report attachment");
            }
        } catch (IOException ex) {
            throw new FatalException("Error get report content", ex);
        }
    }

}
