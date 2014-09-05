package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;

/**
 * @author Sergey.Okolot
 *         Created on 05.09.2014 12:07.
 */
public class OpenLinkActionContext extends ActionContext {
    public static final String COMPONENT_NAME = "open.link.action";

    private String openUrl;

    public OpenLinkActionContext() {
    }

    public OpenLinkActionContext(AbstractActionConfig actionConfig) {
        super(actionConfig);
    }

    public String getOpenUrl() {
        return openUrl;
    }

    public void setOpenUrl(String openUrl) {
        this.openUrl = openUrl;
    }

    @Override
    public void setActionConfig(AbstractActionConfig actionConfig) {
        super.setActionConfig(actionConfig);

    }
}
