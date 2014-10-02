package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.DialogWindowConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.CheckBoxFieldUpdateEvent;
import ru.intertrust.cm.core.gui.impl.client.event.CheckBoxFieldUpdateEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.HierarchicalCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HierarchicalCollectionEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkNoneEditablePanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.ButtonForm;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.TooltipWidget;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPluginView;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.LinkUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationResponse;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsRequest;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsResponse;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.ExpandHierarchicalCollectionData;
import ru.intertrust.cm.core.gui.model.plugin.HierarchicalCollectionData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 11:15
 */
@ComponentName("table-browser")
public class TableBrowserWidget extends TooltipWidget implements HyperlinkStateChangedEventHandler, HierarchicalCollectionEventHandler {
    public static final int DEFAULT_DIALOG_WIDTH = 500;
    public static final int DEFAULT_DIALOG_HEIGHT = 300;
    private PluginPanel pluginPanel;
    private FocusPanel openDialogButton;
    private FocusPanel clearButton;

    private ArrayList<Id> temporaryStateOfSelectedIds = new ArrayList<Id>();
    private TableBrowserState currentState;
    private int dialogWidth;
    private int dialogHeight;
    private DialogBox dialogBox;
    private TableBrowserItemsView widgetItemsView;
    private Panel root = new HorizontalPanel();

    private List<BreadCrumbItem> breadCrumbItems = new ArrayList<>();

    private HandlerRegistration expandHierarchyRegistration;
    private HandlerRegistration checkBoxRegistration;
    private HandlerRegistration rowSelectedRegistration;

    private String collectionName;
    private CollectionViewerConfig initialCollectionViewerConfig;

    @Override
    public void setCurrentState(WidgetState state) {
        initialData = state;
        currentState = (TableBrowserState) state;
        if (isEditable()) {
            setCurrentStateForEditableWidget(currentState);
        } else {
            setCurrentStateForNoneEditableWidget(currentState);
        }
    }

    @Override
    protected boolean isChanged() {
        return false;
    }

    private void setCurrentStateForEditableWidget(TableBrowserState state) {

        widgetItemsView.setListValues(state.getListValues());
        widgetItemsView.setShouldDrawTooltipButton(shouldDrawTooltipButton());
        widgetItemsView.setTooltipClickHandler(new ShowTooltipHandler());
        initDialogWindowSize();

        initAddButton();
        initClearAllButton();
        widgetItemsView.setSelectedIds(state.getSelectedIds());
        if (isDisplayingAsHyperlink()) {
            widgetItemsView.setEventBus(localEventBus);
            widgetItemsView.displayHyperlinkItems();
        } else {
            widgetItemsView.displayItems();
        }

    }

    private void setCurrentStateForNoneEditableWidget(TableBrowserState state) {
        HyperlinkNoneEditablePanel noneEditablePanel = (HyperlinkNoneEditablePanel) impl;
        LinkedHashMap<Id, String> listValues = state.getListValues();
        if (isDisplayingAsHyperlink()) {
            noneEditablePanel.displayHyperlinks(listValues);
        } else {
            noneEditablePanel.displayItems(listValues.values());
        }
        if (shouldDrawTooltipButton()) {
            noneEditablePanel.addShowTooltipLabel(new ShowTooltipHandler());
        }
    }

    @Override
    protected TableBrowserState createNewState() {
        TableBrowserState state = new TableBrowserState();
        TableBrowserState previousState = getInitialData();
        if (isEditable()) {
            state.setListValues(widgetItemsView.getListValues());
        } else {
            LinkedHashMap<Id, String> listValues = previousState.getListValues();
            state.setListValues(listValues);
        }
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
        SelectionStyleConfig selectionStyleConfig = currentState.getTableBrowserConfig().getSelectionStyleConfig();
        return initWidgetView(selectionStyleConfig);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        commonInitialization(state);
        SelectionStyleConfig selectionStyleConfig = currentState.getTableBrowserConfig().getSelectionStyleConfig();
        return new HyperlinkNoneEditablePanel(selectionStyleConfig, localEventBus);

    }

