package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.DialogWindowConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.ShowTooltipEvent;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.WidgetItemRemoveEvent;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.WidgetItemRemoveEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser.TooltipCallback;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkDisplay;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkNoneEditablePanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.ButtonForm;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.EditableTooltipWidget;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPluginView;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.LinkUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.plugin.ExpandHierarchicalCollectionData;
import ru.intertrust.cm.core.gui.model.plugin.HierarchicalCollectionData;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 11:15
 */
@ComponentName("table-browser")
public class TableBrowserWidget extends EditableTooltipWidget implements WidgetItemRemoveEventHandler,
        HyperlinkStateChangedEventHandler, HierarchicalCollectionEventHandler {
    public static final int DEFAULT_DIALOG_WIDTH = 500;
    public static final int DEFAULT_DIALOG_HEIGHT = 300;
    private PluginPanel pluginPanel;
    private FocusPanel openDialogButton;
    private FocusPanel clearButton;

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
        currentState.resetChanges();

        if (isEditable()) {
            setCurrentStateForEditableWidget();
        } else {
            setCurrentStateForNoneEditableWidget();
        }
    }

    @Override
    protected boolean isChanged() {
        return false;
    }

    private void setCurrentStateForEditableWidget() {
        initDialogWindowSize();
        initAddButton();
        initClearAllButton();
        displayItems();

    }

    private void setCurrentStateForNoneEditableWidget() {
        HyperlinkNoneEditablePanel noneEditablePanel = (HyperlinkNoneEditablePanel) impl;
        LinkedHashMap<Id, String> listValues = currentState.getListValues();
        if (isDisplayingAsHyperlink()) {
            noneEditablePanel.displayHyperlinks(listValues, shouldDrawTooltipButton());
        } else {
            noneEditablePanel.displayItems(listValues.values(), shouldDrawTooltipButton());
        }

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
        SelectionStyleConfig selectionStyleConfig = currentState.getTableBrowserConfig().getSelectionStyleConfig();
        return initWidgetView(selectionStyleConfig);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        commonInitialization(state);
        SelectionStyleConfig selectionStyleConfig = currentState.getTableBrowserConfig().getSelectionStyleConfig();
        return new HyperlinkNoneEditablePanel(selectionStyleConfig, localEventBus, false);

    }

    private void commonInitialization(WidgetState state) {
        currentState = (TableBrowserState) state;
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        localEventBus.addHandler(ShowTooltipEvent.TYPE, this);

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
                CollectionPluginView view = (CollectionPluginView) collectionPlugin.getView();
                view.setBreadcrumbWidgets(breadCrumbItemsToWidgets());
                dialogBox.center();
            }
        });
        pluginPanel.open(collectionPlugin);
        return collectionPlugin;
    }

    private Panel initWidgetView(SelectionStyleConfig selectionStyleConfig) {
        widgetItemsView = new TableBrowserItemsView(selectionStyleConfig, localEventBus);

        openDialogButton = new FocusPanel();
        openDialogButton.addClickHandler(new FetchFilteredRowsClickHandler());
        clearButton = new FocusPanel();
        clearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                currentState.clearState();

            }
        });
        root.add(widgetItemsView);
        root.add(openDialogButton);
        root.add(clearButton);

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
        if (currentState.isSingleChoice()) {
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

    private void addCancelButtonClickHandler(final Button cancelButton, final DialogBox dialogBox) {

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
                currentState.resetChanges();
                widgetItemsView.clearFilterInput();
            }
        });
    }

    @Override
    public void onHyperlinkStateChangedEvent(HyperlinkStateChangedEvent event) {
        updateHyperlink(event);
    }

    @Override
    protected String getTooltipHandlerName() {
        return "widget-items-handler";
    }

    @Override
    protected void removeTooltipButton() {
        widgetItemsView.removeTooltipButton();
    }

    @Override
    protected void drawItemFromTooltipContent() {
        Map.Entry<Id, String> entry = pollItemFromTooltipContent();
        currentState.getListValues().put(entry.getKey(), entry.getValue());
        displayItems();
    }

    private void displayItems() {
        if (isDisplayingAsHyperlink()) {
            widgetItemsView.displayHyperlinks(currentState.getListValues(), shouldDrawTooltipButton());
        } else {
            widgetItemsView.displayItems(currentState.getListValues(), shouldDrawTooltipButton());
        }
        widgetItemsView.changeInputFilterWidth();
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
                ApplicationWindow.errorAlert(caught.getMessage());
            }

            @Override
            public void onSuccess(Dto result) {
                HierarchicalCollectionData data = (HierarchicalCollectionData) result;
                DomainObjectSurferConfig pluginConfig = data.getDomainObjectSurferConfig();
                CollectionViewerConfig collectionViewerConfig = pluginConfig.getCollectionViewerConfig();
                LinkConfig link = data.getHierarchicalLink();
                NavigationConfig navigationConfig = new NavigationConfig();
                LinkUtil.addHierarchicalLinkToNavigationConfig(navigationConfig, link);

                if (breadCrumbItems.isEmpty()) {
                    breadCrumbItems.add(new BreadCrumbItem("root", "Исходная коллекция", //we haven't display text for the root
                            initialCollectionViewerConfig));
                }
                breadCrumbItems.add(new BreadCrumbItem(link.getName(), link.getDisplayText(), collectionViewerConfig));

                openCollectionPlugin(collectionViewerConfig, navigationConfig);
            }
        });
    }

    @Override
    public void onWidgetItemRemove(WidgetItemRemoveEvent event) {
        if (!event.isTooltipContent()) {
            tryToPoolFromTooltipContent();
            currentState.getListValues().remove(event.getId());
        } else {
            currentState.getTooltipValues().remove(event.getId());
        }
        currentState.getSelectedIds().remove(event.getId());
        displayItems();


    }

    private class FetchFilteredRowsClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            // temporaryStateOfSelectedIds.clear();
            final List<Id> selectedFromHistory = Application.getInstance().getHistoryManager().getSelectedIds();
            //  currentState.getTemporarySelectedIds().addAll(selectedFromHistory);//causes not expected behaviour for  table browser!

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
                currentState.applyChanges();
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
                    currentState.removeFromTemporaryState(id);

                } else {
                    currentState.addToTemporaryState(id);
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
                currentState.applyChanges();
                fetchTableBrowserItems();
                dialogBox.hide();
                widgetItemsView.clearFilterInput();
            }
        });
        rowSelectedRegistration = localEventBus.addHandler(CollectionRowSelectedEvent.TYPE, new CollectionRowSelectedEventHandler() {
            @Override
            public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
                currentState.resetChanges();
                currentState.addToTemporaryState(event.getId());

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
            widgetItemsView.displayItems(currentState.getListValues(), shouldDrawTooltipButton());
            return;
        }
        TableBrowserConfig tableBrowserConfig = currentState.getTableBrowserConfig();
        WidgetItemsRequest widgetItemsRequest = new WidgetItemsRequest();
        widgetItemsRequest.setSelectionPattern(tableBrowserConfig.getSelectionPatternConfig().getValue());
        widgetItemsRequest.setSelectedIds(currentState.getTemporarySelectedIds());
        widgetItemsRequest.setCollectionName(tableBrowserConfig.getCollectionRefConfig().getName());
        widgetItemsRequest.setFormattingConfig(tableBrowserConfig.getFormattingConfig());
        widgetItemsRequest.setDefaultSortCriteriaConfig(tableBrowserConfig.getDefaultSortCriteriaConfig());
        widgetItemsRequest.setSelectionFiltersConfig(tableBrowserConfig.getSelectionFiltersConfig());
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

    private void handleItems(LinkedHashMap<Id, String> listValues) {
        currentState.resetChanges();
        if (currentState.isSingleChoice()) {
            currentState.setListValues(listValues);
        } else {
            if (shouldDrawTooltipButton()) {
                putToCorrectContent(listValues);
            } else {
                currentState.getListValues().putAll(listValues);
            }
        }
        displayItems();


    }

    private void putToCorrectContent(LinkedHashMap<Id, String> listValues) {
        int limit = WidgetUtil.getLimit(currentState.getTableBrowserConfig().getSelectionFiltersConfig());
        int size = currentState.getListValues().size();
        if (size <= limit) {
            int delta = limit - size; //how much will be shown in main content
            putInMainContentAndRemoveFromCommon(listValues, delta);
            putInTooltipContent(listValues);

        } else {
            putInTooltipContent(listValues);
        }
    }

    private void putInTooltipContent(final LinkedHashMap<Id, String> listValues) {
        final LinkedHashMap<Id, String> tooltipValues = currentState.getTooltipValues();
        if (tooltipValues == null) {
            fetchWidgetItems(new TooltipCallback() {
                @Override
                public void perform() {
                    tooltipValues.putAll(listValues);
                }
            });
        } else {
            tooltipValues.putAll(listValues);
        }

    }

    private void putInMainContentAndRemoveFromCommon(LinkedHashMap<Id, String> commonListValues, int delta) {

        LinkedHashMap<Id, String> currentListValues = currentState.getListValues();
        Iterator<Id> idIterator = commonListValues.keySet().iterator();
        int count = 0;
        while (idIterator.hasNext() && count < delta) {
            count++;
            Id id = idIterator.next();
            String representation = commonListValues.get(id);
            currentListValues.put(id, representation);
            idIterator.remove();
        }
    }

    private void updateHyperlink(final HyperlinkStateChangedEvent event) {
        List<Id> ids = new ArrayList<Id>();
        Id id = event.getId();
        String selectionPattern = currentState.getTableBrowserConfig().getSelectionPatternConfig().getValue();
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
                LinkedHashMap<Id, String> listValues = getUpdatedHyperlinks(id, representation, event.isTooltipContent());
                HyperlinkDisplay hyperlinkDisplay = event.getHyperlinkDisplay();
                hyperlinkDisplay.displayHyperlinks(listValues, !event.isTooltipContent() && shouldDrawTooltipButton());

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink");
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
            Application.getInstance().getHistoryManager().setSelectedIds(currentState.getTemporarySelectedIds().toArray(
                    new Id[currentState.getTemporarySelectedIds().size()]));
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