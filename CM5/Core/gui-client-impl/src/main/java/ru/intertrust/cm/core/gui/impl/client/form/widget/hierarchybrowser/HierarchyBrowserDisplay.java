package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.10.2014
 *         Time: 7:33
 */
public interface HierarchyBrowserDisplay {
    void display(List<HierarchyBrowserItem> items, boolean shouldDrawTooltipButton);
}
