package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 05/03/15
 *         Time: 12:05 PM
 */
public class CollectionDataGrid extends DataGrid<CollectionRowItem> {
    private HeaderPanel panel;

    public CollectionDataGrid(int pageNumber, Resources resources) {
        super(pageNumber, resources);
        panel = (HeaderPanel) getWidget();
        panel.getHeaderWidget().addStyleName("dataGridHeaderRow");
    }

    public ScrollPanel getScrollPanel() {

        return (ScrollPanel) panel.getContentWidget();
    }

}
