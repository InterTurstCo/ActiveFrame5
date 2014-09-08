package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;

import ru.intertrust.cm.core.gui.api.client.Component;
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
        Window.open(context.getOpenUrl(), "_blank", "");
    }

    @Override
    public Component createNew() {
        return new OpenLinkAction();
    }
}
