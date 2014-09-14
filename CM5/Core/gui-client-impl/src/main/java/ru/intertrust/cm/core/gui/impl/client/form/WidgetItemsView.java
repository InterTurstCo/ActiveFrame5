package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkClickHandler;
import ru.intertrust.cm.core.gui.impl.client.util.DisplayStyleBuilder;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.12.13
 *         Time: 16:15
 */
public class WidgetItemsView extends Composite {
    private AbsolutePanel mainBoxPanel;
    protected AbsolutePanel container;
    private Style.Display displayStyle;
    private LinkedHashMap<Id, String> listValues;
    private Set<Id> selectedIds = new HashSet<Id>();
    private EventBus eventBus;
    private PopupPanel popupPanel;

    public WidgetItemsView(SelectionStyleConfig selectionStyleConfig) {
        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("facebook-main-box");
        listValues = new LinkedHashMap<Id, String>();
        displayStyle = DisplayStyleBuilder.getDisplayStyle(selectionStyleConfig);

        container = new AbsolutePanel();
        container.add(mainBoxPanel);
        initWidget(container);
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

    public void removeChosenItem(Id id) {
        listValues.remove(id);
    }

    public void updateHyperlinkItem(Id id, String representation) {
        listValues.put(id, representation);
        displayHyperlinkItems();

    }

    public void setPopupPanel(PopupPanel popupPanel) {
        this.popupPanel = popupPanel;
    }

    public LinkedHashMap<Id, String> getListValues() {
        return listValues;
    }

    public void setListValues(LinkedHashMap<Id, String> listValues) {
        this.listValues = listValues;
    }

    public void displayChosenRowItem(Map.Entry<Id, String> entry) {
        final AbsolutePanel element = new AbsolutePanel();

        element.setStyleName("facebook-element");
        Label label = new Label(entry.getValue());
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn");
        final Id id = entry.getKey();
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                listValues.remove(id);
                selectedIds.remove(id);
                element.removeFromParent();
            }
        });
        element.add(label);
        element.add(delBtn);
        if (displayStyle.equals(Style.Display.INLINE_BLOCK)) {
            element.getElement().getStyle().setFloat(Style.Float.LEFT);
            label.getElement().getStyle().setFloat(Style.Float.LEFT);
            delBtn.getElement().getStyle().setFloat(Style.Float.LEFT);
        }
        mainBoxPanel.add(element);
    }

    private void displayChosenRowItemAsHyperlink(Map.Entry<Id, String> entry) {
        final AbsolutePanel element = new AbsolutePanel();
        //  element.getElement().getStyle().setDisplay(displayStyle);
        element.setStyleName("facebook-element");
        Label label = new Label(entry.getValue());
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");
        final Id id = entry.getKey();
        label.addClickHandler(new HyperlinkClickHandler(id, popupPanel, eventBus));
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn facebookElementDel");
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectedIds.remove(id);
                listValues.remove(id);
                element.removeFromParent();
            }
        });
        element.add(label);
        element.add(delBtn);
        if (displayStyle.equals(Style.Display.INLINE_BLOCK)) {
            element.getElement().getStyle().setFloat(Style.Float.LEFT);
            label.getElement().getStyle().setFloat(Style.Float.LEFT);
            delBtn.getElement().getStyle().setFloat(Style.Float.LEFT);
        }
        mainBoxPanel.add(element);
    }

    public void displayItems() {
        mainBoxPanel.clear();
        Set<Map.Entry<Id, String>> entries = listValues.entrySet();
        for (Map.Entry<Id, String> entry : entries) {
            displayChosenRowItem(entry);
        }

    }

    public void displayHyperlinkItems() {
        mainBoxPanel.clear();
        Set<Map.Entry<Id, String>> entries = listValues.entrySet();
        for (Map.Entry<Id, String> entry : entries) {
            displayChosenRowItemAsHyperlink(entry);
        }

    }


}


