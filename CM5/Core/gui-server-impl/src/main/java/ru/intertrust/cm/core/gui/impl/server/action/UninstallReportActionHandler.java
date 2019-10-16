package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;


/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 20.09.2016
 * Time: 14:34
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("uninstall.report.action")
public class UninstallReportActionHandler extends ActionHandler {

    private static final String REPORT_NAME = "name";
    @Autowired
    ReportServiceAdmin reportServiceAdmin;

    @Override
    public SimpleActionData executeAction(ActionContext context) {
        Id rootObject = context.getRootObjectId();
        String reportName = crudService.find(rootObject).getString(REPORT_NAME);
        reportServiceAdmin.undeploy(reportName);
        SimpleActionData aData = new SimpleActionData();
        aData.setSavedMainObjectId(rootObject);
        return aData;
    }

    @Override
    public ActionContext getActionContext(ActionConfig actionConfig) {
        return new SaveActionContext(actionConfig);
    }

}
