package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.config.gui.ActionConfig;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

/**
 * @author Denis Mitavskiy
 *         Date: 22.09.13
 *         Time: 23:09
 */
public class SaveActionContext extends ActionContext {
    private FormState formState;
    private FormPluginState pluginState;

    /**
     * Default constructor
     */
    public SaveActionContext(){}

    public SaveActionContext(final ActionConfig actionConfig) {
        super(actionConfig);
    }

    public FormState getFormState() {
        return formState;
    }

    public void setFormState(FormState formState) {
        this.formState = formState;
    }

    public FormPluginState getPluginState() {
        return pluginState;
    }

    public void setPluginState(FormPluginState pluginState) {
        this.pluginState = pluginState;
    }
}
