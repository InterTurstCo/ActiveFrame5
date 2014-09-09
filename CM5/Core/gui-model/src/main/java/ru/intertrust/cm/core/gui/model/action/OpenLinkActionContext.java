package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;

/**
 * @author Sergey.Okolot
 *         Created on 05.09.2014 12:07.
 */
public class OpenLinkActionContext extends ActionContext {
    public static final String COMPONENT_NAME = "open.link.action";

    private String baseUrl;
    private String queryString;

    public OpenLinkActionContext() {
    }

    public OpenLinkActionContext(AbstractActionConfig actionConfig) {
        super(actionConfig);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public void setActionConfig(AbstractActionConfig actionConfig) {
        super.setActionConfig(actionConfig);

    }
}
