package ru.intertrust.cm.core.gui.model.action.system;

import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 06.08.2014 16:43.
 */
public class ResetPluginSettingsActionContext extends ActionContext {

    public static final String COMPONENT_NAME = "reset.plugin.settings.action";

    private String link;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
