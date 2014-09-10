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
        StringBuilder urlBuilder = new StringBuilder();
        if (context.getBaseUrl() == null) {
            urlBuilder.append(Window.Location.getProtocol()).append(Window.Location.getHost())
                    .append(Window.Location.getPath())
                    .append(Window.Location.getQueryString());
        } else {
            urlBuilder.append(context.getBaseUrl());
        }
        if (context.getQueryString() != null && !context.getQueryString().isEmpty()) {
            urlBuilder.append(urlBuilder.indexOf("?") > 0 ? '&' : '?').append(context.getQueryString());
        }
        Window.open(urlBuilder.toString(), "_blank", "");
    }

    @Override
    public Component createNew() {
        return new OpenLinkAction();
    }
}
