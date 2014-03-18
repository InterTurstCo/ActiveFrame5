package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.dto.ReportResult;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.GenerateReportActionData;

import java.util.HashMap;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 17:39
 */
@ComponentName("generate-report.action")
public class GenerateReportActionHandler extends ActionHandler {

    @Autowired
    private ReportService reportService;

    @Override
    public GenerateReportActionData executeAction(ActionContext context) {
        GenerateReportActionData result = new GenerateReportActionData();

        ReportResult reportResult = reportService.generate("all-employee", new HashMap<String, Object>());

        result.setReportBytes(reportResult.getReport());
        return result;
    }
}