    private void commonInitialization(WidgetState state) {
        currentState = (TableBrowserState) state;
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);

    }

    @Override
    public Component createNew() {
        return new TableBrowserWidget();
    }

    private void initCollectionPluginPanel() {
        pluginPanel = new PluginPanel();
        pluginPanel.setVisibleWidth(dialogWidth);
        pluginPanel.setVisibleHeight(dialogHeight - 100);//it's height of table body only. TODO: eliminate hardcoded value

    }

    private CollectionViewerConfig initCollectionConfig() {
        CollectionViewerConfig collectionViewerConfig = new CollectionViewerConfig();
        CollectionViewRefConfig collectionViewRefConfig = new CollectionViewRefConfig();
        TableBrowserConfig tableBrowserConfig = currentState.getTableBrowserConfig();
        TableBrowserParams tableBrowserParams = new TableBrowserParams()
                .setFilterName(tableBrowserConfig.getInputTextFilterConfig().getName())
                .setFilterValue(widgetItemsView.getFilterValue())
                .setExcludedIds(currentState.getIds())
                .setSingleChoice(currentState.isSingleChoice())
                .setDisplayChosenValues(tableBrowserConfig.getDisplayChosenValues().isDisplayChosenValues())
                .setPageSize(tableBrowserConfig.getPageSize());
        collectionViewerConfig.setTableBrowserParams(tableBrowserParams);
        collectionViewRefConfig.setName(tableBrowserConfig.getCollectionViewRefConfig().getName());
        CollectionRefConfig collectionRefConfig = new CollectionRefConfig();
        collectionRefConfig.setName(tableBrowserConfig.getCollectionRefConfig().getName());
        DefaultSortCriteriaConfig defaultSortCriteriaConfig = tableBrowserConfig.getDefaultSortCriteriaConfig();
        collectionViewerConfig.setDefaultSortCriteriaConfig(defaultSortCriteriaConfig);
        collectionViewerConfig.setCollectionRefConfig(collectionRefConfig);
        collectionViewerConfig.setCollectionViewRefConfig(collectionViewRefConfig);

        SelectionFiltersConfig selectionFiltersConfig = tableBrowserConfig.getSelectionFiltersConfig();
        collectionViewerConfig.setSelectionFiltersConfig(selectionFiltersConfig);

        collectionViewerConfig.setInitialFiltersConfig(tableBrowserConfig.getInitialFiltersConfig());
        return collectionViewerConfig;
    }

    private CollectionPlugin openCollectionPlugin(CollectionViewerConfig collectionViewerConfig, NavigationConfig navigationConfig) {

        final CollectionPlugin collectionPlugin = ComponentRegistry.instance.get("collection.plugin");
        collectionPlugin.setConfig(collectionViewerConfig);
        collectionPlugin.setLocalEventBus(localEventBus);
        collectionPlugin.setNavigationConfig(navigationConfig);
        CollectionRefConfig collectionRefConfig = collectionViewerConfig.getCollectionRefConfig();
        collectionName = collectionRefConfig.getName();
        collectionPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                CollectionPluginView view = (CollectionPluginView)collectionPlugin.getView();
                view.setBreadcrumbWidgets(breadCrumbItemsToWidgets());
                dialogBox.center();
            }
        });
        pluginPanel.open(collectionPlugin);
        return collectionPlugin;
    }

    private Panel initWidgetView(SelectionStyleConfig selectionStyleConfig) {
        widgetItemsView = new TableBrowserItemsView(selectionStyleConfig);

        openDialogButton = new FocusPanel();
        openDialogButton.addClickHandler(new FetchFilteredRowsClickHandler());
        clearButton = new FocusPanel();
        clearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                temporaryStateOfSelectedIds.clear();
                widgetItemsView.getListValues().clear();
                widgetItemsView.displayItems();
                currentState.getSelectedIds().clear();

            }
        });
        root.add(widgetItemsView);
        root.add(openDialogButton);
        root.add(clearButton);

        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        return root;
    }

    private void initAddButton() {
        openDialogButton.clear();
        Widget addButton;
        TableBrowserConfig tableBrowserConfig = currentState.getTableBrowserConfig();
        if (tableBrowserConfig.getAddButtonConfig() != null) {
            String img = tableBrowserConfig.getAddButtonConfig().getImage();
            String text = tableBrowserConfig.getAddButtonConfig().getText();
            if (text == null || text.length() == 0) {
                text = "Добавить";
            }
            addButton = new ButtonForm(openDialogButton, img, text);
        } else {
            addButton = new AbsolutePanel();
            addButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().arrowDownButton());
        }

        openDialogButton.add(addButton);
    }

    private void initClearAllButton() {
        TableBrowserConfig tableBrowserConfig = currentState.getTableBrowserConfig();
        if (tableBrowserConfig.getClearAllButtonConfig() != null) {
            String img = tableBrowserConfig.getClearAllButtonConfig().getImage();
            String text = tableBrowserConfig.getClearAllButtonConfig().getText();

            clearButton.clear();
            ButtonForm buttonForm = new ButtonForm(clearButton, img, text);

            clearButton.add(buttonForm);

        }

    }

    private void initDialogView() {
        dialogBox = new DialogBox();
        dialogBox.removeStyleName("gwt-DialogBox");
        dialogBox.addStyleName("table-browser-dialog popup-z-index");
        //initCollectionConfig();
        initCollectionPluginPanel();
        Button okButton = new Button("OK");
        okButton.removeStyleName("gwt-Button");
        okButton.addStyleName("dark-button buttons-fixed");
        Button cancelButton = new Button("Отмена");
        cancelButton.removeStyleName("gwt-Button");
        cancelButton.addStyleName("light-button buttons-fixed position-margin-left");
        if (isSingleChoice()) {
            addClickHandlersForSingleChoice(okButton, cancelButton, dialogBox);
        } else {
            addClickHandlersForMultiplyChoice(okButton, cancelButton, dialogBox);
        }
        AbsolutePanel buttonsContainer = new AbsolutePanel();
        buttonsContainer.addStyleName("table-browser-dialog-box-button-panel");
        buttonsContainer.add(okButton);
        buttonsContainer.add(cancelButton);
        FlowPanel dialogBoxContent = new FlowPanel();

        dialogBoxContent.setWidth(dialogWidth + "px");
        dialogBoxContent.addStyleName("table-browser-dialog-box-content");

        dialogBoxContent.add(pluginPanel);
        dialogBoxContent.add(buttonsContainer);
        dialogBox.add(dialogBoxContent);
        dialogBox.setWidth(dialogWidth + "px");
        dialogBox.setHeight(dialogHeight + "px");

    }

    private boolean isSingleChoice() {
        return currentState.isSingleChoice();

    }

    private void addCancelButtonClickHandler(final Button cancelButton, final DialogBox dialogBox) {

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
                temporaryStateOfSelectedIds.clear();
                widgetItemsView.clearFilterInput();
            }
        });
    }

    @Override
    public void onHyperlinkStateChangedEvent(HyperlinkStateChangedEvent event) {
        PopupPanel popupPanel = event.getPopupPanel();
        if (popupPanel != null) {
            popupPanel.hide();
            fetchWidgetItems();
            return;
        }
        Id id = event.getId();
        updateHyperlink(id, currentState.getTableBrowserConfig().getSelectionPatternConfig().getValue());
    }

    @Override
    protected String getTooltipHandlerName() {
        return "widget-items-handler";
    }

    @Override
    public void onExpandHierarchyEvent(HierarchicalCollectionEvent event) {
        String currentCollectionName = collectionName;
        ExpandHierarchicalCollectionData data = new ExpandHierarchicalCollectionData(
                event.getChildCollectionViewerConfigs(), event.getSelectedId(), currentCollectionName);

        final Command command = new Command("initializeForHierarchicalCollection", "domain.object.surfer.plugin", data);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                ApplicationWindow.errorAlert(caught.getMessage());
            }
            @Override
            public void onSuccess(Dto result) {
                HierarchicalCollectionData data = (HierarchicalCollectionData) result;
                DomainObjectSurferConfig pluginConfig = data.getDomainObjectSurferConfig();
                CollectionViewerConfig collectionViewerConfig = pluginConfig.getCollectionViewerConfig();
                LinkConfig link = data.getHierarchicalLink();
                NavigationConfig navigationConfig = new NavigationConfig();
                addHierarchicalLinkToNavigationConfig(navigationConfig, link);

                if (breadCrumbItems.isEmpty()) {
                    breadCrumbItems.add(new BreadCrumbItem("root", "Исходная коллекция", //we haven't display text for the root
                            initialCollectionViewerConfig));
                }
                breadCrumbItems.add(new BreadCrumbItem(link.getName(), link.getDisplayText(), collectionViewerConfig));

                openCollectionPlugin(collectionViewerConfig, navigationConfig);
            }

            //TODO: extract common code (here and DOSP)
            private void addHierarchicalLinkToNavigationConfig(NavigationConfig navigationConfig, LinkConfig link) {
                LinkConfig parentLink = findParentLink(navigationConfig, link);
                link.setParentLinkConfig(parentLink);
                if (!navigationConfig.getHierarchicalLinkList().contains(link)) {
                    navigationConfig.getHierarchicalLinkList().add(link);
                }
            }

            private LinkConfig findParentLink(NavigationConfig navigationConfig, LinkConfig link) {
                LinkConfig parentLinkConfig = null;
                HistoryManager manager = Application.getInstance().getHistoryManager();
                String parentLinkName = manager.getLink();
                List<LinkConfig> results = new ArrayList<>();
                LinkUtil.findLink(parentLinkName, navigationConfig.getLinkConfigList(), results);
                if (results.isEmpty()) {
                    LinkUtil.findLink(parentLinkName, navigationConfig.getHierarchicalLinkList(), results);
                }
                if (!results.isEmpty()) {
                    parentLinkConfig = results.get(0);
                }
                return parentLinkConfig;
            }
        });
    }

    private class FetchFilteredRowsClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
           // temporaryStateOfSelectedIds.clear();
            final List<Id> selectedFromHistory = Application.getInstance().getHistoryManager().getSelectedIds();
            temporaryStateOfSelectedIds.addAll(selectedFromHistory);
            breadCrumbItems.clear();
            unregisterHandlers();
            initDialogView();
            initialCollectionViewerConfig = initCollectionConfig();
            openCollectionPlugin(initialCollectionViewerConfig, null);
        }
    }

    private void addClickHandlersForMultiplyChoice(final Button okButton, final Button cancelButton, final DialogBox dialogBox) {

        addCancelButtonClickHandler(cancelButton, dialogBox);
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                currentState.getSelectedIds().addAll(temporaryStateOfSelectedIds);
                temporaryStateOfSelectedIds.clear();
                fetchTableBrowserItems();
                dialogBox.hide();
                widgetItemsView.clearFilterInput();
            }
        });
        checkBoxRegistration = localEventBus.addHandler(CheckBoxFieldUpdateEvent.TYPE, new CheckBoxFieldUpdateEventHandler() {
            @Override
            public void onCheckBoxFieldUpdate(CheckBoxFieldUpdateEvent event) {
                Id id = event.getId();
                if (event.isDeselected()) {
                    temporaryStateOfSelectedIds.remove(id);
                    widgetItemsView.removeChosenItem(id);
                } else {
                    temporaryStateOfSelectedIds.add(id);
                }
            }
        });
        expandHierarchyRegistration = localEventBus.addHandler(HierarchicalCollectionEvent.TYPE, this);
    }

    private void addClickHandlersForSingleChoice(final Button okButton, final Button cancelButton, final DialogBox dialogBox) {
        addCancelButtonClickHandler(cancelButton, dialogBox);
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                currentState.getSelectedIds().clear();
                currentState.getSelectedIds().addAll(temporaryStateOfSelectedIds);
                fetchTableBrowserItems();
                dialogBox.hide();
                widgetItemsView.clearFilterInput();
            }
        });
        rowSelectedRegistration = localEventBus.addHandler(CollectionRowSelectedEvent.TYPE, new CollectionRowSelectedEventHandler() {
            @Override
            public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
                temporaryStateOfSelectedIds.clear();
                temporaryStateOfSelectedIds.add(event.getId());

            }
        });
        expandHierarchyRegistration = localEventBus.addHandler(HierarchicalCollectionEvent.TYPE, this);
    }

    private void initDialogWindowSize() {
        DialogWindowConfig dialogWindowConfig = currentState.getTableBrowserConfig().getDialogWindowConfig();
        String widthString = dialogWindowConfig != null ? dialogWindowConfig.getWidth() : null;
        String heightString = dialogWindowConfig != null ? dialogWindowConfig.getHeight() : null;
        dialogWidth = widthString == null ? DEFAULT_DIALOG_WIDTH : Integer.parseInt(widthString.replaceAll("\\D+", ""));
        dialogHeight = heightString == null ? DEFAULT_DIALOG_HEIGHT : Integer.parseInt(heightString.replaceAll("\\D+", ""));
    }

    private void fetchTableBrowserItems() {

        if (currentState.getSelectedIds().isEmpty()) {
            widgetItemsView.displayItems();
            return;
        }
        TableBrowserConfig tableBrowserConfig = currentState.getTableBrowserConfig();
        WidgetItemsRequest widgetItemsRequest = new WidgetItemsRequest();
        widgetItemsRequest.setSelectionPattern(tableBrowserConfig.getSelectionPatternConfig().getValue());
        widgetItemsRequest.setSelectedIds(currentState.getIds());
        widgetItemsRequest.setCollectionName(tableBrowserConfig.getCollectionRefConfig().getName());
        widgetItemsRequest.setFormattingConfig(tableBrowserConfig.getFormattingConfig());
        widgetItemsRequest.setDefaultSortCriteriaConfig(tableBrowserConfig.getDefaultSortCriteriaConfig());
        widgetItemsRequest.setSelectionFiltersConfig(tableBrowserConfig.getSelectionFiltersConfig());
        Command command = new Command("fetchTableBrowserItems",getName(), widgetItemsRequest);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                WidgetItemsResponse list = (WidgetItemsResponse) result;
                LinkedHashMap<Id, String> listValues = list.getListValues();
                handleItemsForMainContent(listValues);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });

    }

    private void handleItemsForMainContent(LinkedHashMap<Id, String> listValues) {
        if (isSingleChoice()) {
            widgetItemsView.setListValues(listValues);
        } else {
            widgetItemsView.getListValues().putAll(listValues);
        }
        widgetItemsView.displayItems();
        temporaryStateOfSelectedIds.clear();

    }

    private void updateHyperlink(Id id, String selectionPattern) {
        List<Id> ids = new ArrayList<Id>();
        ids.add(id);
        RepresentationRequest request = new RepresentationRequest(ids, selectionPattern, currentState.
                getTableBrowserConfig().getFormattingConfig());
        Command command = new Command("getRepresentationForOneItem", "representation-updater", request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                RepresentationResponse response = (RepresentationResponse) result;
                Id id = response.getId();
                String representation = response.getRepresentation();
                if (isEditable()) {
                    widgetItemsView.updateHyperlinkItem(id, representation);
                } else {
                    HyperlinkNoneEditablePanel noneEditablePanel = (HyperlinkNoneEditablePanel) impl;
                    noneEditablePanel.cleanPanel();
                    LinkedHashMap<Id, String> listValues = getUpdatedHyperlinks(id, representation);
                    noneEditablePanel.displayHyperlinks(listValues);
                }

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink");
            }
        });
    }

    private LinkedHashMap<Id, String> getUpdatedHyperlinks(Id id, String representation) {
        TableBrowserState state = getInitialData();
        LinkedHashMap<Id, String> listValues = state.getListValues();
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
    }

    private List<IsWidget> breadCrumbItemsToWidgets() {
        List<IsWidget> breadCrumbWidgets = new ArrayList<>();
        for (final BreadCrumbItem item : breadCrumbItems) {
            Anchor breadCrumb = new Anchor(item.displayText);
            breadCrumb.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    navigateByBreadCrumb(item.name);
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
            if (breadCrumbItem.name.equals(linkName)) {
                config = breadCrumbItem.config;
                removeFrom = i;
            }
        }
        breadCrumbItems.subList(removeFrom, breadCrumbItems.size()).clear();
        if (config != null) {
            openCollectionPlugin(config, new NavigationConfig());
            //TODO: adding to history makes the rows to be highlighted. can we just check checkbox without highlighting?
            Application.getInstance().getHistoryManager().setSelectedIds(temporaryStateOfSelectedIds.toArray(
                    new Id[temporaryStateOfSelectedIds.size()]));
        }
    }

    private static class BreadCrumbItem {
        private final String name;
        private final String displayText;
        private final CollectionViewerConfig config;

        private BreadCrumbItem(String name, String displayText, CollectionViewerConfig config) {
            this.name = name;
            this.displayText = displayText;
            this.config = config;
        }
    }

}