package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.event.HierarchyBrowserCheckBoxUpdateEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.HierarchyBrowserHyperlinkClickHandler;
import ru.intertrust.cm.core.gui.impl.client.util.DisplayStyleBuilder;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 20.12.13
 *         Time: 13:15
 */
public class HierarchyBrowserFacebookStyleView implements IsWidget {

    private AbsolutePanel mainBoxPanel;
    private EventBus eventBus;
    private ArrayList<HierarchyBrowserItem> chosenItems = new ArrayList<HierarchyBrowserItem>();

    private Style.Display displayStyle;
    private boolean displayAsHyperlinks;
    public HierarchyBrowserFacebookStyleView(SelectionStyleConfig selectionStyleConfig, EventBus eventBus, boolean displayAsHyperlink) {
        this.eventBus = eventBus;
        this.displayAsHyperlinks = displayAsHyperlink;
        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("facebook-main-box");
        displayStyle = DisplayStyleBuilder.getDisplayStyle(selectionStyleConfig);
        mainBoxPanel.getElement().getStyle().clearOverflowY();

    }

    public ArrayList<HierarchyBrowserItem> getChosenItems() {
        return chosenItems;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    private void displayChosenItem(final HierarchyBrowserItem item) {

        final AbsolutePanel element = new AbsolutePanel();
        element.getElement().getStyle().setMarginLeft(5, Style.Unit.PX);
        element.getElement().getStyle().setDisplay(displayStyle);
        element.setStyleName("facebook-element");
        Label label = new Label(item.getStringRepresentation());
        label.setStyleName("facebook-label");
        if (displayAsHyperlinks) {
            label.getElement().getStyle().setCursor(Style.Cursor.POINTER);
            label.addClickHandler(new HierarchyBrowserHyperlinkClickHandler("Collection item", item.getId(), item.getNodeCollectionName(), eventBus));
        }
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn");
        delBtn.getElement().getStyle().setPadding(2, Style.Unit.PX);
        delBtn.getElement().getStyle().setBackgroundColor("red");
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                chosenItems.remove(item);
                element.removeFromParent();
                item.setChosen(false);
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

    public void handleAddingChosenItem(HierarchyBrowserItem item, boolean singleChoice) {
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
        displayChosenItem(item);
    }

    public void handleRemovingChosenItem(HierarchyBrowserItem item) {
        chosenItems.remove(item);
        displayChosenItems();
    }

    public void handleAddingChosenItems(ArrayList<HierarchyBrowserItem> chosenItems) {
        this.chosenItems = chosenItems;
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
        }  return null;
    }


    @Override
    public Widget asWidget() {
        return mainBoxPanel;
    }
}


