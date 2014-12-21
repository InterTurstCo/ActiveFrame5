package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.user.client.ui.CheckBox;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.12.2014
 *         Time: 7:25
 */
public class HierarchyCheckBoxesWrapper {
    private CheckBox checkBox;
    private HierarchyBrowserItem item;

    public HierarchyCheckBoxesWrapper(CheckBox checkBox, HierarchyBrowserItem item) {
        this.checkBox = checkBox;
        this.item = item;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public HierarchyBrowserItem getItem(){
        return item;
    }

}
