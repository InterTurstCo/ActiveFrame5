package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.OpenLinkActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 04.09.2014 15:30.
 */
@ComponentName(OpenLinkActionContext.COMPONENT_NAME)
public class OpenLinkAction extends Action {

    @Override
    protected void execute() {
        final OpenLinkActionContext context = getInitialContext();
        ApplicationWindow.infoAlert("Link open" + context.getOpenUrl());
    }

    @Override
    public Component createNew() {
        return new OpenLinkAction();
    }
}
