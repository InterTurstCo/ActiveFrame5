package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.HyperlinkClickHandler;
import ru.intertrust.cm.core.gui.impl.client.util.DisplayStyleBuilder;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.12.13
 *         Time: 16:15
 */
public class FacebookStyleView implements IsWidget {
    private AbsolutePanel mainBoxPanel;
    private Style.Display displayStyle;
    private ArrayList<TableBrowserItem> chosenItems;
    private Set<Id> selectedIds = new HashSet<Id>();
    private EventBus eventBus;
    public FacebookStyleView(SelectionStyleConfig selectionStyleConfig) {
        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("facebook-main-box");
        chosenItems = new ArrayList<TableBrowserItem>();
        displayStyle = DisplayStyleBuilder.getDisplayStyle(selectionStyleConfig);

    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    public Set<Id> getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(Set<Id> selectedIds) {
        this.selectedIds = selectedIds;
    }

    public int removeChosenItem(Id id) {
        int index = -1;
        for (TableBrowserItem chosenItem : chosenItems) {
            if (chosenItem.getId().equals(id)) {
                index = chosenItems.indexOf(chosenItem);
            }
        }
        if (index != -1) {
            chosenItems.remove(index);
        }
        return index;
    }

    public void updateHyperlinkItem(TableBrowserItem item) {
         int index = removeChosenItem(item.getId());
         if (index == -1) {
            throw new GuiException("Hyperlink in trouble!");
        }
         chosenItems.add(index, item);
         displayHyperlinkItems();

    }

    public ArrayList<TableBrowserItem> getChosenItems() {
        return chosenItems;
    }

    public void setChosenItems(ArrayList<TableBrowserItem> chosenItems) {
        this.chosenItems = chosenItems;
    }

    public void displayChosenRowItem(final TableBrowserItem rowItem) {
        final AbsolutePanel element = new AbsolutePanel();
        element.getElement().getStyle().setDisplay(displayStyle);
        element.setStyleName("facebook-element");
        Label label = new Label(rowItem.getStringRepresentation());
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn");
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                chosenItems.remove(rowItem);
                element.removeFromParent();
            }
        });
        element.add(label);
        element.add(delBtn);
        mainBoxPanel.add(element);
    }

    private void displayChosenRowItemAsHyperlink(final TableBrowserItem rowItem) {
        final AbsolutePanel element = new AbsolutePanel();
        element.getElement().getStyle().setDisplay(displayStyle);
        element.setStyleName("facebook-element");
        Label label = new Label(rowItem.getStringRepresentation());
        label.setStyleName("facebook-label");
        label.addClickHandler(new HyperlinkClickHandler("Collection item", rowItem.getId(), eventBus));
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn");
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectedIds.remove(rowItem.getId());
                chosenItems.remove(rowItem);
                element.removeFromParent();
            }
        });
        element.add(label);
        element.add(delBtn);
        mainBoxPanel.add(element);
    }

    public void displaySelectedItems() {
        mainBoxPanel.clear();
        for (TableBrowserItem rowItem : chosenItems) {
            displayChosenRowItem(rowItem);
        }

    }

    public void displayHyperlinkItems() {
        mainBoxPanel.clear();
        for (TableBrowserItem rowItem : chosenItems) {
            displayChosenRowItemAsHyperlink(rowItem);
        }

    }

    @Override
    public Widget asWidget() {
        return mainBoxPanel;
    }
}


