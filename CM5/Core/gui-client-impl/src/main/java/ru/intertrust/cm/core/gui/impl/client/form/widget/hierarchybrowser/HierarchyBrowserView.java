package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.form.widget.ClearAllButtonConfig;
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
    private FocusPanel clearButton;
    private VerticalPanel buttonActionPanel;
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
        widgetContainer = new AbsolutePanel();
        widgetContainer.setStyleName("hierarh-browser-inline");
        buttonActionPanel = new VerticalPanel();
        buttonActionPanel.setStyleName("hierarh-browser-inline");
        Label label = new Label("Адресаты:");
        label.setStyleName("hierarh-browser-inline");
        widgetChosenContent = new HierarchyBrowserFacebookStyleView(eventBus);
        widgetChosenContent.asWidget().setStyleName("hierarh-browser-inline hierarh-browser-border");

        openPopupButton = new FocusPanel();
        Image plus = new Image("images/green-plus.png");
        Label text = new Label("Выбрать");
        AbsolutePanel buttonPanel = setStyleButton(plus, text, openPopupButton);

        openPopupButton.add(buttonPanel);

        widgetContainer.add(label);
        widgetContainer.add(widgetChosenContent);
        widgetContainer.add(buttonActionPanel);
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

     public void drawClearButtonIfItIs(ClearAllButtonConfig config){

         if (config != null){
             clearButton = new FocusPanel();
             Image img;
             Label text;
             if (config.getImage() != null){
                 img = new Image(config.getImage());
             } else {
                 img = new Image("");
             }

             if (config.getText() != null){
                 text = new Label(config.getText());
             }    else {
                 text = new Label("");
             }
             AbsolutePanel buttonPanel = setStyleButton(img, text, clearButton);

             clearButton.add(buttonPanel);


             clearButton.addClickHandler(new ClickHandler() {
                 @Override
                 public void onClick(ClickEvent event) {
                     chosenItems.clear();
                     widgetChosenContent.handleAddingChosenItems(chosenItems);

                 }
             });



         }


     }

    private AbsolutePanel setStyleButton(Image img, Label text, FocusPanel focusPanel){
        AbsolutePanel buttonPanel = new AbsolutePanel();
        focusPanel.setStyleName("hierarh-browser-inline composite-button");
        buttonPanel.setStyleName("hierarh-browser-button");
        img.setStyleName("hierarh-browser-inline-margin-left");
        text.setStyleName("hierarh-browser-inline-margin-left");
        buttonPanel.add(img);
        buttonPanel.add(text);
        buttonActionPanel.add(focusPanel);


        return buttonPanel;

    }
}