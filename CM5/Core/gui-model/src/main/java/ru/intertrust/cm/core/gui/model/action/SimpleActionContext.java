package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

/**
 * @author Sergey.Okolot
 *         Created on 02.10.2014 17:41.
 */
public class SimpleActionContext extends ActionContext {
    public static final String COMPONENT_NAME = "simple.action";

    private FormState mainFormState;
    private FormViewerConfig viewerConfig;
    private FormPluginState pluginState;
    private Id confirmDomainObjectId;
    private boolean saved;
    private String collectionName;

    public SimpleActionContext() {
    }

    public SimpleActionContext(AbstractActionConfig actionConfig) {
        super(actionConfig);
    }

    public FormState getMainFormState() {
        return mainFormState;
    }

    public void setMainFormState(FormState mainFormState) {
        this.mainFormState = mainFormState;
    }

    public FormViewerConfig getViewerConfig() {
        return viewerConfig;
    }

    public void setViewerConfig(FormViewerConfig viewerConfig) {
        this.viewerConfig = viewerConfig;
    }

    public FormPluginState getPluginState() {
        return pluginState;
    }

    public void setPluginState(FormPluginState pluginState) {
        this.pluginState = pluginState;
    }

    public Id getConfirmDomainObjectId() {
        return confirmDomainObjectId;
    }

    public void setConfirmDomainObjectId(Id confirmDomainObjectId) {
        this.confirmDomainObjectId = confirmDomainObjectId;
    }

    public boolean isContextSaved() {
        return saved;
    }

    public void setContextSaved() {
        this.saved = true;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}
