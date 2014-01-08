package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.12.13
 *         Time: 13:15
 */
public class HierarchyBrowserView extends Composite {

    private HierarchyBrowserFacebookStyleView widgetChosenContent;
    private ArrayList<HierarchyBrowserItem> chosenItems;
    private AbsolutePanel widgetContainer;
    private FocusPanel openPopupButton;
    private EventBus eventBus;

    public HierarchyBrowserView(EventBus eventBus) {
        this.eventBus = eventBus;
        widgetContainer = initWidgetContent();
        initWidget(widgetContainer);
    }

    public void setChosenItems(ArrayList<HierarchyBrowserItem> chosenItems) {
        this.chosenItems = chosenItems;
    }

    public ArrayList<HierarchyBrowserItem> getChosenItems() {
        return widgetChosenContent.getChosenItems();
    }

    @Override
    public Widget asWidget() {
        return widgetContainer;
    }

    private AbsolutePanel initWidgetContent() {
        AbsolutePanel widgetContainer = new AbsolutePanel();
        widgetContainer.setStyleName("hierarh-browser-inline");

        Label label = new Label("Адресаты:");
        label.setStyleName("hierarh-browser-inline");
        widgetChosenContent = new HierarchyBrowserFacebookStyleView(eventBus);
        widgetChosenContent.asWidget().setStyleName("hierarh-browser-inline hierarh-browser-border");

        openPopupButton = new FocusPanel();
        openPopupButton.setStyleName("hierarh-browser-inline composite-button");
        AbsolutePanel buttonPanel = new AbsolutePanel();
        buttonPanel.setStyleName("hierarh-browser-button");
        Image plus = new Image("images/green-plus.png");
        plus.setStyleName("hierarh-browser-inline-margin-left");
        Label text = new Label("Выбрать");
        text.setStyleName("hierarh-browser-inline-margin-left");
        buttonPanel.add(plus);
        buttonPanel.add(text);
        openPopupButton.add(buttonPanel);

        widgetContainer.add(label);
        widgetContainer.add(widgetChosenContent);
        widgetContainer.add(openPopupButton);
        return widgetContainer;
    }

    public void addButtonClickHandler(ClickHandler openButtonClickHandler) {
        openPopupButton.addClickHandler(openButtonClickHandler);
    }

    public void displayBaseWidget(int width, int height) {
        int widgetWidth = width != 0 ? width : 900;
        int widgetHeight = height != 0 ? height : 400;
        widgetChosenContent.asWidget().setSize(0.7 * widgetWidth + "px", 0.3 * widgetHeight + "px");
        widgetChosenContent.handleAddingChosenItems(chosenItems);
    }

}