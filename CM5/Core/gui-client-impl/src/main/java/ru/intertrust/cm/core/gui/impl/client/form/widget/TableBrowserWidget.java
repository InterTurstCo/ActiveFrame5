package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.DialogWindowConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetItemsView;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkNoneEditablePanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.ButtonForm;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.TooltipWidget;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
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
public class TableBrowserWidget extends TooltipWidget implements HyperlinkStateChangedEventHandler {
    public static final int DEFAULT_DIALOG_WIDTH = 500;
    public static final int DEFAULT_DIALOG_HEIGHT = 300;
    private PluginPanel pluginPanel;
    private FocusPanel openDialogButton;
    private FocusPanel clearButton;
    private TextBox filterEditor;
    private EventBus localEventBus = new SimpleEventBus();
    private ArrayList<Id> temporaryStateOfSelectedIds = new ArrayList<Id>();
    private TableBrowserState currentState;
    private int dialogWidth;
    private int dialogHeight;
    private DialogBox dialogBox;
    private WidgetItemsView widgetItemsView;
    private FlowPanel root = new FlowPanel();

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

    private void setCurrentStateForEditableWidget(TableBrowserState state) {

        widgetItemsView.setListValues(state.getListValues());
        initDialogWindowSize();
        initDialogView();
        initAddButton();
        initClearAllButton();
        widgetItemsView.setSelectedIds(state.getSelectedIds());
        if (isDisplayingAsHyperlink()) {
            widgetItemsView.setEventBus(localEventBus);
            widgetItemsView.displayHyperlinkItems();
        } else {
            widgetItemsView.displayItems();
        }
        widgetItemsView.addShowTooltipButton(new ShowTooltipHandler());
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
        collectionViewerConfig.setFilterName(tableBrowserConfig.getInputTextFilterConfig().getName());
        collectionViewerConfig.setFilterValue(filterEditor.getValue());
        collectionViewRefConfig.setName(tableBrowserConfig.getCollectionViewRefConfig().getName());
        CollectionRefConfig collectionRefConfig = new CollectionRefConfig();
        collectionRefConfig.setName(tableBrowserConfig.getCollectionRefConfig().getName());
        DefaultSortCriteriaConfig defaultSortCriteriaConfig = tableBrowserConfig.getDefaultSortCriteriaConfig();
        collectionViewerConfig.setDefaultSortCriteriaConfig(defaultSortCriteriaConfig);
        collectionViewerConfig.setCollectionRefConfig(collectionRefConfig);
        collectionViewerConfig.setCollectionViewRefConfig(collectionViewRefConfig);
        collectionViewerConfig.setSingleChoice(currentState.isSingleChoice());
        collectionViewerConfig.setDisplayChosenValues(tableBrowserConfig.getDisplayChosenValues().isDisplayChosenValues());
        collectionViewerConfig.setExcludedIds(currentState.getIds());
        SelectionFiltersConfig selectionFiltersConfig = tableBrowserConfig.getSelectionFiltersConfig();
        collectionViewerConfig.setSelectionFiltersConfig(selectionFiltersConfig);
        InitialFiltersConfig initialFiltersConfig = tableBrowserConfig.getInitialFiltersConfig();
        collectionViewerConfig.setInitialFiltersConfig(initialFiltersConfig);
        return collectionViewerConfig;
    }

    private void openCollectionPlugin() {

        CollectionPlugin collectionPlugin = ComponentRegistry.instance.get("collection.plugin");
        CollectionViewerConfig collectionViewerConfig = initCollectionConfig();
        collectionPlugin.setConfig(collectionViewerConfig);
        collectionPlugin.setLocalEventBus(localEventBus);
        collectionPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                dialogBox.center();
            }
        });
        pluginPanel.open(collectionPlugin);

    }

    private FlowPanel initWidgetView(SelectionStyleConfig selectionStyleConfig) {
        widgetItemsView = new WidgetItemsView(selectionStyleConfig);
        filterEditor = new TextBox();
        filterEditor.getElement().setClassName("table-browser-filter-editor");
        openDialogButton = new FocusPanel();
        openDialogButton.addClickHandler(new FetchFilteredRowsClickHandler());
        clearButton = new FocusPanel();
        clearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                temporaryStateOfSelectedIds.clear();
                widgetItemsView.getListValues().clear();
                widgetItemsView.displayItems();
                ((TableBrowserState) getCurrentState()).getIds().clear();

            }
        });
        root.add(filterEditor);
        root.add(openDialogButton);
        root.add(clearButton);
        root.add(widgetItemsView);
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        return root;
    }

    private void initAddButton() {
        openDialogButton.clear();
        ButtonForm addButton;
        TableBrowserConfig tableBrowserConfig = currentState.getTableBrowserConfig();
        if (tableBrowserConfig.getClearAllButtonConfig() != null) {
            String img = tableBrowserConfig.getAddButtonConfig().getImage();
            String text = tableBrowserConfig.getAddButtonConfig().getText();
            if (text == null || text.equals("...") || text.length() == 0) {
                text = "Добавить";
            }
            addButton = new ButtonForm(openDialogButton, img, text);
        } else {
            addButton = new ButtonForm(openDialogButton, null, "Добавить");
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
            root.insert(clearButton, 2);
        }

    }

    private void initDialogView() {
        dialogBox = new DialogBox();
        dialogBox.removeStyleName("gwt-DialogBox");
        dialogBox.addStyleName("table-browser-dialog popup-z-index");
        initCollectionConfig();
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
        final SingleChoiceConfig singleChoice = currentState.getTableBrowserConfig().getSingleChoice();
        return singleChoice != null && singleChoice.isSingleChoice();
    }

    private void addCancelButtonClickHandler(final Button cancelButton, final DialogBox dialogBox) {

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
                temporaryStateOfSelectedIds.clear();
            }
        });
    }

    @Override
    public void onHyperlinkStateChangedEvent(HyperlinkStateChangedEvent event) {
        Id id = event.getId();
        updateHyperlink(id, currentState.getTableBrowserConfig().getSelectionPatternConfig().getValue());
    }

    @Override
    protected String getTooltipHandlerName() {
        return "widget-items-handler";
    }

    private class FetchFilteredRowsClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            openCollectionPlugin();

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
            }
        });
        localEventBus.addHandler(CheckBoxFieldUpdateEvent.TYPE, new CheckBoxFieldUpdateEventHandler() {
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

    }

    private void addClickHandlersForSingleChoice(final Button okButton, final Button cancelButton, final DialogBox dialogBox) {
        addCancelButtonClickHandler(cancelButton, dialogBox);
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                fetchTableBrowserItems();
                dialogBox.hide();
            }
        });
        localEventBus.addHandler(CollectionRowSelectedEvent.TYPE, new CollectionRowSelectedEventHandler() {
            @Override
            public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
                temporaryStateOfSelectedIds.clear();
                temporaryStateOfSelectedIds.add(event.getId());

            }
        });

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
        Command command = new Command("fetchWidgetItems", "widget-items-handler", widgetItemsRequest);
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


}