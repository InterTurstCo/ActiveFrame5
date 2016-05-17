package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.api.client.Predicate;
import ru.intertrust.cm.core.gui.api.client.event.OpenHyperlinkInSurferEvent;
import ru.intertrust.cm.core.gui.api.client.event.PluginCloseListener;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.ActionSuccessListener;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.*;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.EventBlocker;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.FormDialogBox;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.TooltipSizer;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.form.widget.hierarchybrowser.HierarchyBrowserUtil;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.*;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 13:15
 */
@ComponentName(HierarchyBrowserWidget.COMPONENT_NAME)
public class HierarchyBrowserWidget extends BaseWidget implements HierarchyBrowserCheckBoxUpdateEventHandler,
        HierarchyBrowserItemClickEventHandler, HierarchyBrowserNodeClickEventHandler,
        HierarchyBrowserRefreshClickEventHandler, HierarchyBrowserSearchClickEventHandler,
        HierarchyBrowserScrollEventHandler, HierarchyBrowserAddItemClickEventHandler,
        HierarchyBrowserHyperlinkStateUpdatedEventHandler, HierarchyBrowserCloseDialogEventHandler,
        HierarchyBrowserShowTooltipEventHandler {
    public static final String COMPONENT_NAME = "hierarchy-browser";
    protected HierarchyBrowserWidgetState currentState;
    private HierarchyBrowserMainPopup mainPopup;
    protected ResettableEventBus localEventBus = new ResettableEventBus(new SimpleEventBus());
    private HandlerRegistration handlerRegistration;
    private Set<Id> initiallySelectedIds = new HashSet<>();

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
        initiallySelectedIds.clear();
        if (this.currentState.getIds() != null) {
            initiallySelectedIds.addAll(this.currentState.getIds());
        }
    }

    @Override
    protected boolean isChanged() {
        final Set<Id> currentValue = currentState.getIds() != null ? new HashSet(currentState.getIds()) : null;
        return currentValue == null ? !initiallySelectedIds.isEmpty() : !currentValue.equals(initiallySelectedIds);
    }

    @Override
    public Object getValue() {
        return currentState.getChosenItems();
    }

    private void setCurrentStateForEditableWidget() {
        final HierarchyBrowserView view = (HierarchyBrowserView) impl;
        view.clear();
        final HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();

        final WidgetDisplayConfig displayConfig = getDisplayConfig();
        view.initWidgetContent(config, new ClearButtonClickHandler());

        view.displayBaseWidget(displayConfig.getWidth(), displayConfig.getHeight(), currentState.getChosenItems(),
                currentState.isTooltipAvailable());
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
        }
        handlerRegistration = view.addButtonClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                currentState.initTemporaryState();
                mainPopup = new HierarchyBrowserMainPopup(localEventBus, currentState);
                mainPopup.createAndShowPopup(currentState.getTemporaryChosenItems());
                mainPopup.addOkClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        currentState.applyChanges();
                        view.displayChosenItems(currentState.getChosenItems(), currentState.isTooltipAvailable());
                        disposeOfMainPopup();

                    }
                });
                mainPopup.addCancelClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        disposeOfMainPopup();
                    }
                });
                mainPopup.addCancelListener(new EventListener() {
                    @Override
                    public void onBrowserEvent(Event event) {
                        disposeOfMainPopup();
                    }
                });
                final NodeContentManager nodeContentManager = new NodeContentManagerBuilder()
                        .withConfig(config)
                        .withMainPopup(mainPopup)
                        .withChosenIds(currentState.getIds())
                        .withCollectionNameNodeMap(currentState.getCollectionNameNodeMap())
                        .withWidgetsContainer(getContainer())
                        .withWidgetIdComponentNames(currentState.getWidgetIdComponentNames())
                        .buildFirstNodeContentManager();
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

        if (HierarchyBrowserUtil.isDisplayingHyperlinks(currentState)) {
            noneEditablePanel.display(hierarchyBrowserItems, currentState.isTooltipAvailable());
        } else {
            noneEditablePanel.displayHierarchyBrowserItems(hierarchyBrowserItems, currentState.isTooltipAvailable());

        }

    }

    @Override
    protected HierarchyBrowserWidgetState createNewState() {
        HierarchyBrowserWidgetState state = new HierarchyBrowserWidgetState();
        ArrayList<HierarchyBrowserItem> chosenItems = currentState.getChosenItems();
        state.setChosenItems(chosenItems);
        state.setSelectedIds(currentState.getIds());

        return state;
    }

    @Override
    public WidgetState getFullClientStateCopy() {
        if (!isEditable()) {
            return super.getFullClientStateCopy();
        }
        HierarchyBrowserWidgetState state = new HierarchyBrowserWidgetState();
        state.setChosenItems(currentState.getChosenItems());
        state.setSelectedIds(currentState.getIds());
        state.setSingleChoice(currentState.isSingleChoice());
        state.setCollectionNameNodeMap(currentState.getCollectionNameNodeMap());
        state.setHierarchyBrowserConfig(currentState.getHierarchyBrowserConfig());
        state.setConstraints(currentState.getConstraints());
        state.setWidgetProperties(currentState.getWidgetProperties());
        return state;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        currentState = (HierarchyBrowserWidgetState) state;
        HierarchyBrowserConfig hierarchyBrowserConfig = currentState.getHierarchyBrowserConfig();
        SelectionStyleConfig selectionStyleConfig = hierarchyBrowserConfig.getSelectionStyleConfig();
        boolean displayAsHyperlinks = HierarchyBrowserUtil.isDisplayingHyperlinks(currentState);
        HierarchyBrowserView hierarchyBrowserView = new HierarchyBrowserView(selectionStyleConfig, localEventBus, displayAsHyperlinks);
        return hierarchyBrowserView;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        currentState = (HierarchyBrowserWidgetState) state;
        HierarchyBrowserConfig hierarchyBrowserConfig = currentState.getHierarchyBrowserConfig();
        SelectionStyleConfig selectionStyleConfig = hierarchyBrowserConfig.getSelectionStyleConfig();
        return new HierarchyBrowserNoneEditablePanel(selectionStyleConfig, localEventBus,
                HierarchyBrowserUtil.isDisplayingHyperlinks(currentState));

    }

    protected void registerEventsHandling() {
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

    @Override
    public void onHierarchyBrowserCheckBoxUpdate(HierarchyBrowserCheckBoxUpdateEvent event) {
        final HierarchyBrowserItem item = event.getItem();
        if (event.isRemoveItemOnly()) {
            if (item != null) {
                currentState.handleRemovingItem(event.getItem());
            }
            refreshView();
            return;
        }
        boolean singleChoiceCase = handleAsSingleChoice(event);
        if (singleChoiceCase) {
            return;
        }

        int delta = item.isChosen() ? 1 : 0;
        if (HierarchyBrowserUtil.shouldInitializeTooltip(currentState, delta)) {
            fetchWidgetItems(new TooltipCallback() {
                @Override
                public void perform() {
                    handleItemChangeState(item);

                }
            });
        } else {
            handleItemChangeState(item);
        }

    }

    private boolean handleAsSingleChoice(HierarchyBrowserCheckBoxUpdateEvent event) {
        boolean result = false;
        HierarchyBrowserItem item = event.getItem();
        if ((currentState.isSingleChoice())) {
            result = true;
            currentState.handleCommonSingleChoice(item);
            localEventBus.fireEvent(new HierarchyBrowserChangeSelectionEvent(item, currentState.isSingleChoice()));
            refreshView();
        } else if (item.isSingleChoice() != null && item.isSingleChoice()) {
            result = true;
            currentState.handleNodeSingleChoice(item);
            localEventBus.fireEvent(new HierarchyBrowserChangeSelectionEvent(item, currentState.isSingleChoice()));

        }

        return result;

    }

    private void handleItemChangeState(HierarchyBrowserItem item) {
        boolean chosen = item.isChosen();
        if (chosen) {
            currentState.handleAddingItem(item);
        } else {
            localEventBus.fireEvent(new HierarchyBrowserChangeSelectionEvent(item, currentState.isSingleChoice()));
            currentState.handleRemovingItem(item);
        }
        refreshView();

    }

    private void refreshView() {
        if (mainPopup != null) {
            mainPopup.displayChosenItems(currentState.getTemporaryChosenItems(), currentState.isTooltipAvailable());
        } else {
            HierarchyBrowserView view = (HierarchyBrowserView) impl;
            view.displayChosenItems(currentState.getChosenItems(), currentState.isTooltipAvailable());
        }
    }

    @Override
    public void onHierarchyBrowserItemClick(HierarchyBrowserItemClickEvent event) {
        HierarchyBrowserItem item = event.getItem();
        String collectionName = item.getNodeCollectionName();
        if (BusinessUniverseConstants.UNDEFINED_COLLECTION_NAME.equalsIgnoreCase(collectionName)) {
            return;
        }
        Id id = item.getId();
        HierarchyBrowserDisplay display = event.getHyperlinkDisplay();
        boolean tooltipContent = event.isTooltipContent();
        NodeCollectionDefConfig nodeCollectionDefConfig = currentState.getCollectionNameNodeMap().get(collectionName);
        boolean modalWindow = HierarchyBrowserUtil.isModalWindow(currentState.getHierarchyBrowserConfig().getDisplayValuesAsLinksConfig(),
                nodeCollectionDefConfig.getDisplayValuesAsLinksConfig());
        final HierarchyBrowserHyperlinkStateUpdatedEvent updateEvent =
                new HierarchyBrowserHyperlinkStateUpdatedEvent(id, collectionName, display, tooltipContent);
        LinkedFormMappingConfig linkedFormMappingConfig = nodeCollectionDefConfig.getLinkedFormMappingConfig();
        List<LinkedFormConfig> linkedFormConfigs = GuiUtil.getLinkedFormConfigs(null, linkedFormMappingConfig);
        if (modalWindow) {
            String domainObjectType = item.getDomainObjectType();
            FormPluginConfig config = GuiUtil.createFormPluginConfig(id, nodeCollectionDefConfig, domainObjectType, false);
            String modalHeight = GuiUtil.getModalHeight(domainObjectType, linkedFormMappingConfig, null);
            String modalWidth = GuiUtil.getModalWidth(domainObjectType, linkedFormMappingConfig, null);
            String title = currentState.getHyperlinkPopupTitle(collectionName, domainObjectType);
            boolean resizable = GuiUtil.isFormResizable(domainObjectType, linkedFormMappingConfig, null);
            FormDialogBox formDialogBox = new FormDialogBox(title, modalWidth, modalHeight, resizable);
            formDialogBox.createFormPlugin(config, eventBus);
            initFormDialogButtons(formDialogBox, id, config, linkedFormConfigs, updateEvent);
        } else {
            PluginCloseListener closeListener = createPluginCloseListener(updateEvent);
            Application.getInstance().getEventBus().fireEvent(new OpenHyperlinkInSurferEvent(id, linkedFormConfigs, closeListener, true));
        }

    }

    private void initFormDialogButtons(final FormDialogBox formDialogBox, final Id id, final FormPluginConfig config,
                                       final List<LinkedFormConfig> linkedFormConfigs, final HierarchyBrowserHyperlinkStateUpdatedEvent updateEvent) {
        formDialogBox.initButton(LocalizeUtil.get(OPEN_IN_FULL_WINDOW_KEY, OPEN_IN_FULL_WINDOW),
                createOpenInFullWindowClickHandler(formDialogBox, id, linkedFormConfigs, updateEvent, false));
        formDialogBox.initButton(LocalizeUtil.get(EDIT_BUTTON_KEY, EDIT_BUTTON), createEditFormClickHandler(formDialogBox,
                id, config, linkedFormConfigs, updateEvent));
        formDialogBox.initButton(LocalizeUtil.get(CANCELLATION_BUTTON_KEY, CANCELLATION_BUTTON),
                createCancelClickHandler(formDialogBox));
    }

    private ClickHandler createEditFormClickHandler(final FormDialogBox formDialogBox, final Id id,
                                                    final FormPluginConfig config,final List<LinkedFormConfig> linkedFormConfigs,
                                                    final HierarchyBrowserHyperlinkStateUpdatedEvent updateEvent) {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formDialogBox.clearButtons();
                config.getPluginState().setEditable(true);
                final FormPlugin editableFormPlugin = formDialogBox.createFormPlugin(config, eventBus);
                formDialogBox.initButton(LocalizeUtil.get(OPEN_IN_FULL_WINDOW_KEY, OPEN_IN_FULL_WINDOW),
                        createOpenInFullWindowClickHandler(formDialogBox, id, linkedFormConfigs, updateEvent, true));
                formDialogBox.initButton(LocalizeUtil.get(SAVE_BUTTON_KEY, SAVE_BUTTON), new ClickHandler() {
                    @Override
                    public void onClick(final ClickEvent event) {
                        final SaveAction action = GuiUtil.createSaveAction(editableFormPlugin, id, true);
                        action.addActionSuccessListener(new ActionSuccessListener() {
                            @Override
                            public void onSuccess() {
                                formDialogBox.hide();
                                localEventBus.fireEvent(updateEvent);

                            }
                        });
                        action.perform();

                    }
                });
                formDialogBox.initButton(LocalizeUtil.get(CANCEL_BUTTON_KEY, CANCEL_BUTTON), createCancelClickHandler(formDialogBox));

            }

        };
    }

    private ClickHandler createOpenInFullWindowClickHandler(final FormDialogBox formDialogBox, final Id id,
                                                            final List<LinkedFormConfig> linkedFormConfigs,
                                                            final HierarchyBrowserHyperlinkStateUpdatedEvent updateEvent, final boolean editable) {
        final PluginCloseListener pluginCloseListener = createPluginCloseListener(updateEvent);
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Application.getInstance().getEventBus().fireEvent(new OpenHyperlinkInSurferEvent(id, linkedFormConfigs, pluginCloseListener, editable));
                formDialogBox.hide();
                if (mainPopup != null) {
                    mainPopup.hidePopup();
                }
            }
        };
    }

    private PluginCloseListener createPluginCloseListener(final HierarchyBrowserHyperlinkStateUpdatedEvent updateEvent) {
        return new PluginCloseListener() {
            @Override
            public void onPluginClose() {
                localEventBus.fireEvent(updateEvent);
            }
        };
    }

    private ClickHandler createCancelClickHandler(final FormDialogBox formDialogBox) {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formDialogBox.hide();
            }
        };
    }

    @Override
    public void onHierarchyBrowserNodeClick(HierarchyBrowserNodeClickEvent event) {
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        final String collectionName = event.getCollectionName();
        Id parentId = event.getParentId();
        handleRecursionDeepness(collectionName, event.getRecursionDeepness(), 0);
        ArrayList<Id> chosenIds = currentState.getTemporarySelectedIds();
        NodeContentManager nodeContentManager = new NodeContentManagerBuilder()
                .withConfig(config)
                .withMainPopup(mainPopup)
                .withChosenIds(chosenIds)
                .withCollectionName(collectionName)
                .withParentId(parentId)
                .withCollectionNameNodeMap(currentState.getCollectionNameNodeMap())
                .withWidgetsContainer(getContainer())
                .withWidgetIdComponentNames(currentState.getWidgetIdComponentNames())
                .buildNewNodeContentManager();
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserRefreshClick(HierarchyBrowserRefreshClickEvent event) {
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        String parentCollectionName = event.getParentCollectionName();
        Id parentId = event.getParentId();
        ArrayList<Id> chosenIds = currentState.getTemporarySelectedIds();
        handleRecursionDeepness(parentCollectionName, event.getRecursionDeepness(), 1);
        NodeContentManager nodeContentManager = new NodeContentManagerBuilder()
                .withConfig(config)
                .withMainPopup(mainPopup)
                .withChosenIds(chosenIds)
                .withCollectionName(parentCollectionName)
                .withParentId(parentId)
                .withInputText(event.getFilterText())
                .withCollectionNameNodeMap(currentState.getCollectionNameNodeMap())
                .withWidgetsContainer(getContainer())
                .withWidgetIdComponentNames(currentState.getWidgetIdComponentNames())
                .buildRefreshNodeContentManager();
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserSearchClick(HierarchyBrowserSearchClickEvent event) {
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        final String parentCollectionName = event.getParentCollectionName();
        Id parentId = event.getParentId();
        String inputText = event.getInputText();
        ArrayList<Id> chosenIds = currentState.getTemporarySelectedIds();
        handleRecursionDeepness(parentCollectionName, event.getRecursionDeepness(), 1);
        NodeContentManager nodeContentManager = new NodeContentManagerBuilder()
                .withConfig(config)
                .withMainPopup(mainPopup)
                .withChosenIds(chosenIds)
                .withCollectionName(parentCollectionName)
                .withParentId(parentId)
                .withInputText(inputText)
                .withCollectionNameNodeMap(currentState.getCollectionNameNodeMap())
                .withWidgetsContainer(getContainer())
                .withWidgetIdComponentNames(currentState.getWidgetIdComponentNames())
                .buildRefreshNodeContentManager();
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
        ArrayList<Id> chosenIds = currentState.getTemporarySelectedIds();
        handleRecursionDeepness(parentCollectionName, event.getRecursionDeepness(), 1);
        NodeContentManager nodeContentManager = new NodeContentManagerBuilder()
                .withConfig(config)
                .withMainPopup(mainPopup)
                .withChosenIds(chosenIds)
                .withCollectionName(parentCollectionName)
                .withParentId(parentId)
                .withInputText(inputText)
                .withOffset(offset)
                .withCollectionNameNodeMap(currentState.getCollectionNameNodeMap())
                .withWidgetsContainer(getContainer())
                .withWidgetIdComponentNames(currentState.getWidgetIdComponentNames())
                .buildScrollNodeContentManager();

        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserAddItemClick(HierarchyBrowserAddItemClickEvent event) {

        final Id parentId = event.getParentId();
        NodeCollectionDefConfig nodeConfig = event.getNodeConfig();
        String domainObjectTypeToCreate = event.getDomainObjectType();
        final String parentCollectionName = event.getParentCollectionName();
        final int recursionDeepness = event.getRecursionDeepness();
        FormPluginConfig config = GuiUtil.createNewFormPluginConfig(domainObjectTypeToCreate,
                nodeConfig.getLinkedFormMappingConfig(), getContainer(), nodeConfig.getParentWidgetIdsForNewFormMap());
        FillParentOnAddConfig fillParentOnAddConfig = nodeConfig.getFillParentOnAddConfig();
        if (fillParentOnAddConfig != null) {
            HierarchyBrowserUpdaterContext hierarchyBrowserUpdaterContext = new HierarchyBrowserUpdaterContext(fillParentOnAddConfig,
                    parentId);
            config.setUpdaterContext(hierarchyBrowserUpdaterContext);
            config.setDomainObjectUpdatorComponent("hierarchy-browser-do-updater");
        }
        PopupTitlesHolder popupTitlesHolder = nodeConfig.getDoTypeTitlesMap().get(domainObjectTypeToCreate.toLowerCase());
        String newObjectTitle = popupTitlesHolder == null ? null : popupTitlesHolder.getTitleNewObject();
        LinkedFormMappingConfig linkedFormMappingConfig = nodeConfig.getLinkedFormMappingConfig();
        final String modalHeight = GuiUtil.getModalHeight(domainObjectTypeToCreate, linkedFormMappingConfig, null);
        final String modalWidth = GuiUtil.getModalWidth(domainObjectTypeToCreate, linkedFormMappingConfig, null);
        final boolean resizable = GuiUtil.isFormResizable(domainObjectTypeToCreate, linkedFormMappingConfig, null);
        final FormDialogBox createItemDialogBox = new FormDialogBox(newObjectTitle, modalWidth, modalHeight, resizable);
        final FormPlugin createFormPlugin = createItemDialogBox.createFormPlugin(config, eventBus, getContainer());
        createItemDialogBox.initButton(LocalizeUtil.get(SAVE_BUTTON_KEY, SAVE_BUTTON), new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                final SaveAction action = GuiUtil.createSaveAction(createFormPlugin, parentId, true);
                action.addActionSuccessListener(new ActionSuccessListener() {
                    @Override
                    public void onSuccess() {
                        createItemDialogBox.hide();
                        localEventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(parentId, parentCollectionName, recursionDeepness));
                    }
                });
                action.perform();

            }
        });
        createItemDialogBox.initButton(LocalizeUtil.get(CANCELLATION_BUTTON_KEY, CANCELLATION_BUTTON), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createItemDialogBox.hide();
            }
        });
    }

    @Override
    public void onHierarchyBrowserHyperlinkStateUpdatedEvent(final HierarchyBrowserHyperlinkStateUpdatedEvent event) {
        Id id = event.getId();
        String collectionName = event.getCollectionName();
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();

        HierarchyBrowserHyperlinkContentManager hyperlinkContentManager =
                new EditableHierarchyBrowserHyperlinkContentManager(id, collectionName, config,
                        currentState.getCollectionNameNodeMap()) {
                    @Override
                    protected void handleHyperlinkUpdate(HierarchyBrowserItem updatedItem) {
                        HierarchyBrowserDisplay hyperlinkDisplay = event.getHyperlinkDisplay();
                        if (hyperlinkDisplay == null) { //event generated when node item changes
                            handleNodeItemChanges(updatedItem);
                        } else {
                            handleHyperlinkItemChanges(hyperlinkDisplay, updatedItem, event.isTooltipContent());
                        }

                    }
                };
        hyperlinkContentManager.updateHyperlink();
    }

    private void handleRecursionDeepness(final String collectionName, int recursionDeepness, int delta) {
        NodeCollectionDefConfig nodeDefConfig = currentState.getCollectionNameNodeMap().get(collectionName);
        if (nodeDefConfig.isChildrenRecursive()) {
            List<NodeCollectionDefConfig> nodeDefConfigs = nodeDefConfig.getNodeCollectionDefConfigs();
            NodeCollectionDefConfig recursiveChildNode = GuiUtil.find(nodeDefConfigs, new Predicate<NodeCollectionDefConfig>() {
                @Override
                public boolean evaluate(NodeCollectionDefConfig input) {
                    return collectionName.equalsIgnoreCase(input.getCollection());
                }
            });
            recursiveChildNode.setRecursiveDeepness(recursionDeepness - delta);
        }
    }

    private void handleNodeItemChanges(HierarchyBrowserItem updatedItem) {
        handleHyperlinkItemChanges(mainPopup, updatedItem, false);

    }

    private void handleHyperlinkItemChanges(HierarchyBrowserDisplay hyperlinkDisplay,
                                            HierarchyBrowserItem updatedItem, boolean tooltipContent) {
        boolean shouldDrawTooltipButton = currentState.isTooltipAvailable();
        List<HierarchyBrowserItem> itemsToRedraw = tooltipContent ? currentState.getTooltipChosenItems()
                : currentState.getCurrentItems();
        HierarchyBrowserUtil.handleUpdateChosenItem(updatedItem, itemsToRedraw);

        hyperlinkDisplay.display(itemsToRedraw, shouldDrawTooltipButton);
        if (mainPopup != null) {
            mainPopup.refreshNode(updatedItem);
            //Main content should be updated accordingly, saving hyperlink DO could not be reverted
            List<HierarchyBrowserItem> mainContent = currentState.getChosenItems();
            boolean updated = HierarchyBrowserUtil.handleUpdateChosenItem(updatedItem, mainContent);
            if (updated) {
                ((HierarchyBrowserDisplay) impl).display(mainContent, shouldDrawTooltipButton);
            }
        }
    }

    @Override
    public void onHierarchyBrowserCloseDialogEvent(HierarchyBrowserCloseDialogEvent event) {
        if (mainPopup != null) {
            disposeOfMainPopup();
        }
    }

    private void disposeOfMainPopup() {
        mainPopup.hidePopup();
        currentState.resetChanges();
        mainPopup = null;
        localEventBus.removeHandlers();
        registerEventsHandling();

    }

    @Override
    public void onHierarchyBrowserShowTooltip(HierarchyBrowserShowTooltipEvent event) {
        HierarchyBrowserItemsView itemsView = event.getItemsView();
        onTooltipElementClick(itemsView);
    }

    private void fetchWidgetItems(final TooltipCallback tooltipCallback) {
        final HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        ComplexFiltersParams filtersParams =
                GuiUtil.createComplexFiltersParams(getContainer());
        final HierarchyBrowserTooltipRequest request = new HierarchyBrowserTooltipRequest(config, currentState.getIds(), filtersParams);
        Command command = new Command("fetchWidgetItems", getName(), request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                HierarchyBrowserTooltipResponse response = (HierarchyBrowserTooltipResponse) result;
                ArrayList<HierarchyBrowserItem> items = response.getItems();
                currentState.setTooltipChosenItems(items);
                tooltipCallback.perform();

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });
    }

    private void onTooltipElementClick(final HierarchyBrowserItemsView itemsView) {

        if (currentState.getTooltipChosenItems() == null) {
            final HandlerRegistration blockerHandler = Event.addNativePreviewHandler(new EventBlocker(impl));
            fetchWidgetItems(new TooltipCallback() {
                @Override
                public void perform() {
                    blockerHandler.removeHandler();
                    createAndShowTooltip(itemsView, currentState.getTooltipChosenItems());
                }
            });
        } else {
            createAndShowTooltip(itemsView, currentState.getTooltipChosenItems());

        }
    }

    private void createAndShowTooltip(HierarchyBrowserItemsView itemsView, ArrayList<HierarchyBrowserItem> items) {
        final HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        SelectionStyleConfig styleConfig = config.getSelectionStyleConfig();

        if (itemsView == null) {
            HierarchyBrowserNoneEditableTooltip tooltip = new HierarchyBrowserNoneEditableTooltip(styleConfig,
                    localEventBus, HierarchyBrowserUtil.isDisplayingHyperlinks(currentState));
            TooltipSizer.setWidgetBounds(config, tooltip);
            tooltip.displayItems(items);
            tooltip.showRelativeTo(impl);
        } else {
            HierarchyBrowserEditableTooltip tooltip = new HierarchyBrowserEditableTooltip(styleConfig, localEventBus,
                    HierarchyBrowserUtil.isDisplayingHyperlinks(currentState));
            TooltipSizer.setWidgetBounds(config, tooltip);
            tooltip.displayItems(items);
            tooltip.showRelativeTo(itemsView);
        }
    }


    private class ClearButtonClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            currentState.clearState();
            HierarchyBrowserView view = (HierarchyBrowserView) impl;
            view.displayChosenItems(currentState.getChosenItems(), false);

        }
    }

}


