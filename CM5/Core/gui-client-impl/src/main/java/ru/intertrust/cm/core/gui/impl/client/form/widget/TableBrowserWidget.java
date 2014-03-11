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
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.form.FacebookStyleView;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.ButtonForm;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 11:15
 */
@ComponentName("table-browser")
public class TableBrowserWidget extends BaseWidget implements HyperlinkStateChangedEventHandler {
    public static final int DEFAULT_DIALOG_WIDTH = 500;
    public static final int DEFAULT_DIALOG_HEIGHT = 300;
    private TableBrowserConfig tableBrowserConfig;
    private PluginPanel pluginPanel;
    private FocusPanel openDialogButton;
    private FocusPanel clearButton;
    private TextBox filterEditor;
    private EventBus localEventBus = new SimpleEventBus();
    private ArrayList<Id> chosenIds = new ArrayList<Id>();
    private boolean singleChoice;
    private int dialogWidth;
    private int dialogHeight;
    private DialogBox dialogBox;
    private FacebookStyleView facebookStyleView;
    FlowPanel root = new FlowPanel();

    @Override
    public void setCurrentState(WidgetState currentState) {
        TableBrowserState tableBrowserState = (TableBrowserState) currentState;
        tableBrowserConfig = tableBrowserState.getTableBrowserConfig();
        if (isEditable()) {
            setCurrentStateForEditableWidget(tableBrowserState);
        } else {
            setCurrentStateForNoneEditableWidget(tableBrowserState);
        }
    }

    private void setCurrentStateForEditableWidget(TableBrowserState state) {

        singleChoice = state.isSingleChoice();
        facebookStyleView.setChosenItems(state.getTableBrowserItems());
        initDialogWindowSize();
        initDialogView();
        initAddButton();
        initClearAllButton();
        if (displayHyperlinks()) {
            facebookStyleView.setEventBus(localEventBus);
            facebookStyleView.displayHyperlinkItems();

        } else {
            facebookStyleView.displaySelectedItems();
        }
    }

    private void setCurrentStateForNoneEditableWidget(TableBrowserState state) {
        SimpleNoneEditablePanelWithHyperlinks noneEditablePanel = (SimpleNoneEditablePanelWithHyperlinks) impl;
        List<TableBrowserItem> tableBrowserItems = state.getTableBrowserItems();

        if (displayHyperlinks()) {
            for (TableBrowserItem item : tableBrowserItems) {
               displayHyperlink(item, noneEditablePanel);
            }
        } else {
            for (TableBrowserItem tableBrowserItem : tableBrowserItems) {
                String representation = tableBrowserItem.getStringRepresentation();
                noneEditablePanel.displayItem(representation);
            }
        }
    }

    @Override
    public TableBrowserState getCurrentState() {
        if (isEditable()) {
            TableBrowserState state = new TableBrowserState();
            state.setTableBrowserItems(facebookStyleView.getChosenItems());
            return state;
        } else {
            return getInitialData();
        }
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        commonInitialization(state);
        SelectionStyleConfig selectionStyleConfig = tableBrowserConfig.getSelectionStyleConfig();
        return initWidgetView(selectionStyleConfig);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        commonInitialization(state);
        SelectionStyleConfig selectionStyleConfig = tableBrowserConfig.getSelectionStyleConfig();
        return new SimpleNoneEditablePanelWithHyperlinks(selectionStyleConfig,localEventBus);

    }

