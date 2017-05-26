package ru.intertrust.cm.core.gui.impl.client.action.configextension;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FormPanel;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.action.SimpleServerAction;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;

/**
 * Created by Ravil on 26.05.2017.
 */
@ComponentName("download.config.action.handler")
public class DownloadConfigAction  extends SimpleServerAction {

    private static final String METHOD = "POST";
    private static final String URL = "configuration-export-to-file";
    private FormPanel submitForm;


    @Override
    protected SimpleActionContext appendCurrentContext(ActionContext initialContext) {
        return new SimpleActionContext();
    }

    @Override
    protected void execute() {
        submitForm = new FormPanel();
        submitForm.setVisible(false);
        PluginPanel p = getPlugin().getOwner();
        p.asWidget().getElement().appendChild(submitForm.getElement());
        submitForm.setAction(GWT.getHostPageBaseURL() + URL);
        submitForm.setMethod(METHOD);
        submitForm.submit();
    }

    @Override
    public Component createNew() {
        return new DownloadConfigAction();
    }
}
