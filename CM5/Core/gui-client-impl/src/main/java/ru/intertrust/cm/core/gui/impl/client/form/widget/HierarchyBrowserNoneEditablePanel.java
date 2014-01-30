package ru.intertrust.cm.core.gui.impl.client.form.widget;

import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 29.01.14
 *         Time: 13:15
 */
public class HierarchyBrowserNoneEditablePanel extends NoneEditablePanel {
    List<HierarchyBrowserItem> hierarchyBrowserItems;

    public List<HierarchyBrowserItem> getHierarchyBrowserItems() {
        return hierarchyBrowserItems;
    }

    public void setHierarchyBrowserItems(List<HierarchyBrowserItem> hierarchyBrowserItems) {
        this.hierarchyBrowserItems = hierarchyBrowserItems;
    }

    @Override
    public void showSelectedItems(String howToDisplay) {
        initDisplayStyle(howToDisplay);
        for (HierarchyBrowserItem hierarchyBrowserItem : hierarchyBrowserItems) {
            String itemRepresentation = hierarchyBrowserItem.getStringRepresentation();
            displayItem(itemRepresentation);
        }

    }
}
