package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.CollectionRowMoreItemsEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.builder.PlatformCellTableBuilder;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 05/03/15
 *         Time: 12:05 PM
 */
public class CollectionDataGrid extends DataGrid<CollectionRowItem> {

    private HeaderPanel panel;
    private EventBus eventBus;
    private CollectionPlugin plugin;
    private boolean embeddedDisplayCheckBoxes;
    private boolean displayCheckBoxes;
    private Widget emptyTableWidget;

    public CollectionDataGrid(CollectionPlugin plugin, int pageNumber, Resources resources, EventBus eventBus) {
        super(pageNumber, resources);
        this.plugin = plugin;
        this.eventBus = eventBus;
        panel = (HeaderPanel) getWidget();
        panel.getHeaderWidget().getElement().getFirstChildElement().setClassName("dataGridHeaderRow");
        setAutoHeaderRefreshDisabled(false);
        setHeaderBuilder(new HeaderBuilder<>(this, false));
        setTableBuilder(new PlatformCellTableBuilder(this));

        addStyleName("collection-plugin-view-container");
        addCellPreviewHandler(new CollectionCellPreviewHandler());
        addDomHandler(new CollectionRowOnDoubleClickHandler(), DoubleClickEvent.getType());
        sinkEvents(Event.ONDBLCLICK | Event.ONCLICK | Event.KEYEVENTS | Event.FOCUSEVENTS);

    }

    public ScrollPanel getScrollPanel() {
        return (ScrollPanel) panel.getContentWidget();
    }

    public void setEmptyTableMessage(boolean displayMessage) {
        String emptyTableText = displayMessage ? LocalizeUtil.get(LocalizationKeys.EMPTY_TABLE_KEY, BusinessUniverseConstants.EMPTY_TABLE)
                : BusinessUniverseConstants.EMPTY_VALUE;
        emptyTableWidget = new HTML("<br/><div align='center'> <h1> " + emptyTableText + " </h1> </div>");
        this.setEmptyTableWidget(emptyTableWidget);

    }

    public void setEmptyTableWidgetWidth(int width) {
        emptyTableWidget.setWidth(width + com.google.gwt.dom.client.Style.Unit.PX.getType());
    }

    public void setEmbeddedDisplayCheckBoxes(boolean embeddedDisplayCheckBoxes) {
        this.embeddedDisplayCheckBoxes = embeddedDisplayCheckBoxes;
    }

    public void setDisplayCheckBoxes(boolean displayCheckBoxes) {
        this.displayCheckBoxes = displayCheckBoxes;
    }

    private class CollectionCellPreviewHandler implements CellPreviewEvent.Handler<CollectionRowItem> {

        private Id clickedItemId;

        @Override
        public void onCellPreview(final CellPreviewEvent<CollectionRowItem> event) {
            CollectionRowItem clickedItem = event.getValue();
            EventTarget eventTarget = event.getNativeEvent().getEventTarget();
            Element element = Element.as(eventTarget);
            if (cancelSelection(clickedItem, element)) {
                getSelectionModel().setSelected(clickedItem, false);
                return;
            }
            final Id id = clickedItem.getId();
            int nativeEventType = Event.getTypeInt(event.getNativeEvent().getType());
            switch (nativeEventType) {
                case Event.ONCLICK:
                    if (CollectionRowItem.RowType.BUTTON.equals(clickedItem.getRowType())) {
                        eventBus.fireEvent(new CollectionRowMoreItemsEvent(clickedItem));
                        getSelectionModel().setSelected(clickedItem, false);
                    } else {
                        handleClickEvent(id);
                    }
                    break;
                case Event.ONKEYDOWN:
                    handleKeyEvents(event);
                    break;
            }
        }

        private boolean cancelSelection(CollectionRowItem clickedItem, Element element) {
            return CollectionRowItem.RowType.FILTER.equals(clickedItem.getRowType())
                    || (displayCheckBoxes && "checkbox".equalsIgnoreCase(element.getPropertyString("type")));
        }

        private void handleKeyEvents(CellPreviewEvent<CollectionRowItem> event) {
            NativeEvent nativeEvent = event.getNativeEvent();
            int keyCode = nativeEvent.getKeyCode();
            switch (keyCode) {
                case KeyCodes.KEY_UP:
                case KeyCodes.KEY_DOWN:
                    handleKeyArrowUpAndDown();
                    break;

            }
        }

        private void handleKeyArrowUpAndDown() {
            if (embeddedDisplayCheckBoxes) {
                return;
            }
            int rowIndex = getKeyboardSelectedRow();
            CollectionRowItem item = getVisibleItem(rowIndex);
            getSelectionModel().setSelected(item, true);
            eventBus.fireEvent(new CollectionRowSelectedEvent(item.getId()));
        }

        private void handleClickEvent(final Id id) {
            if (checkDirtiness()) {
                Application.getInstance().getActionManager().checkChangesBeforeExecution(new ConfirmCallback() {
                    @Override
                    public void onAffirmative() {
                        handleClick(id);
                    }

                    @Override
                    public void onCancel() {
                        clickedItemId = null;
                    }
                });
            } else {
                handleClick(id);
            }
        }

        private void handleClick(final Id id) {
            if (id != clickedItemId) {
                performOnClickAction(id);
            }
            clickedItemId = id;
        }

        public void performOnClickAction(Id id) {
            final CollectionViewerConfig collectionViewerConfig = (CollectionViewerConfig) plugin.getConfig();
            if (collectionViewerConfig.getTableBrowserParams() == null && !collectionViewerConfig.isEmbedded()) {
                Application.getInstance().getHistoryManager().setSelectedIds(id);
            }
            eventBus.fireEvent(new CollectionRowSelectedEvent(id));
        }
    }

    private class CollectionRowOnDoubleClickHandler implements DoubleClickHandler {

        @Override
        public void onDoubleClick(DoubleClickEvent doubleClickEvent) {
            CollectionDataGrid grid = (CollectionDataGrid) doubleClickEvent.getSource();
            int rowIndex = grid.getKeyboardSelectedRow();
            CollectionRowItem doubleClickedRow = grid.getVisibleItem(rowIndex);
            if (doubleClickedRow != null) {
                final Id id = doubleClickedRow.getId();
                handleDoubleClickEvent(id);
            }
        }

        private void handleDoubleClickEvent(final Id id) {
            if (checkDirtiness()) {
                Application.getInstance().getActionManager().checkChangesBeforeExecution(new ConfirmCallback() {
                    @Override
                    public void onAffirmative() {
                        performOnDoubleClickAction(id);
                    }

                    @Override
                    public void onCancel() {
                    }
                });
            } else {
                performOnDoubleClickAction(id);
            }
        }

        private void performOnDoubleClickAction(Id id) {
            eventBus.fireEvent(new OpenDomainObjectFormEvent(id));
        }
    }

    private boolean checkDirtiness() {
        final DomainObjectSurferPlugin parentSurfer = plugin.getContainingDomainObjectSurferPlugin();
        return parentSurfer != null && !parentSurfer.getPluginState().isToggleEdit();
    }

}
