package ru.intertrust.cm.core.rest.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import javax.annotation.PostConstruct;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.ReportServiceAsync;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
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
    private ReportServiceAsync reportservice;
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
    public Id generateReport(@RequestBody GenerateReportParam param) {

        //Создаем объект очереди генерации отчета
        DomainObject queue = crudService.createDomainObject("generate_report_queue");
        queue.setString("name", param.getName());
        queue.setTimestamp("start", new Date());
        queue = crudService.save(queue);

        reportservice.generateAsync(param.getName(), param.getParams(), queue.getId(), ticketService.createTicket());

        return queue.getId();
    }

    @RequestMapping(value = "/cmd/generate", method = RequestMethod.POST)
    public Id generateReportTest() {
        return null;
    }

    @RequestMapping(value = "/report/content/{reportId}", method = RequestMethod.GET)
    public InputStreamResource getReportContent(@PathVariable(value = "reportId") String reportId) {
        try {
            IdentifiableObjectCollection col =
                    collectionsService.findCollectionByQuery("select id from report_result_attachment where name='report' and report_result = {0}",
                            Collections.singletonList(new ReferenceValue(idService.createId(reportId))));

            if (col.size() == 0) {
                throw new FatalException("Report result " + idService.createId(reportId) + " not contains report attachment");
            }
            return new InputStreamResource(RemoteInputStreamClient.wrap(attachmentService.loadAttachment(col.get(0).getId())));
        } catch (IOException ex) {
            throw new FatalException("Error get report content", ex);
        }
    }
}
