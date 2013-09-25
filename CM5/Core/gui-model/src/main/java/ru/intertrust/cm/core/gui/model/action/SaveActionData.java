package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginData;

/**
 * @author Denis Mitavskiy
 *         Date: 25.09.13
 *         Time: 18:19
 */
public class SaveActionData extends ActionData {
    private SomeActivePluginData someActivePluginData;

    public SomeActivePluginData getSomeActivePluginData() {
        return someActivePluginData;
    }

    public void setSomeActivePluginData(SomeActivePluginData someActivePluginData) {
        this.someActivePluginData = someActivePluginData;
    }
}
