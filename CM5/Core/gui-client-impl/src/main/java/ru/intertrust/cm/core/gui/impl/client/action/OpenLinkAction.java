package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.core.client.GWT;
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
            String url = context.getBaseUrl();
            if (url.indexOf("://") < 0) {
                urlBuilder.append(Window.Location.getProtocol());
            }
            url = url.replace("{host}", Window.Location.getHostName());
            url = url.replace("{port}", Window.Location.getPort());
            url = url.replace("{context-root}", getContextRoot());
            urlBuilder.append(url);
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

    private String getContextRoot() {
        final String pageBaseUrl = GWT.getHostPageBaseURL();
        int index = pageBaseUrl.indexOf(Window.Location.getHost()) + Window.Location.getHost().length() + 1;
        return pageBaseUrl.substring(index, pageBaseUrl.length() - 1);
    }
}
