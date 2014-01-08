package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.impl.client.event.HierarchyBrowserCheckBoxUpdateEvent;
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

    public HierarchyBrowserFacebookStyleView(EventBus eventBus) {
        this.eventBus = eventBus;
        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("facebook-main-box");

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
        element.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        element.setStyleName("facebook-element");
        Label label = new Label(item.getStringRepresentation());
        label.setStyleName("facebook-label");
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

    public void handleAddingChosenItem(HierarchyBrowserItem item) {
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

    @Override
    public Widget asWidget() {
        return mainBoxPanel;
    }
}


