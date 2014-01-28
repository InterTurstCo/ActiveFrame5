package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.FormDialogBox;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.NodeMetadata;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 13:15
 */
@ComponentName("hierarchy-browser")
public class HierarchyBrowserWidget extends BaseWidget implements HierarchyBrowserCheckBoxUpdateEventHandler,
        HierarchyBrowserItemClickEventHandler, HierarchyBrowserNodeClickEventHandler,
        HierarchyBrowserRefreshClickEventHandler, HierarchyBrowserSearchClickEventHandler,
        HierarchyBrowserScrollEventHandler, HierarchyBrowserAddItemClickEventHandler {
    private HierarchyBrowserConfig config;
    private HierarchyBrowserMainPopup mainPopup;
    private HierarchyBrowserView hierarchyBrowserView;
    private EventBus eventBus = new SimpleEventBus();
    private boolean singleChoice;

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
        hierarchyBrowserView.initAddButton(config.getAddButtonConfig());
        hierarchyBrowserView.initClearButtonIfItIs(config.getClearAllButtonConfig());

        WidgetDisplayConfig displayConfig = getDisplayConfig();
        final ArrayList<HierarchyBrowserItem> chosenItems = state.getChosenItems();
        final ArrayList<Id> chosenIds = state.getIds();
        final int widgetWidth = getSizeFromString(displayConfig.getWidth());
        final int widgetHeight = getSizeFromString(displayConfig.getHeight());

        singleChoice = state.isSingleChoice();
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
         hierarchyBrowserView = new HierarchyBrowserView(eventBus);
        return hierarchyBrowserView;
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
        eventBus.addHandler(HierarchyBrowserAddItemClickEvent.TYPE, this);
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
            mainPopup.handleAddingChosenItem(event.getItem(), singleChoice);
        } else {
            mainPopup.handleRemovingChosenItem(item);
        }

    }

    @Override
    public void onHierarchyBrowserItemClick(HierarchyBrowserItemClickEvent event) {
        final Id id = event.getItemId();
        final NodeMetadata nodeMetadata = event.getMetadata();
        final String title = nodeMetadata.getDomainObjectType();
        final FormPluginConfig config = new FormPluginConfig();
        config.setDomainObjectId(id);
        config.getPluginState().setEditable(false);
        final FormDialogBox noneEditableFormDialogBox = new FormDialogBox(title);
        config.getPluginState().setToggleEdit(true);
        config.getPluginState().setInCentralPanel(true);
        final FormPlugin noneEditableFormPlugin = noneEditableFormDialogBox.createFormPlugin(config);
        noneEditableFormDialogBox.initButton("Открыть в полном окне", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                noneEditableFormPlugin.setDisplayActionToolBar(true);
                noneEditableFormPlugin.setLocalEventBus(getEventBus());
                Application.getInstance().getEventBus()
                        .fireEvent(new CentralPluginChildOpeningRequestedEvent(noneEditableFormPlugin));
                noneEditableFormDialogBox.hide();
                mainPopup.hidePopup();
            }
        });
        noneEditableFormDialogBox.initButton("Изменить", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                noneEditableFormDialogBox.hide();
                config.getPluginState().setEditable(true);
                final FormDialogBox editableFormDialogBox =
                        new FormDialogBox("Редактирование " + title);
                final FormPlugin editableFormPlugin = editableFormDialogBox.createFormPlugin(config);
                editableFormDialogBox.initButton("Изменить", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        final SaveAction action = ComponentRegistry.instance.get("save.action");
                        SaveActionContext saveActionContext = new SaveActionContext();
                        saveActionContext.setRootObjectId(id);
                        action.setInitialContext(saveActionContext);
                        action.setPlugin(editableFormPlugin);
                        action.addActionSuccessListener(new ActionSuccessListener() {
                            @Override
                            public void onSuccess() {
                                editableFormDialogBox.hide();
                                eventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(nodeMetadata));
                            }
                        });
                        action.execute();

                    }
                });
                editableFormDialogBox.initButton("Отмена", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {

                        editableFormDialogBox.hide();
                        config.getPluginState().setEditable(false);
                    }
                });

            }

        });
      noneEditableFormDialogBox.initButton("Отмена", new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
             noneEditableFormDialogBox.hide();
          }
      });

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
        NodeMetadata nodeMetadata = event.getNodeMetadata();
        String collectionName = nodeMetadata.getCollectionName();
        Id parentId = nodeMetadata.getParentId();
        HierarchyBrowserWidgetState state = (HierarchyBrowserWidgetState) getCurrentState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new RedrawNodeContentWithNewItemContentManager(config,
                mainPopup, chosenIds, collectionName, parentId, "");
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserSearchClick(HierarchyBrowserSearchClickEvent event) {
        NodeMetadata nodeMetadata = event.getNodeMetadata();
        String collectionName = nodeMetadata.getCollectionName();
        Id parentId = nodeMetadata.getParentId();
        String inputText = event.getInputText();
        HierarchyBrowserWidgetState state = (HierarchyBrowserWidgetState) getCurrentState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new RedrawNodeContentWithNewItemContentManager(config, mainPopup, chosenIds,
                collectionName, parentId, inputText);
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserScroll(HierarchyBrowserScrollEvent event) {
        NodeMetadata nodeMetadata = event.getNodeMetadata();
        String collectionName = nodeMetadata.getCollectionName();
        Id parentId = nodeMetadata.getParentId();
        int factor = event.getFactor();
        int offset = factor * config.getPageSize();
        String inputText = event.getInputText();
        HierarchyBrowserWidgetState state = (HierarchyBrowserWidgetState) getCurrentState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new RedrawNodeContentWithMoreItemsContentManager(config, mainPopup,
                chosenIds, collectionName, parentId, inputText, offset);
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserAddItemClick(HierarchyBrowserAddItemClickEvent event) {
        final NodeMetadata nodeMetadata = event.getMetadata();
        final Id parentId = nodeMetadata.getParentId();
        String domainObjectTypeToCreate = nodeMetadata.getDomainObjectType();
        String title = "Создать " + nodeMetadata.getDomainObjectType();
        FormPluginConfig config = new FormPluginConfig();
        config.setDomainObjectTypeToCreate(domainObjectTypeToCreate);
        config.getPluginState().setEditable(true);
        final FormDialogBox createItemDialogBox = new FormDialogBox(title);
        final FormPlugin createFormPlugin = createItemDialogBox.createFormPlugin(config);
        createItemDialogBox.initButton("Cохранить", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final SaveAction action = ComponentRegistry.instance.get("save.action");
                SaveActionContext saveActionContext = new SaveActionContext();
                saveActionContext.setRootObjectId(parentId);
                action.setInitialContext(saveActionContext);
                action.setPlugin(createFormPlugin);
                action.addActionSuccessListener(new ActionSuccessListener() {
                    @Override
                    public void onSuccess() {
                        createItemDialogBox.hide();
                        eventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(nodeMetadata));
                    }
                });
                action.execute();

            }
        });
        createItemDialogBox.initButton("Отмена", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createItemDialogBox.hide();
            }
        });
    }
}