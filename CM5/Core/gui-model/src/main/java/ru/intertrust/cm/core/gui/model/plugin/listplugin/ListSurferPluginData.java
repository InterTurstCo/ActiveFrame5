package ru.intertrust.cm.core.gui.model.plugin.listplugin;

import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;

/**
 * Created by Ravil on 11.04.2017.
 */
public class ListSurferPluginData extends ActivePluginData {
    private ListPluginData listPluginData;
    private FormPluginData formPluginData;
    private Integer splitterPosition;
    private Integer splitterOrientation;
    public ListPluginData getListPluginData() {
        return listPluginData;
    }

    public void setListPluginData(ListPluginData listPluginData) {
        this.listPluginData = listPluginData;
    }

    public FormPluginData getFormPluginData() {
        return formPluginData;
    }

    public void setFormPluginData(FormPluginData formPluginData) {
        this.formPluginData = formPluginData;
    }

    public Integer getSplitterPosition() {
        return splitterPosition;
    }

    public void setSplitterPosition(Integer splitterPosition) {
        this.splitterPosition = splitterPosition;
    }

    public Integer getSplitterOrientation() {
        return splitterOrientation;
    }

    public void setSplitterOrientation(Integer splitterOrientation) {
        this.splitterOrientation = splitterOrientation;
    }
}
