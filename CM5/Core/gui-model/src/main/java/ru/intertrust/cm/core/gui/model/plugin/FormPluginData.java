package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.gui.model.form.FormDisplayData;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:43
 */
public class FormPluginData extends ActivePluginData {
    private FormDisplayData formDisplayData;

    public FormPluginData() {
    }

    public FormPluginData(FormDisplayData formDisplayData) {
        this.formDisplayData = formDisplayData;
    }

    public FormDisplayData getFormDisplayData() {
        return formDisplayData;
    }

    public void setFormDisplayData(FormDisplayData formDisplayData) {
        this.formDisplayData = formDisplayData;
    }
}
