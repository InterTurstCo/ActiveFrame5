package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.event.collection.CollectionChangeSelectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.form.ParentTabSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.form.ParentTabSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.tablebrowser.OpenCollectionRequestEvent;
import ru.intertrust.cm.core.gui.impl.client.event.tablebrowser.OpenCollectionRequestEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.ShowTooltipEvent;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.WidgetItemRemoveEvent;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.WidgetItemRemoveEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkDisplay;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkediting.LinkCreatorWidget;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPluginView;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.impl.client.util.LinkUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.filters.WidgetIdComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.plugin.ExpandHierarchicalCollectionData;
import ru.intertrust.cm.core.gui.model.plugin.HierarchicalCollectionData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.*;

import static ru.intertrust.cm.core.gui.model.util.WidgetUtil.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 11:15
 */
@ComponentName("table-browser")
public class TableBrowserWidget extends LinkCreatorWidget implements WidgetItemRemoveEventHandler,
        HyperlinkStateChangedEventHandler, HierarchicalCollectionEventHandler, OpenCollectionRequestEventHandler,
        CheckBoxFieldUpdateEventHandler, ParentTabSelectedEventHandler {
    private static int MAGIC_NUMBER = 26;
    protected CollectionDialogBox dialogBox;
    private ViewHolder viewHolder;
    private List<BreadCrumbItem> breadCrumbItems = new ArrayList<>();

    private HandlerRegistration expandHierarchyRegistration;
    private HandlerRegistration checkBoxRegistration;
    private HandlerRegistration rowSelectedRegistration;
    private CollectionPlugin collectionPlugin;
    private String collectionName;
    protected CollectionViewerConfig initialCollectionViewerConfig;
    private Set<Id> initiallySelectedIds = new HashSet<>();
    protected TableBrowserState currentState;

    public TableBrowserWidget() {
        super();
    }

    @Override
    public void setCurrentState(WidgetState state) {
        initialData = state;
        currentState = (TableBrowserState) state;
        currentState.resetTemporaryState();
        viewHolder.setContent(state);
        initiallySelectedIds.clear();
        if (currentState.getSelectedIds() != null) {
            initiallySelectedIds.addAll(currentState.getSelectedIds());
        }
    }

    @Override
    protected boolean isChanged() {
        Set<Id> currentlySelectedIds = currentState.getSelectedIds();
        return currentlySelectedIds == null ? !initiallySelectedIds.isEmpty() : !currentlySelectedIds.equals(initiallySelectedIds);
    }


    @Override
    public TableBrowserState createNewState() {
        TableBrowserState state = new TableBrowserState();
        TableBrowserState previousState = getInitialData();
        state.setSelectedIds(previousState.getSelectedIds());
        return state;
    }

    @Override
    public WidgetState getFullClientStateCopy() {
        if (!isEditable()) {
            return super.getFullClientStateCopy();
        }
        TableBrowserState stateWithItems = createNewState();
        TableBrowserState fullClientState = new TableBrowserState();
        fullClientState.setListValues(stateWithItems.getListValues());
        fullClientState.setSelectedIds(stateWithItems.getSelectedIds());
        TableBrowserState initialState = getInitialData();
        fullClientState.setSingleChoice(initialState.isSingleChoice());
        fullClientState.setTableBrowserConfig(initialState.getTableBrowserConfig());
        fullClientState.setDomainFieldOnColumnNameMap(initialState.getDomainFieldOnColumnNameMap());
        fullClientState.setWidgetProperties(initialState.getWidgetProperties());
        fullClientState.setConstraints(initialState.getConstraints());
        return fullClientState;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        commonInitialization(state);
        localEventBus.addHandler(WidgetItemRemoveEvent.TYPE, this);
        localEventBus.addHandler(CheckBoxFieldUpdateEvent.TYPE, this);
        viewHolder = new TableBrowserViewsBuilder()
                .withEventBus(localEventBus)
                .withEditable(true)
                .withState(currentState)
                .withCreateLinkedItemButton(getCreateButton())
                .withHasLinkedFormMappings(this)
                .withOpenCollectionButtonHandler(new OpenCollectionClickHandler())
                .withWidgetDisplayConfig(getDisplayConfig())
                .withParentWidget(this)
                .buildViewHolder();
        return viewHolder.getWidget();
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        commonInitialization(state);
        viewHolder = new TableBrowserViewsBuilder()
                .withState(currentState)
                .withEventBus(localEventBus)
                .withEditable(false)
                .withHasLinkedFormMappings(this)
                .withWidgetDisplayConfig(getDisplayConfig())
                .withHeight(calculateDynamicHeight(state))
                .buildViewHolder();
        return viewHolder.getWidget();

    }

    private Integer calculateDynamicHeight(WidgetState state) {
        if ((((TableBrowserState) state).getTableBrowserConfig().getPageSize() == 0 || ((TableBrowserState) state).getTableBrowserConfig().getPageSize() <= 0
        ) && (getDisplayConfig() == null || getDisplayConfig().getHeight()==null) && (((TableBrowserState) state).getStretched())) {
            if (((TableBrowserState) state).getSelectedIds().size() > 0) {
                return ((TableBrowserState) state).getSelectedIds().size() * MAGIC_NUMBER;
            }
        }
        return null;
    }

    private void commonInitialization(WidgetState state) {
        currentState = (TableBrowserState) state;
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        localEventBus.addHandler(ShowTooltipEvent.TYPE, this);
        localEventBus.addHandler(OpenCollectionRequestEvent.TYPE, this);
        if (currentState.isTableView()) {
            eventBus.addHandler(ParentTabSelectedEvent.TYPE, this);
        }

    }

    @Override
    public Component createNew() {
        return new TableBrowserWidget();
    }


    protected CollectionViewerConfig initCollectionConfig(Boolean displayOnlyChosenIds, Boolean displayCheckBoxes) {
        CollectionViewerConfig collectionViewerConfig = new CollectionViewerConfig();
        CollectionViewRefConfig collectionViewRefConfig = new CollectionViewRefConfig();
        TableBrowserConfig tableBrowserConfig = currentState.getTableBrowserConfig();
        TableBrowserParams tableBrowserParams = createTableBrowserParams(displayOnlyChosenIds, displayCheckBoxes);
        collectionViewerConfig.setTableBrowserParams(tableBrowserParams);
        collectionViewRefConfig.setName(tableBrowserConfig.getCollectionViewRefConfig().getName());
        CollectionRefConfig collectionRefConfig = new CollectionRefConfig();
        collectionRefConfig.setName(tableBrowserConfig.getCollectionRefConfig().getName());
        DefaultSortCriteriaConfig defaultSortCriteriaConfig = tableBrowserConfig.getDefaultSortCriteriaConfig();
        collectionViewerConfig.setDefaultSortCriteriaConfig(defaultSortCriteriaConfig);
        collectionViewerConfig.setCollectionRefConfig(collectionRefConfig);
        collectionViewerConfig.setCollectionViewRefConfig(collectionViewRefConfig);
        collectionViewerConfig.setEmbedded(true);
        SelectionFiltersConfig selectionFiltersConfig = tableBrowserConfig.getSelectionFiltersConfig();
        collectionViewerConfig.setSelectionFiltersConfig(selectionFiltersConfig);

        collectionViewerConfig.setInitialFiltersConfig(tableBrowserConfig.getInitialFiltersConfig());
        return collectionViewerConfig;
    }

    protected TableBrowserParams createTableBrowserParams(Boolean displayOnlyChosenIds, Boolean displayCheckBoxes) {
        TableBrowserConfig tableBrowserConfig = currentState.getTableBrowserConfig();
        TableBrowserParams tableBrowserParams = new TableBrowserParams()
                .setComplexFiltersParams(createFiltersParams())
                .setIds(currentState.getIds())
                .setDisplayOnlySelectedIds(displayOnlyChosenIds)
                .setDisplayCheckBoxes(displayCheckBoxes == null ? !currentState.isSingleChoice() : displayCheckBoxes)
                .setDisplayChosenValues(isDisplayChosenValues(displayOnlyChosenIds, displayCheckBoxes))
                .setPageSize(tableBrowserConfig.getPageSize())
                .setSelectionFiltersConfig(currentState.getWidgetConfig().getSelectionFiltersConfig())
                .setCollectionExtraFiltersConfig(tableBrowserConfig.getCollectionExtraFiltersConfig())
                .setHasColumnButtons(tableBrowserConfig.getCollectionTableButtonsConfig() == null ? false
                        : tableBrowserConfig.getCollectionTableButtonsConfig().isDisplayAllPossible());
        return tableBrowserParams;
    }

    private ComplexFiltersParams createFiltersParams() {
        Collection<WidgetIdComponentName> widgetsIdsComponentNames = currentState.getExtraWidgetIdsComponentNames();
        String filterName = currentState.getTableBrowserConfig().getInputTextFilterConfig().getName();
        String filterValue = viewHolder.getChildViewHolder() == null ? null
                : ((TableBrowserItemsView) (viewHolder.getChildViewHolder().getWidget())).getFilterValue();
        WidgetsContainer container = getContainer();
        return GuiUtil.createComplexFiltersParams(filterValue, filterName, container, widgetsIdsComponentNames);

    }

    private boolean isDisplayChosenValues(Boolean displayOnlyIncludedIds, Boolean displayCheckBoxes) {
        TableBrowserConfig config = currentState.getWidgetConfig();
        boolean displayChosenValuesFromConfig = config.getDisplayChosenValues() != null && config.getDisplayChosenValues().isDisplayChosenValues();
        boolean displayChosenValuesForWidgetPurposes = !displayOnlyIncludedIds && displayCheckBoxes != null && displayCheckBoxes;
        return displayChosenValuesForWidgetPurposes || displayChosenValuesFromConfig;

    }

    protected void openCollectionPlugin(CollectionViewerConfig collectionViewerConfig, NavigationConfig navigationConfig,
                                        PluginPanel pluginPanel) {
        collectionPlugin = ComponentRegistry.instance.get("collection.plugin");
        collectionPlugin.setLocalEventBus(localEventBus);
        collectionPlugin.setConfig(collectionViewerConfig);

        collectionPlugin.setNavigationConfig(navigationConfig);
        CollectionRefConfig collectionRefConfig = collectionViewerConfig.getCollectionRefConfig();
        collectionName = collectionRefConfig.getName();
        collectionPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                CollectionPluginView view = (CollectionPluginView) collectionPlugin.getView();
                view.setBreadcrumbWidgets(breadCrumbItemsToWidgets());

            }
        });
        pluginPanel.open(collectionPlugin);
    }

    private ConfiguredButton getCreateButton() {
        ConfiguredButton createButton = getCreateButton(currentState);
        if (createButton != null) {
            createButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    getClickAction().perform();
                }
            });
        }
        return createButton;
    }

    protected void initDialogView() {
        dialogBox = new TableBrowserViewsBuilder().withState(currentState).buildCollectionDialogBox();
        if (currentState.isSingleChoice()) {
            addClickHandlersForSingleChoice(dialogBox);
        } else {
            addClickHandlersForMultiplyChoice(dialogBox);
        }
        initialCollectionViewerConfig = initCollectionConfig(false, null);
        openCollectionPlugin(initialCollectionViewerConfig, null, dialogBox.getPluginPanel());
        dialogBox.center();

    }

    private void addCancelButtonClickHandler(final CollectionDialogBox dialogBox) {
        dialogBox.addCancelButtonHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
                currentState.resetTemporaryState();
                unregisterHandlers();

            }
        });
    }

    private void addOkButtonClickHandler(final CollectionDialogBox dialogBox) {
        dialogBox.addOkButtonHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                currentState.applyChanges();
                dialogBox.hide();
                fetchTableBrowserItems();
                unregisterHandlers();

            }
        });
    }

    @Override
    public void onHyperlinkStateChangedEvent(HyperlinkStateChangedEvent event) {
        if (event.getHyperlinkDisplay() == null) {
            viewHolder.setContent(currentState);
        } else {
            updateHyperlink(event);
        }
    }

    @Override
    protected String getTooltipHandlerName() {
        return "widget-items-handler";
    }

    @Override
    protected void removeTooltipButton() {
        ((TableBrowserItemsView) viewHolder.getChildViewHolder().getWidget()).removeTooltipButton();
    }

    @Override
    protected void drawItemFromTooltipContent() {
        Map.Entry<Id, String> entry = pollItemFromTooltipContent();
        currentState.getListValues().put(entry.getKey(), entry.getValue());
        displayItems();
    }

    private void displayItems() {
        viewHolder.setContent(currentState);
    }

    @Override
    public void onExpandHierarchyEvent(HierarchicalCollectionEvent event) {
        String currentCollectionName = collectionName;
        ExpandHierarchicalCollectionData data = new ExpandHierarchicalCollectionData(
                event.getChildCollectionViewerConfigs(), event.getSelectedId(), currentCollectionName);

        final Command command = new Command("prepareHierarchicalCollectionData", "hierarchical.collection.builder", data);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                ApplicationWindow.errorAlert("Ошибка получения данных иерархической коллекции: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Dto result) {
                HierarchicalCollectionData data = (HierarchicalCollectionData) result;
                DomainObjectSurferConfig pluginConfig = data.getDomainObjectSurferConfig();
                CollectionViewerConfig collectionViewerConfig = pluginConfig.getCollectionViewerConfig();
                collectionViewerConfig.setEmbedded(true);
                LinkConfig link = data.getHierarchicalLink();
                NavigationConfig navigationConfig = new NavigationConfig();
                LinkUtil.addHierarchicalLinkToNavigationConfig(navigationConfig, link);

                if (breadCrumbItems.isEmpty()) {
                    breadCrumbItems.add(new BreadCrumbItem("root", "Исходная коллекция", //we haven't display text
                            // for the root
                            initialCollectionViewerConfig));
                }
                breadCrumbItems.add(new BreadCrumbItem(link.getName(), link.getDisplayText(), collectionViewerConfig));
                PluginPanel pluginPanel = new TableBrowserViewsBuilder().withState(currentState)
                        .createDialogCollectionPluginPanel();
                openCollectionPlugin(collectionViewerConfig, navigationConfig, dialogBox.getPluginPanel());
            }
        });
    }

    @Override
    public void onWidgetItemRemove(WidgetItemRemoveEvent event) {
        Id id = event.getId();
        if (!event.isTooltipContent()) {
            tryToPoolFromTooltipContent();
            currentState.getListValues().remove(id);
        } else {
            currentState.getTooltipValues().remove(id);
        }
        currentState.getSelectedIds().remove(id);
        currentState.decrementFilteredItemsNumber();
        localEventBus.fireEvent(new CollectionChangeSelectionEvent(Arrays.asList(id), false));
        displayItems();

    }

    @Override
    public LinkedFormMappingConfig getLinkedFormMappingConfig() {
        return currentState.getWidgetConfig().getLinkedFormMappingConfig();
    }

    @Override
    public LinkedFormConfig getLinkedFormConfig() {
        return currentState.getWidgetConfig().getLinkedFormConfig();
    }

    @Override
    protected void handleNewCreatedItem(Id id, String representation) {
        if (currentState.isSingleChoice()) {
            currentState.clearState();
        }
        currentState.getSelectedIds().add(id);

        fetchTableBrowserItems();
        if (currentState.isTableView()) {
            localEventBus.fireEvent(new CollectionChangeSelectionEvent(Arrays.asList(id), true));
        }
    }

    @Override
    public void openCollectionView(OpenCollectionRequestEvent event) {
        CollectionViewerConfig config = initCollectionConfig(event.isDisplayOnlyChosenIds(), event.isDisplayCheckBoxes());
        PluginPanel pluginPanel = event.getPluginPanel();
        pluginPanel.closeCurrentPlugin();
        openCollectionPlugin(config, null, pluginPanel);
    }

    @Override
    public void onCheckBoxFieldUpdate(CheckBoxFieldUpdateEvent event) {
        if (currentState.isTableView()) {
            Id id = event.getId();
            if (event.isDeselected()) {
                LinkedHashMap<Id, String> listValues = currentState.getListValues();
                LinkedHashMap<Id, String> tooltipListValues = currentState.getTooltipValues();
                LinkedHashMap<Id, String> common = new LinkedHashMap<Id, String>();
                common.putAll(listValues);
                if (isNotEmpty(tooltipListValues)) {
                    common.putAll(tooltipListValues);
                }
                common.remove(id);
                currentState.getSelectedIds().remove(id);
                handleItems(common);
            } else {
                handlePossibleSingleChoice();
                currentState.getSelectedIds().add(id);
                fetchTableBrowserItems();
            }

        }
    }

    private void handlePossibleSingleChoice() {
        if (currentState.isSingleChoice()) {
            Id idToRemove = currentState.getSelectedIds().isEmpty() ? null : currentState.getSelectedIds().iterator().next();
            if (idToRemove != null) {
                currentState.getSelectedIds().clear();
                localEventBus.fireEvent(new CollectionChangeSelectionEvent(Arrays.asList(idToRemove), false));
            }

        }
    }

    @Override
    public void onParentTabSelectedEvent(ParentTabSelectedEvent event) {
        Element parentElement = event.getParent().getElement();
        Node widgetNode = impl.getElement().getParentNode();
        boolean widgetIsChildOfSelectedTab = parentElement.isOrHasChild(widgetNode);
        boolean viewIsInitialized = collectionPlugin != null && collectionPlugin.getView() != null;
        if (widgetIsChildOfSelectedTab && viewIsInitialized) {
            collectionPlugin.getView().onPluginPanelResize();
        }
    }

    @Override
    public Object getValue() {
        return currentState.getSelectedIds();
    }


    protected class OpenCollectionClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            // temporaryStateOfSelectedIds.clear();
            final List<Id> selectedFromHistory = Application.getInstance().getHistoryManager().getSelectedIds();
            //  currentState.getTemporarySelectedIds().addAll(selectedFromHistory);//causes not expected behaviour for  table browser!
            breadCrumbItems.clear();
            currentState.setTemporaryState(true);
            initDialogView();

        }
    }

    protected void addClickHandlersForMultiplyChoice(final CollectionDialogBox dialogBox) {
        addCommonClickHandlers(dialogBox);
        checkBoxRegistration = localEventBus.addHandler(CheckBoxFieldUpdateEvent.TYPE, new CheckBoxFieldUpdateEventHandler() {
            @Override
            public void onCheckBoxFieldUpdate(CheckBoxFieldUpdateEvent event) {
                Id id = event.getId();
                if (event.isDeselected()) {
                    currentState.removeFromTemporaryState(id);
                } else {
                    currentState.addToTemporaryState(id);
                }

            }
        });



    }


    protected void addClickHandlersForSingleChoice(final CollectionDialogBox dialogBox) {
        addCommonClickHandlers(dialogBox);
        rowSelectedRegistration = localEventBus.addHandler(CollectionRowSelectedEvent.TYPE, new CollectionRowSelectedEventHandler() {
            @Override
            public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
                currentState.getTemporarySelectedIds().clear();
                currentState.addToTemporaryState(event.getId());

            }
        });

    }

    private void addCommonClickHandlers(CollectionDialogBox dialogBox) {
        addCancelButtonClickHandler(dialogBox);
        addOkButtonClickHandler(dialogBox);
        expandHierarchyRegistration = localEventBus.addHandler(HierarchicalCollectionEvent.TYPE, this);
    }

    private void fetchTableBrowserItems() {
        if (currentState.getSelectedIds().isEmpty()) {
            viewHolder.setContent(currentState);
        } else {
            TableBrowserConfig tableBrowserConfig = currentState.getTableBrowserConfig();
            WidgetItemsRequest widgetItemsRequest = new WidgetItemsRequest();
            widgetItemsRequest.setSelectionPattern(tableBrowserConfig.getSelectionPatternConfig().getValue());
            widgetItemsRequest.setSelectedIds(currentState.getIds());
            widgetItemsRequest.setCollectionName(tableBrowserConfig.getCollectionRefConfig().getName());
            widgetItemsRequest.setFormattingConfig(tableBrowserConfig.getFormattingConfig());
            widgetItemsRequest.setSelectionSortCriteriaConfig(tableBrowserConfig.getSelectionSortCriteriaConfig());
            widgetItemsRequest.setSelectionFiltersConfig(tableBrowserConfig.getSelectionFiltersConfig());
            ComplexFiltersParams params = GuiUtil.createComplexFiltersParams(getContainer());
            widgetItemsRequest.setComplexFiltersParams(params);
            Command command = new Command("fetchTableBrowserItems", getName(), widgetItemsRequest);
            BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                @Override
                public void onSuccess(Dto result) {
                    WidgetItemsResponse list = (WidgetItemsResponse) result;
                    LinkedHashMap<Id, String> listValues = list.getListValues();
                    handleItems(listValues);

                }

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("something was going wrong while obtaining rows");
                }
            });

        }
    }

    private void handleItems(LinkedHashMap<Id, String> listValues) {
        currentState.setListValues(new LinkedHashMap<Id, String>(listValues));
        currentState.setFilteredItemsNumber(listValues.size());
        putToCorrectContent();
        displayItems();

    }

    private void putToCorrectContent() {
        int limit = getLimit(currentState.getTableBrowserConfig().getSelectionFiltersConfig());
        if (limit > 0) {
            LinkedHashMap<Id, String> currentListValues = currentState.getListValues();
            LinkedHashMap<Id, String> tooltipListValues = new LinkedHashMap<Id, String>();

            Iterator<Id> idIterator = currentListValues.keySet().iterator();
            int count = 0;
            while (idIterator.hasNext()) {
                count++;
                Id id = idIterator.next();
                if (count > limit) {
                    String representation = currentListValues.get(id);
                    tooltipListValues.put(id, representation);
                    idIterator.remove();
                }

            }
            currentState.setTooltipValues(tooltipListValues);
        }

    }


    private void updateHyperlink(final HyperlinkStateChangedEvent event) {
        List<Id> ids = new ArrayList<Id>();
        Id id = event.getId();
        TableBrowserConfig config = currentState.getTableBrowserConfig();
        String selectionPattern = config.getSelectionPatternConfig().getValue();
        ids.add(id);
        String collectionName = config.getCollectionRefConfig() == null ? null : config.getCollectionRefConfig().getName();
        RepresentationRequest request = new RepresentationRequest(ids, selectionPattern, collectionName,
                config.getFormattingConfig());

        Command command = new Command("getRepresentationForOneItem", getName(), request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                RepresentationResponse response = (RepresentationResponse) result;
                Id id = response.getId();
                String representation = response.getRepresentation();
                LinkedHashMap<Id, String> listValues = getUpdatedHyperlinks(id, representation, event.isTooltipContent());
                HyperlinkDisplay hyperlinkDisplay = event.getHyperlinkDisplay();
                hyperlinkDisplay.displayHyperlinks(listValues, !event.isTooltipContent() && shouldDrawTooltipButton(currentState));
                if (currentState.isTableView()) {
                    localEventBus.fireEvent(new UpdateCollectionEvent(id));
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink", caught);
            }
        });
    }

    private LinkedHashMap<Id, String> getUpdatedHyperlinks(Id id, String representation, boolean tooltipContent) {
        LinkedHashMap<Id, String> listValues = tooltipContent ? currentState.getTooltipValues()
                : currentState.getListValues();
        listValues.put(id, representation);
        return listValues;
    }

    private void unregisterHandlers() {
        if (expandHierarchyRegistration != null) {
            expandHierarchyRegistration.removeHandler();
        }
        if (checkBoxRegistration != null) {
            checkBoxRegistration.removeHandler();
        }
        if (rowSelectedRegistration != null) {
            rowSelectedRegistration.removeHandler();
        }
        collectionPlugin.clearHandlers();
    }

    private List<IsWidget> breadCrumbItemsToWidgets() {
        List<IsWidget> breadCrumbWidgets = new ArrayList<>();
        for (final BreadCrumbItem item : breadCrumbItems) {
            Anchor breadCrumb = new Anchor(item.getDisplayText());
            breadCrumb.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    navigateByBreadCrumb(item.getName());
                }
            });
            breadCrumbWidgets.add(breadCrumb);
        }
        return breadCrumbWidgets;
    }

    private void navigateByBreadCrumb(String linkName) {
        CollectionViewerConfig config = null;
        int removeFrom = breadCrumbItems.size();
        for (int i = 0; i < breadCrumbItems.size() - 1; i++) { // skip last item
            BreadCrumbItem breadCrumbItem = breadCrumbItems.get(i);
            if (breadCrumbItem.getName().equals(linkName)) {
                config = breadCrumbItem.getConfig();
                removeFrom = i;
            }
        }
        breadCrumbItems.subList(removeFrom, breadCrumbItems.size()).clear();
        if (config != null) {
            PluginPanel pluginPanel = new TableBrowserViewsBuilder().withState(currentState).createDialogCollectionPluginPanel();
            openCollectionPlugin(config, new NavigationConfig(), dialogBox.getPluginPanel());
            //TODO: adding to history makes the rows to be highlighted. can we just check checkbox without highlighting?
            Application.getInstance().getHistoryManager().setSelectedIds(currentState.getTemporarySelectedIds().toArray(
                    new Id[currentState.getTemporarySelectedIds().size()]));
        }
    }


}