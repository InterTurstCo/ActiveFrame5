package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserState;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 20.11.2014
 *         Time: 8:49
 */
public class TableBrowserCollectionViewHolder extends ViewHolder<TableBrowserCollection, TableBrowserState> {

    public TableBrowserCollectionViewHolder(TableBrowserCollection widget) {
        super(widget);
    }

    @Override
    public void setContent(TableBrowserState state) {
       getWidget().refresh();

    }

}
