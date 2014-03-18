package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.GenerateReportActionData;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 17:09
 */

@ComponentName("generate-report.action")
public class GenerateReportAction extends SimpleServerAction {

    @Override
    public Component createNew() {
        return new GenerateReportAction();
    }

    @Override
    protected void onSuccess(ActionData actionData) {
        GenerateReportActionData reportData = (GenerateReportActionData) actionData;

        //TODO: [report-plugin] show results

        Window.alert("Report has been generated");
    }
}
