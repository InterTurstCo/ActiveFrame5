package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.ActionSuccessListener;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.*;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.FormDialogBox;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.TooltipSizer;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 13:15
 */
@ComponentName("hierarchy-browser")
public class HierarchyBrowserWidget extends BaseWidget implements HierarchyBrowserCheckBoxUpdateEventHandler,
        HierarchyBrowserItemClickEventHandler, HierarchyBrowserNodeClickEventHandler,
        HierarchyBrowserRefreshClickEventHandler, HierarchyBrowserSearchClickEventHandler,
        HierarchyBrowserScrollEventHandler, HierarchyBrowserAddItemClickEventHandler,
        HierarchyBrowserHyperlinkStateUpdatedEventHandler, HierarchyBrowserCloseDialogEventHandler,
        HierarchyBrowserShowTooltipEventHandler {
    private HierarchyBrowserWidgetState currentState;
    private HierarchyBrowserMainPopup mainPopup;
    private boolean singleChoice;
    private Map<String, NodeCollectionDefConfig> collectionNameNodeMap;
    private EventBus localEventBus = new SimpleEventBus();
    private HandlerRegistration handlerRegistration;

    @Override
    public Component createNew() {
        HierarchyBrowserWidget widget = new HierarchyBrowserWidget();
        widget.registerEventsHandling();
        return widget;
    }

    public void setCurrentState(WidgetState currentState) {
        this.currentState = (HierarchyBrowserWidgetState) currentState;
        initialData = currentState;
        if (isEditable()) {
            setCurrentStateForEditableWidget();
        } else {
            setCurrentStateForNoneEditableWidget();
        }
    }

    @Override
    protected boolean isChanged() {
        final List<Id> initialValue = ((HierarchyBrowserWidgetState) getInitialData()).getIds();
        final List<Id> currentValue = ((HierarchyBrowserView) impl).getSelectedIds();
        return initialValue == null ? currentValue != null : !initialValue.equals(currentValue);
    }

    private void setCurrentStateForEditableWidget() {
        final HierarchyBrowserView view = (HierarchyBrowserView) impl;
        view.clear();
        final HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        view.initWidgetContent(config);
        collectionNameNodeMap = currentState.getCollectionNameNodeMap();
        final WidgetDisplayConfig displayConfig = getDisplayConfig();
        final ArrayList<HierarchyBrowserItem> chosenItems = currentState.getChosenItems();

        DialogWindowConfig dialogWindowConfig = config.getDialogWindowConfig();
        final int popupWidth = getSizeFromString(dialogWindowConfig != null ?
                dialogWindowConfig.getWidth() : null, HierarchyBrowserMainPopup.DEFAULT_WIDTH);
        final int popupHeight = getSizeFromString(dialogWindowConfig != null ?
                dialogWindowConfig.getHeight() : null, HierarchyBrowserMainPopup.DEFAULT_HEIGHT);
        singleChoice = currentState.isSingleChoice();

        view.setChosenItems(chosenItems);
        ArrayList<Id> selectedIds = currentState.getIds();
        view.setSelectedIds(selectedIds);
        final boolean displayAsHyperlinks = isDisplayingHyperlinks();
        view.displayBaseWidget(displayConfig.getWidth(), displayConfig.getHeight());
        final ArrayList<HierarchyBrowserItem> copyOfItems = getCopyOfChosenItems(chosenItems);
        final ArrayList<Id> copyOfSelectedIds = new ArrayList<>(selectedIds);
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
        }
        handlerRegistration = view.addButtonClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                mainPopup = new HierarchyBrowserMainPopupBuilder().
                        setEventBus(localEventBus).
                        setChosenItems(copyOfItems).
                        setPopupWidth(popupWidth).
                        setPopupHeight(popupHeight).
                        setSelectionStyleConfig(config.getSelectionStyleConfig()).
                        setDisplayAsHyperlinks(displayAsHyperlinks).
                        setRootNodeLinkConfig(currentState.getRootNodeLinkConfig())
                        .setShouldDisplayTooltipButton(currentState.shouldDrawTooltipButton())
                        .createHierarchyBrowserMainPopup();
                mainPopup.createAndShowPopup(copyOfSelectedIds);
                mainPopup.addOkClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        HierarchyBrowserView view = (HierarchyBrowserView) impl;
                        view.setChosenItems(mainPopup.getChosenItems());
                        view.setSelectedIds(mainPopup.getSelectedIds());
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
                        copyOfSelectedIds.clear();
                        copyOfSelectedIds.addAll(view.getSelectedIds());

                    }
                });
                final NodeContentManager nodeContentManager = new FirstNodeContentManager(config,
                        mainPopup, createNewState().getIds(), collectionNameNodeMap);
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

    private void setCurrentStateForNoneEditableWidget() {
        List<HierarchyBrowserItem> hierarchyBrowserItems = currentState.getChosenItems();
        HierarchyBrowserNoneEditablePanel noneEditablePanel = (HierarchyBrowserNoneEditablePanel) impl;
        noneEditablePanel.cleanPanel();
        if (isDisplayingHyperlinks()) {
            noneEditablePanel.displayHyperlinks(hierarchyBrowserItems);
        } else {
            noneEditablePanel.displayHierarchyBrowserItems(hierarchyBrowserItems);

        }
        if (currentState.shouldDrawTooltipButton()) {
            noneEditablePanel.addShowTooltipLabel(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    fetchWidgetItems(null);
                }
            });
        }
    }

    @Override
    protected HierarchyBrowserWidgetState createNewState() {

        HierarchyBrowserWidgetState state = new HierarchyBrowserWidgetState();
        if (isEditable()) {
            HierarchyBrowserView view = (HierarchyBrowserView) impl;
            state.setChosenItems(view.getChosenItems());
            state.setSelectedIds(view.getSelectedIds());
        } else {
            HierarchyBrowserWidgetState previousState = getInitialData();
            ArrayList<HierarchyBrowserItem> chosenItems = previousState.getChosenItems();
            state.setChosenItems(chosenItems);
            state.setSelectedIds(previousState.getIds());

        }
        return state;
    }

    @Override
    public WidgetState getFullClientStateCopy() {
        if (!isEditable()) {
            return super.getFullClientStateCopy();
        }
        HierarchyBrowserWidgetState stateWithItems = createNewState();
        HierarchyBrowserWidgetState state = new HierarchyBrowserWidgetState();
        state.setChosenItems(stateWithItems.getChosenItems());
        HierarchyBrowserWidgetState initialState = new HierarchyBrowserWidgetState();
        state.setSingleChoice(initialState.isSingleChoice());
        state.setCollectionNameNodeMap(initialState.getCollectionNameNodeMap());
        state.setHierarchyBrowserConfig(initialState.getHierarchyBrowserConfig());
        state.setConstraints(initialState.getConstraints());
        state.setWidgetProperties(initialState.getWidgetProperties());
        return state;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        currentState = (HierarchyBrowserWidgetState) state;
        HierarchyBrowserConfig hierarchyBrowserConfig = currentState.getHierarchyBrowserConfig();
        SelectionStyleConfig selectionStyleConfig = hierarchyBrowserConfig.getSelectionStyleConfig();
        boolean displayAsHyperlinks = isDisplayingHyperlinks();
        boolean shouldDrawTooltipButton = currentState.shouldDrawTooltipButton();
        return new HierarchyBrowserView(selectionStyleConfig, localEventBus, displayAsHyperlinks, shouldDrawTooltipButton);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        currentState = (HierarchyBrowserWidgetState) state;
        HierarchyBrowserConfig hierarchyBrowserConfig = currentState.getHierarchyBrowserConfig();
        SelectionStyleConfig selectionStyleConfig = hierarchyBrowserConfig.getSelectionStyleConfig();
        return new HierarchyBrowserNoneEditablePanel(selectionStyleConfig, localEventBus);
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
        localEventBus.addHandler(HierarchyBrowserShowTooltipEvent.TYPE, this);
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
            mainPopup.handleAddingItem(event.getItem(), singleChoice);
        } else {
            mainPopup.handleRemovingItem(item);

        }

    }

    @Override
    public void onHierarchyBrowserItemClick(HierarchyBrowserItemClickEvent event) {
        final Id id = event.getItemId();
        final String collectionName = event.getCollectionName();
        if (BusinessUniverseConstants.UNDEFINED_COLLECTION_NAME.equalsIgnoreCase(collectionName)) {
            return;
        }
        NodeCollectionDefConfig nodeCollectionDefConfig = collectionNameNodeMap.get(collectionName);
        final String title = nodeCollectionDefConfig.getDomainObjectType();

        final FormPluginConfig config = new FormPluginConfig();
        config.setDomainObjectId(id);
        config.getPluginState().setEditable(false);
        final FormDialogBox noneEditableFormDialogBox = new FormDialogBox(title);
        config.getPluginState().setToggleEdit(true);
        config.getPluginState().setInCentralPanel(true);
        final FormPlugin noneEditableFormPlugin = noneEditableFormDialogBox.createFormPlugin(config, eventBus);
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
                final FormPlugin editableFormPlugin = editableFormDialogBox.createFormPlugin(config, eventBus);
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
                                localEventBus.fireEvent(new HyperlinkStateChangedEvent(id, null));

                            }
                        });
                        action.perform();

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
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        String collectionName = event.getCollectionName();
        Id parentId = event.getParentId();
        HierarchyBrowserWidgetState state = createNewState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new NewNodeContentManager(config, mainPopup, chosenIds,
                collectionName, parentId, collectionNameNodeMap);
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserRefreshClick(HierarchyBrowserRefreshClickEvent event) {
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        String parentCollectionName = event.getParentCollectionName();
        Id parentId = event.getParentId();
        HierarchyBrowserWidgetState state = createNewState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new RefreshNodeContentManager(config, mainPopup, chosenIds,
                parentCollectionName, parentId, "", collectionNameNodeMap);
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserSearchClick(HierarchyBrowserSearchClickEvent event) {
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        String parentCollectionName = event.getParentCollectionName();
        Id parentId = event.getParentId();
        String inputText = event.getInputText();
        HierarchyBrowserWidgetState state = createNewState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new RefreshNodeContentManager(config, mainPopup, chosenIds,
                parentCollectionName, parentId, inputText, collectionNameNodeMap);
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserScroll(HierarchyBrowserScrollEvent event) {
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        String parentCollectionName = event.getParentCollectionName();
        Id parentId = event.getParentId();
        int factor = event.getFactor();
        int offset = factor * config.getPageSize();
        String inputText = event.getInputText();
        HierarchyBrowserWidgetState state = createNewState();
        ArrayList<Id> chosenIds = state.getIds();
        NodeContentManager nodeContentManager = new ScrollNodeContentManager(config, mainPopup,
                chosenIds, parentCollectionName, parentId, inputText, offset, collectionNameNodeMap);
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserAddItemClick(HierarchyBrowserAddItemClickEvent event) {

        final Id parentId = event.getParentId();
        String domainObjectTypeToCreate = event.getEntry().getKey();
        String title = event.getEntry().getValue();

        final String parentCollectionName = event.getParentCollectionName();
        NodeCollectionDefConfig collectionDefConfig = getRequiredNodeConfig(parentCollectionName, domainObjectTypeToCreate);
        FormPluginConfig config = new FormPluginConfig();
        config.setDomainObjectTypeToCreate(domainObjectTypeToCreate);
        config.getPluginState().setEditable(true);
        FillParentOnAddConfig fillParentOnAddConfig = collectionDefConfig.getFillParentOnAddConfig();
        if (fillParentOnAddConfig != null) {
            HierarchyBrowserUpdaterContext hierarchyBrowserUpdaterContext = new HierarchyBrowserUpdaterContext(fillParentOnAddConfig,
                    parentId);
            config.setUpdaterContext(hierarchyBrowserUpdaterContext);
            config.setDomainObjectUpdatorComponent("hierarchy-browser-do-updater");
        }
        final FormDialogBox createItemDialogBox = new FormDialogBox(title);
        final FormPlugin createFormPlugin = createItemDialogBox.createFormPlugin(config, eventBus);
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
                        localEventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(parentId, parentCollectionName));
                    }
                });
                action.perform();

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
        PopupPanel popupPanel = event.getPopupPanel();
        if (popupPanel != null) {
            popupPanel.hide();
            localEventBus.fireEvent(new HierarchyBrowserShowTooltipEvent(null));
            return;
        }
        Id id = event.getId();
        String collectionName = event.getCollectionName();
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        if (isEditable()) {
            HierarchyBrowserView view = (HierarchyBrowserView) impl;
            HierarchyBrowserHyperlinkContentManager hyperlinkContentManager =
                    new EditableHierarchyBrowserHyperlinkContentManager(id, collectionName, config, view, mainPopup,
                            collectionNameNodeMap);
            hyperlinkContentManager.updateHyperlink();
        } else {
            HierarchyBrowserWidgetState state = createNewState();
            List<HierarchyBrowserItem> items = state.getChosenItems();
            HierarchyBrowserNoneEditablePanel noneEditablePanel =
                    (HierarchyBrowserNoneEditablePanel) impl;
            HierarchyBrowserHyperlinkContentManager hyperlinkContentManager =
                    new NoneEditableHierarchyBrowserHyperlinkContentManager(id, collectionName,
                            config, noneEditablePanel, items, collectionNameNodeMap);
            hyperlinkContentManager.updateHyperlink();
        }

    }

    private boolean isDisplayingHyperlinks() {
        DisplayValuesAsLinksConfig displayValuesAsLinksConfig = currentState.getHierarchyBrowserConfig()
                .getDisplayValuesAsLinksConfig();
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
            List<HierarchyBrowserItem> itemsInDialogWindow = mainPopup.getChosenItems();
            itemsInDialogWindow.clear();
            HierarchyBrowserView view = (HierarchyBrowserView) impl;
            itemsInDialogWindow.addAll(view.getChosenItems());
        }
    }

    private NodeCollectionDefConfig getRequiredNodeConfig(String collectionName, String domainObjectType) {
        NodeCollectionDefConfig rootNodeCollectionDefConfig = collectionNameNodeMap.get(collectionName);
        List<NodeCollectionDefConfig> nodeCollectionDefConfigs = rootNodeCollectionDefConfig.getNodeCollectionDefConfigs();
        for (NodeCollectionDefConfig nodeCollectionDefConfig : nodeCollectionDefConfigs) {
            if (domainObjectType.equalsIgnoreCase(nodeCollectionDefConfig.getDomainObjectType())) {
                return nodeCollectionDefConfig;
            }
        }
        return rootNodeCollectionDefConfig;
    }

    @Override
    public void onHierarchyBrowserShowTooltip(HierarchyBrowserShowTooltipEvent event) {
        HierarchyBrowserItemsView itemsView = event.getItemsView();
        fetchWidgetItems(itemsView);
    }

    private void fetchWidgetItems(final HierarchyBrowserItemsView itemsView) {
        final HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        final HierarchyBrowserTooltipRequest request = new HierarchyBrowserTooltipRequest(config, currentState.getIds());
        Command command = new Command("fetchWidgetItems", getName(), request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                HierarchyBrowserTooltipResponse response = (HierarchyBrowserTooltipResponse) result;
                ArrayList<HierarchyBrowserItem> items = response.getItems();
                SelectionStyleConfig styleConfig = config.getSelectionStyleConfig();

                if (itemsView == null) {
                    HierarchyBrowserNoneEditableTooltip tooltip = new HierarchyBrowserNoneEditableTooltip(styleConfig, localEventBus, isDisplayingHyperlinks());
                    TooltipSizer.setWidgetBounds(config, tooltip);
                    tooltip.displayItems(items);
                    tooltip.showRelativeTo(impl);
                } else {
                    HierarchyBrowserEditableTooltip tooltip = new HierarchyBrowserEditableTooltip(styleConfig, localEventBus, isDisplayingHyperlinks());
                    TooltipSizer.setWidgetBounds(config, tooltip);
                    ArrayList<Id> selectedIds = response.getSelectedIds();
                    tooltip.displayItems(items, selectedIds);
                    tooltip.showRelativeTo(itemsView);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });
    }

}