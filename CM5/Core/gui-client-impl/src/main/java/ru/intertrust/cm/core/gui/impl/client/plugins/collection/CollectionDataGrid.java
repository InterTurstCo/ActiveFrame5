package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 05/03/15
 *         Time: 12:05 PM
 */
public class CollectionDataGrid extends DataGrid<CollectionRowItem>{
    private HeaderPanel panel;
    private EventBus eventBus;
    public CollectionDataGrid(int pageNumber, Resources resources, EventBus eventBus) {
        super(pageNumber, resources);
        this.eventBus = eventBus;
        panel = (HeaderPanel) getWidget();
        panel.getHeaderWidget().addStyleName("dataGridHeaderRow");
        setAutoHeaderRefreshDisabled(true);
        setHeaderBuilder(new HeaderBuilder<CollectionRowItem>(this, false));
        addStyleName("collection-plugin-view collection-plugin-view-container");
        this.addCellPreviewHandler(new CollectionCellPreviewHandler());
    }

    public ScrollPanel getScrollPanel() {

        return (ScrollPanel) panel.getContentWidget();
    }

    @Override
    protected boolean resetFocusOnCell() {
       return  true;
    }

    private  class CollectionCellPreviewHandler implements CellPreviewEvent.Handler<CollectionRowItem> {
    @Override
    public void onCellPreview(CellPreviewEvent<CollectionRowItem> event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        if ("click".equals(nativeEvent.getType())) {
            Id id =  event.getValue().getId();
            eventBus.fireEvent(new CollectionRowSelectedEvent(id));

        }
    }
    }
}
