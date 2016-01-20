package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.config.DefaultFormEditingStyleConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;

/**
 * @author Denis Mitavskiy
 *         Date: 25.09.13
 *         Time: 18:19
 */
public class SaveActionData extends ActionData {
    private FormPluginData formPluginData;
    private DefaultFormEditingStyleConfig defaultFormEditingStyleConfig;

    public FormPluginData getFormPluginData() {
        return formPluginData;
    }

    public void setFormPluginData(FormPluginData formPluginData) {
        this.formPluginData = formPluginData;
    }

    public DefaultFormEditingStyleConfig getDefaultFormEditingStyleConfig() {
        return defaultFormEditingStyleConfig;
    }

    public void setDefaultFormEditingStyleConfig(DefaultFormEditingStyleConfig defaultFormEditingStyleConfig) {
        this.defaultFormEditingStyleConfig = defaultFormEditingStyleConfig;
    }
}
