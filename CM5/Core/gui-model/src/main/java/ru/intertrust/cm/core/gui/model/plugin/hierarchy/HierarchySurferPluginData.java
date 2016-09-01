package ru.intertrust.cm.core.gui.model.plugin.hierarchy;

import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 25.08.2016
 * Time: 10:13
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchySurferPluginData extends ActivePluginData {
    private HierarchyPluginData hierarchyPluginData;
    private FormPluginData formPluginData;
    private Integer splitterPosition;
    private Integer splitterOrientation;
    public HierarchyPluginData getHierarchyPluginData() {
        return hierarchyPluginData;
    }

    public void setHierarchyPluginData(HierarchyPluginData hierarchyPluginData) {
        this.hierarchyPluginData = hierarchyPluginData;
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
