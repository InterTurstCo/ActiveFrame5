package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.gui.model.form.Form;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:43
 */
public class SomeActivePluginData extends ActivePluginData {
    private Form form;

    public SomeActivePluginData() {
    }

    public SomeActivePluginData(Form form) {
        this.form = form;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    @Override
    public String toString() {
        return "SomeActivePluginData {" +
                "form=" + form +
                "actions=" + getActionConfigs() +
                '}';
    }
}
