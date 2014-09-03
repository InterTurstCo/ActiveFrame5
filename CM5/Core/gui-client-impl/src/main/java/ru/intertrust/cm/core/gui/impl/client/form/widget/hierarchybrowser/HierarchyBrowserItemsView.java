package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserCheckBoxUpdateEvent;
import ru.intertrust.cm.core.gui.impl.client.util.DisplayStyleBuilder;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 20.12.13
 *         Time: 13:15
 */
public class HierarchyBrowserItemsView extends Composite {
    private AbsolutePanel container;
    private AbsolutePanel mainBoxPanel;
    private EventBus eventBus;
    private ArrayList<HierarchyBrowserItem> chosenItems = new ArrayList<HierarchyBrowserItem>();
    private ArrayList<Id> selectedIds;
    private Style.Display displayStyle;
    private PopupPanel popupPanel;
    private boolean displayAsHyperlinks;

    public HierarchyBrowserItemsView(SelectionStyleConfig selectionStyleConfig, EventBus eventBus, boolean displayAsHyperlink) {
        this.eventBus = eventBus;
        this.displayAsHyperlinks = displayAsHyperlink;

        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("hierarchyBrowserMainBox");
        displayStyle = DisplayStyleBuilder.getDisplayStyle(selectionStyleConfig);
        mainBoxPanel.getElement().getStyle().clearOverflow();

        container = new AbsolutePanel();
        container.getElement().getStyle().clearOverflow();
        container.add(mainBoxPanel);
        initWidget(container);
    }

    @Override
    public Widget asWidget() {
        return container;
    }

    public ArrayList<Id> getSelectedIds() {
        return selectedIds;
    }

    public ArrayList<HierarchyBrowserItem> getChosenItems() {
        return chosenItems;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setPopupPanel(PopupPanel popupPanel) {
        this.popupPanel = popupPanel;
    }

    private void displayChosenItem(final HierarchyBrowserItem item) {

        final AbsolutePanel element = new AbsolutePanel();
        element.getElement().getStyle().setMarginLeft(5, Style.Unit.PX);
        element.getElement().getStyle().setDisplay(displayStyle);
        element.setStyleName("hierarchyBrowserElement");
        Label label = new Label(item.getStringRepresentation());
        label.setStyleName("hierarchyBrowserLabel");
        if (displayAsHyperlinks) {
            label.addStyleName("facebook-clickable-label");
            label.addClickHandler(new HierarchyBrowserHyperlinkClickHandler("Collection item", item.getId(),
                    item.getNodeCollectionName(), eventBus, popupPanel));
        }
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn");
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                chosenItems.remove(item);
                element.removeFromParent();
                item.setChosen(false);
                selectedIds.remove(item.getId());
                eventBus.fireEvent(new HierarchyBrowserCheckBoxUpdateEvent(item));
            }
        });
        element.add(label);
        element.add(delBtn);
        mainBoxPanel.add(element);
    }

    private void displayChosenItems() {
        mainBoxPanel.clear();
        for (HierarchyBrowserItem item : chosenItems) {
            displayChosenItem(item);
        }

    }

    public void handleAddingItem(HierarchyBrowserItem item, boolean singleChoice) {
        if (singleChoice) {
            if (!chosenItems.isEmpty()) {
                HierarchyBrowserItem itemToDelete = chosenItems.get(0);
                itemToDelete.setChosen(false);
                eventBus.fireEvent(new HierarchyBrowserCheckBoxUpdateEvent(itemToDelete));
            }
            chosenItems.clear();

            mainBoxPanel.clear();
        }
        chosenItems.add(item);
        selectedIds.add(item.getId());
        displayChosenItem(item);
    }

    public void handleRemovingItem(HierarchyBrowserItem item) {
        chosenItems.remove(item);
        selectedIds.remove(item.getId());
        displayChosenItems();
    }

    public void handleAddingChosenItems(ArrayList<HierarchyBrowserItem> chosenItems, ArrayList<Id> selectedIds) {
        this.chosenItems = chosenItems;
        this.selectedIds = selectedIds;
        displayChosenItems();

    }

    public void handleReplacingChosenItem(HierarchyBrowserItem updatedItem) {
        HierarchyBrowserItem itemToReplace = findHierarchyBrowserItem(updatedItem.getId());
        if (itemToReplace == null) {
            return;
        }
        String collectionName = itemToReplace.getNodeCollectionName();
        updatedItem.setNodeCollectionName(collectionName);
        updatedItem.setChosen(itemToReplace.isChosen());
        int index = chosenItems.indexOf(itemToReplace);
        chosenItems.set(index, updatedItem);
        displayChosenItems();

    }

    private HierarchyBrowserItem findHierarchyBrowserItem(Id id) {
        for (HierarchyBrowserItem item : chosenItems) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    public void addShowTooltipLabel(ClickHandler handler) {
        Button openTooltip = new Button("...");
        openTooltip.setStyleName("light-button");
        mainBoxPanel.add(openTooltip);
        openTooltip.addClickHandler(handler);
    }

    public void setHeight(String height){
        mainBoxPanel.setHeight(height);
    }
}


