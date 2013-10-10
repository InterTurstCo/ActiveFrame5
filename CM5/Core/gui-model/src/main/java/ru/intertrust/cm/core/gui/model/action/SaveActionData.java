package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;

/**
 * @author Denis Mitavskiy
 *         Date: 25.09.13
 *         Time: 18:19
 */
public class SaveActionData extends ActionData {
    private FormPluginData formPluginData;

    public FormPluginData getFormPluginData() {
        return formPluginData;
    }

    public void setFormPluginData(FormPluginData formPluginData) {
        this.formPluginData = formPluginData;
    }
}
