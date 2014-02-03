package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
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
    public static final String DEFAULT_WIDTH = "400px";
    public static final String DEFAULT_HEIGHT = "150px";
    private static final String MIN_HEIGHT = "20px";
    private HierarchyBrowserFacebookStyleView widgetChosenContent;
    private ArrayList<HierarchyBrowserItem> chosenItems;
    private DockPanel widgetContainer;
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

    private DockPanel initWidgetContent() {
        openPopupButton = new FocusPanel();
        widgetContainer = new DockPanel();

        widgetContainer.setStyleName("hierarh-browser-inline");
        buttonActionPanel = new VerticalPanel(); //TODO: looks like it's never used
        buttonActionPanel.setStyleName("hierarh-browser-inline");
        Label label = new Label("Адресаты:");
        label.setStyleName("hierarh-browser-inline");
        widgetChosenContent = new HierarchyBrowserFacebookStyleView(eventBus);
        widgetChosenContent.asWidget().setStyleName("hierarh-browser-inline hierarh-browser-border");
        widgetContainer.add(label, DockPanel.WEST);
        widgetContainer.add(widgetChosenContent, DockPanel.CENTER);
        widgetContainer.add(buttonActionPanel, DockPanel.EAST);


        return widgetContainer;
    }

    public void addButtonClickHandler(ClickHandler openButtonClickHandler) {
        openPopupButton.addClickHandler(openButtonClickHandler);
    }

    public void displayBaseWidget(String width, String height) {
        String widgetWidth = width != null ? width : DEFAULT_WIDTH;
        String widgetHeight = height != null ? height : DEFAULT_HEIGHT;

        widgetChosenContent.handleAddingChosenItems(chosenItems);
        widgetContainer.setSize(widgetWidth, widgetHeight);

        widgetContainer.setCellWidth(widgetChosenContent, "100%");
        widgetContainer.setCellHeight(widgetChosenContent, widgetHeight);

        widgetChosenContent.asWidget().getElement().getStyle().setProperty("minHeight", MIN_HEIGHT);
        widgetChosenContent.asWidget().setSize("100%", "100%");
    }

    public void initAddButton(AddButtonConfig config){
        openPopupButton.clear();
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
        widgetContainer.add(openPopupButton, DockPanel.EAST);
    }

    public void initClearButtonIfItIs(ClearAllButtonConfig config){
         if (config != null){
             clearButton = new FocusPanel();
             ButtonForm buttonForm = new ButtonForm(clearButton, config.getImage(), config.getText());
             clearButton.add(buttonForm);
             clearButton.getElement().getStyle().setMarginLeft(24, Style.Unit.PX);
             widgetContainer.add(clearButton, DockPanel.EAST);
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