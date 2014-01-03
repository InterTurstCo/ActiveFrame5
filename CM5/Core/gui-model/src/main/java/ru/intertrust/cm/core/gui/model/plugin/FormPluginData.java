package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.gui.model.form.FormDisplayData;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:43
 */
public class FormPluginData extends ActivePluginData {
    private FormDisplayData formDisplayData;
    private FormPluginMode mode;

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

    public FormPluginMode getMode() {
        return mode == null ? FormPluginMode.EDITABLE : mode;
    }

    public void setMode(final FormPluginMode mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "SomeActivePluginData {" +
                "form=" + formDisplayData +
                "mode=" + getMode() +
                "actions=" + getActionContexts() +
                '}';
    }
}
