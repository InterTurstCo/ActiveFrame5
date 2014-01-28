package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.form.widget.AddButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ClearAllButtonConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.ButtonForm;
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
        openPopupButton = new FocusPanel();
        widgetContainer = new AbsolutePanel();
        widgetContainer.setStyleName("hierarh-browser-inline");
        buttonActionPanel = new VerticalPanel();
        buttonActionPanel.setStyleName("hierarh-browser-inline");
        Label label = new Label("Адресаты:");
        label.setStyleName("hierarh-browser-inline");
        widgetChosenContent = new HierarchyBrowserFacebookStyleView(eventBus);
        widgetChosenContent.asWidget().setStyleName("hierarh-browser-inline hierarh-browser-border");

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

    public void initAddButton(AddButtonConfig config){
        ButtonForm buttonForm;
        String text = config.getText();
        if (text.equals("...")) {
            text = "Выбрать";

        }
        if (config != null){
            buttonForm = new ButtonForm(openPopupButton, config.getImage(), text);
        }else {
            buttonForm = new ButtonForm(openPopupButton, "images/green-plus.png", text);
        }


        openPopupButton.add(buttonForm);
        widgetContainer.add(openPopupButton);
    }

    public void initClearButtonIfItIs(ClearAllButtonConfig config){

         if (config != null){
             clearButton = new FocusPanel();
             ButtonForm buttonForm = new ButtonForm(clearButton, config.getImage(), config.getText());
             clearButton.add(buttonForm);
             widgetContainer.add(clearButton);
             clearButton.addClickHandler(new ClickHandler() {
                 @Override
                 public void onClick(ClickEvent event) {
                     chosenItems.clear();
                     widgetChosenContent.handleAddingChosenItems(chosenItems);

                 }
             });



         }


     }


}