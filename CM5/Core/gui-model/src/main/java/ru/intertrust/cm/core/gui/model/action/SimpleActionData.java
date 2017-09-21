package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;

/**
 * @author Sergey.Okolot
 *         Created on 02.10.2014 17:52.
 */
public class SimpleActionData extends ActionData {

    private FormPluginData pluginData;
    private boolean contextSaved;
    private Id savedMainObjectId;
    private String urlToOpen;

    public FormPluginData getPluginData() {
        return pluginData;
    }

    public void setPluginData(FormPluginData pluginData) {
        this.pluginData = pluginData;
    }

    public boolean isContextSaved() {
        return contextSaved;
    }

    public void setContextSaved(boolean contextSaved) {
        this.contextSaved = contextSaved;
    }

    public Id getSavedMainObjectId() {
        return savedMainObjectId;
    }

    public void setSavedMainObjectId(Id savedMainObjectId) {
        this.savedMainObjectId = savedMainObjectId;
    }

    public String getUrlToOpen() {
        return urlToOpen;
    }

    public void setUrlToOpen(String urlToOpen) {
        this.urlToOpen = urlToOpen;
    }
}
