package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 13:15
 */
@ComponentName("hierarchy-browser")
public class HierarchyBrowserWidget extends BaseWidget implements HierarchyBrowserCheckBoxUpdateEventHandler,
        HierarchyBrowserItemClickEventHandler, HierarchyBrowserNodeClickEventHandler,
        HierarchyBrowserRefreshClickEventHandler, HierarchyBrowserSearchClickEventHandler, HierarchyBrowserScrollEventHandler {
    private HierarchyBrowserConfig config;
    private HierarchyBrowserMainPopup mainPopup;
    private int popupWidth;
    private int popupHeight;
    private EventBus eventBus = new SimpleEventBus();

    @Override
    public Component createNew() {
        HierarchyBrowserWidget widget = new HierarchyBrowserWidget();
        widget.registerEventsHandling();
        return widget;
    }

    public void setCurrentState(WidgetState currentState) {
        HierarchyBrowserWidgetState state = (HierarchyBrowserWidgetState) currentState;
        config = state.getHierarchyBrowserConfig();
        HierarchyBrowserView view = (HierarchyBrowserView) impl;
        WidgetDisplayConfig displayConfig = getDisplayConfig();
        final ArrayList<HierarchyBrowserItem> chosenItems = state.getChosenItems();
        final ArrayList<Id> chosenIds = state.getIds();
        final int widgetWidth = getSizeFromString(displayConfig.getWidth());
        final int widgetHeight = getSizeFromString(displayConfig.getHeight());
        popupWidth = (int) (0.7 * widgetWidth);
        popupHeight = (int) (0.7 * widgetHeight / 2);

        view.setChosenItems(chosenItems);
        view.displayBaseWidget(widgetWidth, widgetHeight);
        view.addButtonClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                mainPopup = new HierarchyBrowserMainPopup(eventBus, chosenItems, widgetWidth, widgetHeight);
                mainPopup.createAndShowPopup();
                mainPopup.addOkClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        HierarchyBrowserView view = (HierarchyBrowserView) impl;
                        view.setChosenItems(mainPopup.getChosenItems());
                        view.displayBaseWidget(widgetWidth, widgetHeight);
                        mainPopup.hidePopup();
                    }
                });
                final NodeContentManager nodeContentManager = new FirstNodeContentManager(config,
                        mainPopup, chosenIds);
                mainPopup.addLinkClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        nodeContentManager.fetchNodeContent();
                    }
                });

                nodeContentManager.fetchNodeContent();
            }
        });
    }

    @Override
    public WidgetState getCurrentState() {
        HierarchyBrowserWidgetState state = new HierarchyBrowserWidgetState();
        HierarchyBrowserView view = (HierarchyBrowserView) impl;
        state.setChosenItems(view.getChosenItems());
        return state;
    }

    @Override
    protected Widget asEditableWidget() {
        return new HierarchyBrowserView(eventBus);
    }

    @Override
    protected Widget asNonEditableWidget() {
        return asEditableWidget();
    }

    private void registerEventsHandling() {
        eventBus.addHandler(HierarchyBrowserCheckBoxUpdateEvent.TYPE, this);
        eventBus.addHandler(HierarchyBrowserNodeClickEvent.TYPE, this);
        eventBus.addHandler(HierarchyBrowserRefreshClickEvent.TYPE, this);
        eventBus.addHandler(HierarchyBrowserItemClickEvent.TYPE, this);
        eventBus.addHandler(HierarchyBrowserSearchClickEvent.TYPE, this);
        eventBus.addHandler(HierarchyBrowserScrollEvent.TYPE, this);
    }

    private int getSizeFromString(String size) {
        String temp = size.replaceAll("\\D", "");
        return Integer.parseInt(temp);
    }


    @Override
    public void onHierarchyBrowserCheckBoxUpdate(HierarchyBrowserCheckBoxUpdateEvent event) {
        HierarchyBrowserItem item = event.getItem();
        boolean chosen = item.isChosen();
        if (chosen) {
            mainPopup.handleAddingChosenItem(event.getItem());
        } else {
            mainPopup.handleRemovingChosenItem(item);
        }

    }

    @Override
    public void onHierarchyBrowserItemClick(HierarchyBrowserItemClickEvent event) {
        String collectionName = event.getCollectionName();
        PopupPanel popup = new PopupPanel();
        popup.setModal(true);
        popup.setSize(popupWidth + "px", popupHeight + "px");
        popup.getElement().getStyle().setZIndex(10);
        AbsolutePanel panel = new AbsolutePanel();
        Label label = new Label(collectionName);
        panel.add(label);
        AbsolutePanel popupButtons = createFooterButtonPanel(popup);
        panel.add(popupButtons);
        popup.add(panel);
        popup.center();
    }

    @Override
    public void onHierarchyBrowserNodeClick(HierarchyBrowserNodeClickEvent event) {
        String collectionName = event.getCollectionName();
        Id parentId = event.getParentId();
        HierarchyBrowserWidgetState state = (HierarchyBrowserWidgetState) getCurrentState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new NewNodeContentManager(config,
                mainPopup, chosenIds, collectionName, parentId);
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserRefreshClick(HierarchyBrowserRefreshClickEvent event) {
        String collectionName = event.getCollectionName();
        Id parentId = event.getParentId();
        HierarchyBrowserWidgetState state = (HierarchyBrowserWidgetState) getCurrentState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new RedrawNodeContentWithNewItemContentManager(config,
                mainPopup, chosenIds, collectionName, parentId, "");
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserSearchClick(HierarchyBrowserSearchClickEvent event) {
        String collectionName = event.getCollectionName();
        Id parentId = event.getParentId();
        String inputText = event.getInputText();
        HierarchyBrowserWidgetState state = (HierarchyBrowserWidgetState) getCurrentState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new RedrawNodeContentWithNewItemContentManager(config, mainPopup, chosenIds,
                collectionName, parentId, inputText);
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserScroll(HierarchyBrowserScrollEvent event) {
        String collectionName = event.getCollectionName();
        Id parentId = event.getParentId();
        int factor = event.getFactor();
        int offset = factor * config.getPageSize();
        String inputText = event.getInputText();
        HierarchyBrowserWidgetState state = (HierarchyBrowserWidgetState) getCurrentState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new RedrawNodeContentWithMoreItemsContentManager(config, mainPopup,
                chosenIds, collectionName, parentId, inputText, offset);
        nodeContentManager.fetchNodeContent();
    }

    private AbsolutePanel createFooterButtonPanel(final PopupPanel popup) {
        AbsolutePanel panel = new AbsolutePanel();
        Button okButton = new Button("OK");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popup.hide();

            }
        });
        Button cancelButton = new Button("CANCEL");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popup.hide();
            }
        });
        panel.add(okButton);
        panel.add(cancelButton);
        return panel;
    }

}