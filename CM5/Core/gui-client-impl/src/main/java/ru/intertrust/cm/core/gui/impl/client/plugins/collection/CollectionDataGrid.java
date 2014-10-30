package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 05/03/15
 *         Time: 12:05 PM
 */
public class CollectionDataGrid extends DataGrid<CollectionRowItem>{
    private HeaderPanel panel;

    public CollectionDataGrid(int pageNumber, Resources resources) {
        super(pageNumber, resources);

        panel = (HeaderPanel) getWidget();
        panel.getHeaderWidget().getElement().getFirstChildElement().setClassName("dataGridHeaderRow");
        setAutoHeaderRefreshDisabled(false);
        setHeaderBuilder(new HeaderBuilder<CollectionRowItem>(this, false));
        addStyleName("collection-plugin-view collection-plugin-view-container");
        setEmptyTableMessage();
    }

    public ScrollPanel getScrollPanel() {
        return (ScrollPanel) panel.getContentWidget();
    }

    @Override
    protected boolean resetFocusOnCell() {
       return  true;
    }

    private void setEmptyTableMessage() {
        String emptyTableText = "Результаты отсутствуют";
        HTML emptyTableWidget = new HTML("<br/><div align='center'> <h1> " + emptyTableText + " </h1> </div>");
        this.setEmptyTableWidget(emptyTableWidget);

    }
}
