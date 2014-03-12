package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserCloseDialogEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserCloseDialogEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.HierarchyBrowserNoneEditablePanelWithHyperlinks;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.FormDialogBox;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.NodeMetadata;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 13:15
 */
@ComponentName("hierarchy-browser")
public class HierarchyBrowserWidget extends BaseWidget implements HierarchyBrowserCheckBoxUpdateEventHandler,
        HierarchyBrowserItemClickEventHandler, HierarchyBrowserNodeClickEventHandler,
        HierarchyBrowserRefreshClickEventHandler, HierarchyBrowserSearchClickEventHandler,
        HierarchyBrowserScrollEventHandler, HierarchyBrowserAddItemClickEventHandler, HierarchyBrowserHyperlinkStateUpdatedEventHandler, HierarchyBrowserCloseDialogEventHandler {
    private HierarchyBrowserConfig hierarchyBrowserConfig;
    private HierarchyBrowserMainPopup mainPopup;
    private boolean singleChoice;
    private EventBus localEventBus = new SimpleEventBus();

    @Override
    public Component createNew() {
        HierarchyBrowserWidget widget = new HierarchyBrowserWidget();
        widget.registerEventsHandling();
        return widget;
    }

    public void setCurrentState(WidgetState currentState) {
        HierarchyBrowserWidgetState state = (HierarchyBrowserWidgetState) currentState;
        if (isEditable()) {
            setCurrentStateForEditableWidget(state);
        } else {
            setCurrentStateForNoneEditableWidget(state);
        }
    }

    private void setCurrentStateForEditableWidget(HierarchyBrowserWidgetState state) {
        final HierarchyBrowserView view = (HierarchyBrowserView) impl;
        view.initAddButton(hierarchyBrowserConfig.getAddButtonConfig());
        view.initClearButtonIfItIs(hierarchyBrowserConfig.getClearAllButtonConfig());
        final WidgetDisplayConfig displayConfig = getDisplayConfig();
        final ArrayList<HierarchyBrowserItem> chosenItems = state.getChosenItems();

        DialogWindowConfig dialogWindowConfig = hierarchyBrowserConfig.getDialogWindowConfig();
        final int popupWidth = getSizeFromString(dialogWindowConfig != null ?
                dialogWindowConfig.getWidth() : null, HierarchyBrowserMainPopup.DEFAULT_WIDTH);
        final int popupHeight = getSizeFromString(dialogWindowConfig != null ?
                dialogWindowConfig.getHeight() : null, HierarchyBrowserMainPopup.DEFAULT_HEIGHT);
        singleChoice = state.isSingleChoice();

        view.setChosenItems(chosenItems);
        final boolean displayAsHyperlinks = displayHyperlinks();
        view.displayBaseWidget(displayConfig.getWidth(), displayConfig.getHeight());
        final ArrayList<HierarchyBrowserItem> copyOfItems = getCopyOfChosenItems(chosenItems);
        view.addButtonClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                mainPopup = new HierarchyBrowserMainPopup(localEventBus, copyOfItems, popupWidth, popupHeight, null, displayAsHyperlinks);
                mainPopup.createAndShowPopup();
                mainPopup.addOkClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        HierarchyBrowserView view = (HierarchyBrowserView) impl;
                        view.setChosenItems(mainPopup.getChosenItems());
                        view.displayBaseWidget(displayConfig.getWidth(), displayConfig.getHeight());
                        mainPopup.hidePopup();

                    }
                });
                mainPopup.addCancelClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        mainPopup.hidePopup();
                        copyOfItems.clear();
                        copyOfItems.addAll(view.getChosenItems());
                    }
                });
                final NodeContentManager nodeContentManager = new FirstNodeContentManager(hierarchyBrowserConfig,
                        mainPopup, createNewState().getIds());
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

    private void setCurrentStateForNoneEditableWidget(HierarchyBrowserWidgetState state) {
        List<HierarchyBrowserItem> hierarchyBrowserItems = state.getChosenItems();
        HierarchyBrowserNoneEditablePanelWithHyperlinks noneEditablePanel = (HierarchyBrowserNoneEditablePanelWithHyperlinks) impl;
        noneEditablePanel.cleanPanel();
        if (displayHyperlinks()) {
            for (HierarchyBrowserItem item : hierarchyBrowserItems) {
                noneEditablePanel.displayHyperlink(item);
            }
        } else {

            for (HierarchyBrowserItem item : hierarchyBrowserItems) {
                String representation = item.getStringRepresentation();
                noneEditablePanel.displayItem(representation);
            }
        }

    }

    @Override
    protected HierarchyBrowserWidgetState createNewState() {
        if (isEditable()) {
            HierarchyBrowserWidgetState state = new HierarchyBrowserWidgetState();
            HierarchyBrowserView view = (HierarchyBrowserView) impl;
            state.setChosenItems(view.getChosenItems());
            state.setConstraints(getInitialData().getConstraints());
            return state;
        } else {
            return getInitialData();
        }

    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        HierarchyBrowserWidgetState browserState = (HierarchyBrowserWidgetState) state;
        hierarchyBrowserConfig = browserState.getHierarchyBrowserConfig();
        SelectionStyleConfig selectionStyleConfig = hierarchyBrowserConfig.getSelectionStyleConfig();
        boolean displayAsHyperlinks = displayHyperlinks();
        return new HierarchyBrowserView(selectionStyleConfig, localEventBus, displayAsHyperlinks);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        HierarchyBrowserWidgetState browserState = (HierarchyBrowserWidgetState) state;
        hierarchyBrowserConfig = browserState.getHierarchyBrowserConfig();
        SelectionStyleConfig selectionStyleConfig = hierarchyBrowserConfig.getSelectionStyleConfig();
        return new HierarchyBrowserNoneEditablePanelWithHyperlinks(selectionStyleConfig, localEventBus);
    }


    private void registerEventsHandling() {
        localEventBus.addHandler(HierarchyBrowserCheckBoxUpdateEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserNodeClickEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserRefreshClickEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserItemClickEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserAddItemClickEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserSearchClickEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserScrollEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserHyperlinkStateUpdatedEvent.TYPE, this);
    }

    private int getSizeFromString(String size, int defaultSize) {
        if (size == null) {
            return defaultSize;
        }
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
                final FormDialogBox editableFormDialogBox = new FormDialogBox("Редактирование " + title);
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
                                localEventBus.fireEvent(new HyperlinkStateChangedEvent(id));

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
        HierarchyBrowserWidgetState state = createNewState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new NewNodeContentManager(hierarchyBrowserConfig,
                mainPopup, chosenIds, collectionName, parentId);
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserRefreshClick(HierarchyBrowserRefreshClickEvent event) {
        NodeMetadata nodeMetadata = event.getNodeMetadata();
        String collectionName = nodeMetadata.getCollectionName();
        Id parentId = nodeMetadata.getParentId();
        HierarchyBrowserWidgetState state = createNewState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new RedrawNodeContentWithNewItemContentManager(hierarchyBrowserConfig,
                mainPopup, chosenIds, collectionName, parentId, "");
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserSearchClick(HierarchyBrowserSearchClickEvent event) {
        NodeMetadata nodeMetadata = event.getNodeMetadata();
        String collectionName = nodeMetadata.getCollectionName();
        Id parentId = nodeMetadata.getParentId();
        String inputText = event.getInputText();
        HierarchyBrowserWidgetState state = createNewState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new RedrawNodeContentWithNewItemContentManager(hierarchyBrowserConfig, mainPopup, chosenIds,
                collectionName, parentId, inputText);
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserScroll(HierarchyBrowserScrollEvent event) {
        NodeMetadata nodeMetadata = event.getNodeMetadata();
        String collectionName = nodeMetadata.getCollectionName();
        Id parentId = nodeMetadata.getParentId();
        int factor = event.getFactor();
        int offset = factor * hierarchyBrowserConfig.getPageSize();
        String inputText = event.getInputText();
        HierarchyBrowserWidgetState state = createNewState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new RedrawNodeContentWithMoreItemsContentManager(hierarchyBrowserConfig, mainPopup,
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
                        localEventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(nodeMetadata));
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

    @Override
    public void onHierarchyBrowserHyperlinkStateUpdatedEvent(HierarchyBrowserHyperlinkStateUpdatedEvent event) {
        Id id = event.getId();
        String collectionName = event.getCollectionName();
        if (isEditable()) {
            HierarchyBrowserView view = (HierarchyBrowserView) impl;
            HierarchyBrowserHyperlinkContentManager hyperlinkContentManager = new EditableHierarchyBrowserHyperlinkContentManager(id, collectionName, hierarchyBrowserConfig, view, mainPopup);
            hyperlinkContentManager.updateHyperlink();
        } else {
            HierarchyBrowserWidgetState state = createNewState();
            List<HierarchyBrowserItem> items = state.getChosenItems();
            HierarchyBrowserNoneEditablePanelWithHyperlinks noneEditablePanel = (HierarchyBrowserNoneEditablePanelWithHyperlinks) impl;
            HierarchyBrowserHyperlinkContentManager hyperlinkContentManager = new NoneEditableHierarchyBrowserHyperlinkContentManager(id, collectionName,
                    hierarchyBrowserConfig, noneEditablePanel, items);
            hyperlinkContentManager.updateHyperlink();
        }

    }

    private boolean displayHyperlinks() {
        DisplayValuesAsLinksConfig displayValuesAsLinksConfig = hierarchyBrowserConfig.getDisplayValuesAsLinksConfig();
        return displayValuesAsLinksConfig != null && displayValuesAsLinksConfig.isValue();
    }

    private ArrayList<HierarchyBrowserItem> getCopyOfChosenItems(ArrayList<HierarchyBrowserItem> itemsToCopy) {
        ArrayList<HierarchyBrowserItem> copyOfItems = new ArrayList<HierarchyBrowserItem>();
        for (HierarchyBrowserItem item : itemsToCopy) {
            copyOfItems.add(item);
        }
        return copyOfItems;
    }

    @Override
    public void onHierarchyBrowserCloseDialogEvent(HierarchyBrowserCloseDialogEvent event) {
        if (mainPopup != null) {
        mainPopup.hidePopup();
        List<HierarchyBrowserItem> itemsInDialogWindow  = mainPopup.getChosenItems();
        itemsInDialogWindow.clear();
         HierarchyBrowserView view = (HierarchyBrowserView) impl;
        itemsInDialogWindow.addAll(view.getChosenItems());
        }
    }

}