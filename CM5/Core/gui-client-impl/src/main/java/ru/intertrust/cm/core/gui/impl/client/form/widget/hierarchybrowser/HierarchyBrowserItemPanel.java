package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.user.client.ui.AbsolutePanel;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.01.2015
 *         Time: 8:42
 */
public class HierarchyBrowserItemPanel extends AbsolutePanel {
    private HierarchyBrowserItem item;

    public HierarchyBrowserItemPanel(HierarchyBrowserItem item) {
        this.item = item;
    }

    public HierarchyBrowserItem getItem() {
        return item;
    }
}
