package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.AddButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ClearAllButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserShowTooltipEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.ButtonForm;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.12.13
 *         Time: 13:15
 */
public class HierarchyBrowserView extends Composite implements HierarchyBrowserDisplay {

    private HierarchyBrowserItemsView widgetChosenContent;
    private Panel widgetContainer;
    private FocusPanel openPopupButton;
    private FocusPanel clearButton;
    private EventBus eventBus;
    private SelectionStyleConfig selectionStyleConfig;
    private boolean displayAsHyperlinks;

    public HierarchyBrowserView(SelectionStyleConfig selectionStyleConfig, EventBus eventBus,boolean displayAsHyperlinks) {

        this.eventBus = eventBus;
        this.selectionStyleConfig = selectionStyleConfig;
        this.displayAsHyperlinks = displayAsHyperlinks;
        widgetContainer = new HorizontalPanel();
        widgetContainer.addStyleName("hierarh-browser-wrapper");

        initWidget(widgetContainer);
    }


    @Override
    public Widget asWidget() {
        return widgetContainer;
    }

    public void initWidgetContent(HierarchyBrowserConfig config, ClickHandler clearHandler) {
        openPopupButton = new FocusPanel();
        widgetChosenContent = new HierarchyBrowserItemsView(selectionStyleConfig, eventBus, displayAsHyperlinks);
        widgetChosenContent.asWidget().setStyleName("hierarh-browser-inline hierarchyBrowserBorder");
        widgetContainer.add(widgetChosenContent);

        initAddButton(config.getAddButtonConfig());
        initClearButtonIfItIs(config.getClearAllButtonConfig(), clearHandler);

    }

    public HandlerRegistration addButtonClickHandler(ClickHandler openButtonClickHandler) {
       return openPopupButton.addDomHandler(openButtonClickHandler, ClickEvent.getType());
    }

    public void displayBaseWidget(String width, String height, List<HierarchyBrowserItem> chosenItems, boolean shouldDrawTooltipButton) {
        setSizeIfConfigured(width, height);

        widgetChosenContent.setTooltipClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserShowTooltipEvent(widgetChosenContent));
            }
        });

        widgetChosenContent.displayChosenItems(chosenItems, shouldDrawTooltipButton);

    }

    private void setSizeIfConfigured(String width, String height){
        if(width != null){
            widgetContainer.setWidth(width);
        }
        if(height != null){
            widgetContainer.setWidth(height);
            widgetChosenContent.setHeight("100px"); //TODO find other solution to see scrollbar
        }
    }


    public void initAddButton(AddButtonConfig config) {
        openPopupButton.clear();
        ButtonForm buttonForm;
        String text = config.getText();
        buttonForm = new ButtonForm(openPopupButton, config.getImage(), text);
        openPopupButton.add(buttonForm);
        openPopupButton.addStyleName("hierar-add-button");
        widgetContainer.add(openPopupButton);
    }

    public void initClearButtonIfItIs(ClearAllButtonConfig config, ClickHandler clickHandler) {
        if (config != null) {
            if (clearButton != null) {
                clearButton.removeFromParent();
            }
            clearButton = new FocusPanel();
            ButtonForm buttonForm = new ButtonForm(clearButton, config.getImage(), config.getText());
            clearButton.add(buttonForm);
            clearButton.addStyleName("hierar-clear-button");
            widgetContainer.add(clearButton);
            clearButton.addClickHandler(clickHandler);

        }
    }

   public void clear(){
       widgetContainer.clear();
   }
   public void displayChosenItems(List<HierarchyBrowserItem> chosenItems, boolean shouldDisplayTooltipButton){
       widgetChosenContent.displayChosenItems(chosenItems, shouldDisplayTooltipButton);
   }

    @Override
    public void display(List<HierarchyBrowserItem> items, boolean shouldDrawTooltipButton) {
        widgetChosenContent.display(items, shouldDrawTooltipButton);
    }
}