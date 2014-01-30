package ru.intertrust.cm.core.gui.impl.client.form.widget;

import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 29.01.14
 *         Time: 13:15
 */
public class TableBrowserNoneEditablePanel extends NoneEditablePanel {
    List<TableBrowserItem> tableBrowserItems;

    public List<TableBrowserItem> getTableBrowserItems() {
        return tableBrowserItems;
    }

    public void setTableBrowserItems(List<TableBrowserItem> tableBrowserItems) {
        this.tableBrowserItems = tableBrowserItems;
    }

    @Override
    public void showSelectedItems(String howToDisplay) {
        initDisplayStyle(howToDisplay);
        for (TableBrowserItem tableBrowserItem : tableBrowserItems) {
            String itemRepresentation = tableBrowserItem.getStringRepresentation();
            displayItem(itemRepresentation);
        }
    }
}