    private void commonInitialization(WidgetState state) {
        TableBrowserState tableBrowserState = (TableBrowserState) state;
        tableBrowserConfig = tableBrowserState.getTableBrowserConfig();
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);

    }

    private boolean displayHyperlinks() {
        DisplayValuesAsLinksConfig displayValuesAsLinksConfig = tableBrowserConfig.getDisplayValuesAsLinksConfig();
        return displayValuesAsLinksConfig != null && displayValuesAsLinksConfig.isValue();
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
        SearchAreaRefConfig searchAreaRefConfig = new SearchAreaRefConfig();
        searchAreaRefConfig.setName(tableBrowserConfig.getInputTextFilterConfig().getName());
        searchAreaRefConfig.setValue(filterEditor.getValue());
        collectionViewRefConfig.setName(tableBrowserConfig.getCollectionViewRefConfig().getName());
        CollectionRefConfig collectionRefConfig = new CollectionRefConfig();
        collectionRefConfig.setName(tableBrowserConfig.getCollectionRefConfig().getName());
        DefaultSortCriteriaConfig defaultSortCriteriaConfig = tableBrowserConfig.getDefaultSortCriteriaConfig();
        collectionViewerConfig.setDefaultSortCriteriaConfig(defaultSortCriteriaConfig);
        collectionViewerConfig.setCollectionRefConfig(collectionRefConfig);
        collectionViewerConfig.setCollectionViewRefConfig(collectionViewRefConfig);
        collectionViewerConfig.setSearchAreaRefConfig(searchAreaRefConfig);
        collectionViewerConfig.setSingleChoice(singleChoice);
        collectionViewerConfig.setDisplayChosenValues(tableBrowserConfig.getDisplayChosenValues().isDisplayChosenValues());
        collectionViewerConfig.setExcludedIds(facebookStyleView.getChosenIds());
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
        facebookStyleView = new FacebookStyleView(selectionStyleConfig);
        filterEditor = new TextBox();
        filterEditor.getElement().setClassName("table-browser-filter-editor");
        openDialogButton = new FocusPanel();
        openDialogButton.addClickHandler(new FetchFilteredRowsClickHandler());
        clearButton = new FocusPanel();
        clearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                chosenIds.clear();
                facebookStyleView.getChosenItems().clear();
                facebookStyleView.displaySelectedItems();

            }
        });
        root.add(filterEditor);
        root.add(openDialogButton);
        root.add(clearButton);
        root.add(facebookStyleView);
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        return root;
    }

    private void initAddButton() {
        openDialogButton.clear();
        ButtonForm addButton;
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
        dialogBox.addStyleName("table-browser-dialog");
        dialogBox.getElement().getStyle().setZIndex(10);
        initCollectionConfig();
        initCollectionPluginPanel();
        Button okButton = new Button("OK");
        okButton.removeStyleName("gwt-Button");
        okButton.addStyleName("dialog-box-button buttons-fixed");
        Button cancelButton = new Button("CANCEL");
        cancelButton.removeStyleName("gwt-Button");
        cancelButton.addStyleName("dialog-box-button buttons-fixed position-margin-left");
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
        final SingleChoiceConfig singleChoice = tableBrowserConfig.getSingleChoice();
        return singleChoice != null && singleChoice.isSingleChoice();
    }

    private void addCancelButtonClickHandler(final Button cancelButton, final DialogBox dialogBox) {

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
    }

    @Override
    public void onHyperlinkStateChangedEvent(HyperlinkStateChangedEvent event) {
        Id id = event.getId();
        updateHyperlink(id, tableBrowserConfig.getSelectionPatternConfig().getValue());
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
                fetchParsedRows();
                dialogBox.hide();
            }
        });
        localEventBus.addHandler(CheckBoxFieldUpdateEvent.TYPE, new CheckBoxFieldUpdateEventHandler() {
            @Override
            public void onCheckBoxFieldUpdate(CheckBoxFieldUpdateEvent event) {
                if (event.isDeselected()) {
                    chosenIds.remove(event.getId());
                    facebookStyleView.removeChosenItem(event.getId());
                } else {
                    chosenIds.add(event.getId());
                }
            }
        });

    }

    private void addClickHandlersForSingleChoice(final Button okButton, final Button cancelButton, final DialogBox dialogBox) {
        addCancelButtonClickHandler(cancelButton, dialogBox);
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                fetchParsedRows();
                dialogBox.hide();
            }
        });
        localEventBus.addHandler(CollectionRowSelectedEvent.TYPE, new CollectionRowSelectedEventHandler() {
            @Override
            public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
                chosenIds.clear();
                chosenIds.add(event.getId());

            }
        });

    }

    private void initDialogWindowSize() {
        DialogWindowConfig dialogWindowConfig = tableBrowserConfig.getDialogWindowConfig();
        String widthString = dialogWindowConfig != null ? dialogWindowConfig.getWidth() : null;
        String heightString = dialogWindowConfig != null ? dialogWindowConfig.getHeight() : null;
        dialogWidth = widthString == null ? DEFAULT_DIALOG_WIDTH : Integer.parseInt(widthString.replaceAll("\\D+", ""));
        dialogHeight = heightString == null ? DEFAULT_DIALOG_HEIGHT : Integer.parseInt(heightString.replaceAll("\\D+", ""));
    }

    private void fetchParsedRows() {

        if (chosenIds.isEmpty()) {
            facebookStyleView.displaySelectedItems();
            return;
        }
        FormatRowsRequest formatRowsRequest = new FormatRowsRequest();
        formatRowsRequest.setSelectionPattern(tableBrowserConfig.getSelectionPatternConfig().getValue());
        formatRowsRequest.setIdsShouldBeFormatted(chosenIds);

        Command command = new Command("fetchParsedRows", getName(), formatRowsRequest);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                ParsedRowsList list = (ParsedRowsList) result;
                ArrayList<TableBrowserItem> items = list.getFilteredRows();
                if (isSingleChoice()) {
                    facebookStyleView.setChosenItems(items);
                } else {
                    facebookStyleView.getChosenItems().addAll(items);
                }
                facebookStyleView.displaySelectedItems();
                chosenIds.clear();
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });

    }

    private void updateHyperlink(Id id, String selectionPattern) {
        HyperlinkUpdateRequest request = new HyperlinkUpdateRequest(id, selectionPattern);
        Command command = new Command("updateHyperlink", "linked-domain-object-hyperlink", request);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                HyperlinkUpdateResponse response = (HyperlinkUpdateResponse) result;
                Id id = response.getId();
                String representation = response.getRepresentation();
                TableBrowserItem updatedItem = new TableBrowserItem(id, representation);
                if (isEditable()) {

                    facebookStyleView.updateHyperlinkItem(updatedItem);
                } else {
                    SimpleNoneEditablePanelWithHyperlinks noneEditablePanel = (SimpleNoneEditablePanelWithHyperlinks) impl;
                    noneEditablePanel.cleanPanel();
                    List<TableBrowserItem> items = getUpdatedHyperlinks(updatedItem);
                    for (TableBrowserItem item : items) {
                         displayHyperlink(item, noneEditablePanel);
                    }
                }

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink");
            }
        });
    }

    private void displayHyperlink(TableBrowserItem item, SimpleNoneEditablePanelWithHyperlinks noneEditablePanel){
        Id itemId = item.getId();
        String itemRepresentation = item.getStringRepresentation();
        noneEditablePanel.displayHyperlink(itemId, itemRepresentation);
    }

    private List<TableBrowserItem> getUpdatedHyperlinks(TableBrowserItem updatedItem) {
        TableBrowserState state = getInitialData();
        Id idToFind = updatedItem.getId();
        List<TableBrowserItem> items = state.getTableBrowserItems();
        for (TableBrowserItem item : items) {
            if (idToFind.equals(item.getId())) {
                int index = items.indexOf(item);
                items.set(index, updatedItem);
            }
        }
        return items;
    }
}