package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserState;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.11.2014
 *         Time: 18:07
 */
public class TableBrowserEditableViewHolder extends ViewHolder<TableBrowserEditableView, TableBrowserState> {
    public TableBrowserEditableViewHolder(TableBrowserEditableView widget) {
        super(widget);
    }

    @Override
    public void setContent(TableBrowserState state) {
          childViewHolder.setContent(state);
    }
}
