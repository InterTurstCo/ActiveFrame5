package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.InputTextFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.FacebookStyleView;
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
public class TableBrowserWidget extends BaseWidget {
    private TableBrowserConfig tableBrowserConfig;
    private PluginPanel pluginPanel;
    private Button openDialogButton;
    private TextBox filterEditor;
    private EventBus eventBus = new SimpleEventBus();
    private ArrayList<TableBrowserRowItem> selectedItemsRepresentations = new ArrayList<TableBrowserRowItem>();
    private ArrayList<Id> selectedIds = new ArrayList<Id>();
    private ArrayList<Id> temporarySelectedIds = new ArrayList<Id>();
    private int width;
    private int height;
    private DialogBox dialogBox;
    private FacebookStyleView facebookStyleView;

    @Override
    public void setCurrentState(WidgetState currentState) {
        TableBrowserState tableBrowserState = (TableBrowserState) currentState;
        tableBrowserConfig = tableBrowserState.getTableBrowserConfig();
        facebookStyleView.initDisplayStyle(tableBrowserConfig.getSelectionStyleConfig().getName());
        initSizes();
        selectedItemsRepresentations = tableBrowserState.getSelectedItemsRepresentations();
        initDialogView();
    }

    @Override
    public TableBrowserState getCurrentState() {
        TableBrowserState state = new TableBrowserState();
        state.setSelectedItemsRepresentations(facebookStyleView.getRowItems());

        return state;
    }

    @Override
    protected Widget asEditableWidget() {

        return initWidgetView();
    }

   private void fetchParsedRows() {

        ParseRowsRequest parseRowsRequest = new ParseRowsRequest();
        String name = tableBrowserConfig.getCollectionRefConfig().getName();
        parseRowsRequest.setCollectionName(name);

        TableBrowserState tableBrowserState = getCurrentState();
     //   parseRowsRequest.setColumnFields(view.getDomainObjectFieldOnColumnNameMap());
        parseRowsRequest.setSelectionPattern(tableBrowserConfig.getSelectionPatternConfig().getValue());

        parseRowsRequest.setExcludeIds(selectedIds);
        parseRowsRequest.setInputTextFilterName(tableBrowserConfig.getInputTextFilterConfig().getName());
        parseRowsRequest.setIdsExclusionFilterName(tableBrowserConfig.getSelectionExcludeFilterConfig().getName());

        Command command = new Command("fetchParsedRows", getName(), parseRowsRequest);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                ParsedRowsList list = (ParsedRowsList) result;

                List<TableBrowserRowItem> items = list.getFilteredRows();
                selectedItemsRepresentations.addAll(items);
                facebookStyleView.setRowItems(selectedItemsRepresentations);
                facebookStyleView.showSelectedItems();
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });

    }

    @Override
    protected Widget asNonEditableWidget() {
        return asEditableWidget();
    }

    @Override
    public Component createNew() {
        return new TableBrowserWidget();
    }

    private void initCollectionPluginPanel(){
        pluginPanel = new PluginPanel();
        pluginPanel.setVisibleWidth(width);
        pluginPanel.setVisibleHeight(height);

    }

    private CollectionViewerConfig initCollectionConfig() {
        CollectionViewerConfig collectionViewerConfig = new CollectionViewerConfig();
        CollectionViewRefConfig collectionViewRefConfig = new CollectionViewRefConfig();
        InputTextFilterConfig inputTextFilterConfig = new InputTextFilterConfig();
        inputTextFilterConfig.setName("byText");
        inputTextFilterConfig.setValue(filterEditor.getValue());
        collectionViewRefConfig.setName(tableBrowserConfig.getCollectionViewRefConfig().getName());
        CollectionRefConfig collectionRefConfig = new CollectionRefConfig();
        collectionRefConfig.setName(tableBrowserConfig.getCollectionRefConfig().getName());
        collectionViewerConfig.setCollectionRefConfig(collectionRefConfig);
        collectionViewerConfig.setCollectionViewRefConfig(collectionViewRefConfig);
        collectionViewerConfig.setInputTextFilterConfig(inputTextFilterConfig);
        collectionViewerConfig.setSingleChoice(tableBrowserConfig.getSingleChoice().isSingleChoice());
        collectionViewerConfig.setDisplayChosenValues(tableBrowserConfig.getDisplayChosenValues().isDisplayChosenValues());
        collectionViewerConfig.setExcludedIds(selectedIds);
        return collectionViewerConfig;
    }

    private void openCollectionPlugin(){

        CollectionPlugin collectionPlugin = ComponentRegistry.instance.get("collection.plugin");
        CollectionViewerConfig collectionViewerConfig = initCollectionConfig();
        collectionPlugin.setConfig(collectionViewerConfig);
        collectionPlugin.setEventBus(eventBus);
        pluginPanel.open(collectionPlugin);
        dialogBox.center();

    }

    private FlowPanel initWidgetView() {
        FlowPanel root = new FlowPanel();
        facebookStyleView = new FacebookStyleView();
        facebookStyleView.setRowItems(selectedItemsRepresentations);
        filterEditor = new TextBox();
        openDialogButton = new Button("ADD");
        openDialogButton.addClickHandler(new FetchFilteredRowsClickHandler());
        root.add(filterEditor);
        root.add(openDialogButton);
        root.add(facebookStyleView);

        return root;
    }

    private void initDialogView() {

        dialogBox = new DialogBox();
        dialogBox.getElement().getStyle().setZIndex(10);
        initCollectionConfig();
        initCollectionPluginPanel();
        Button okButton = new Button("OK");
        Button cancelButton = new Button("CANCEL");
        addClickHandlersForSingleChoice(okButton, cancelButton, dialogBox);
        HorizontalPanel buttonsContainer = new HorizontalPanel();
        buttonsContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        buttonsContainer.add(okButton);
        buttonsContainer.add(cancelButton);
        FlowPanel dialogBoxContent = new FlowPanel();

        dialogBoxContent.setWidth(width+"px");
        dialogBoxContent.getElement().getStyle().setOverflow(Style.Overflow.AUTO);
        dialogBoxContent.add(pluginPanel);
        dialogBoxContent.add(buttonsContainer);

        dialogBox.add(dialogBoxContent);

    }

    private void addCancelButtonClickHandler(final Button cancelButton, final DialogBox dialogBox) {

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
    }

    private class FetchFilteredRowsClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
                 openCollectionPlugin();
         //   fetchParsedRows(text);

        }
    }
    private void addClickHandlersForMultiplyChoice(final Button okButton, final Button cancelButton, final DialogBox dialogBox) {

        addCancelButtonClickHandler(cancelButton, dialogBox);

        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
               selectedIds.addAll(temporarySelectedIds);
                fetchParsedRows();
                dialogBox.hide();
            }
        });
        eventBus.addHandler(CollectionRowSelectedEvent.TYPE, new CollectionRowSelectedEventHandler() {
            @Override
            public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
                   if (event.isDeselected()) {
                       temporarySelectedIds.remove(event.getId());
                   }  else {
                       temporarySelectedIds.add(event.getId());
                   }

            }
        });

    }

    private void addClickHandlersForSingleChoice(final Button okButton, final Button cancelButton, final DialogBox dialogBox) {
        addCancelButtonClickHandler(cancelButton, dialogBox);
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
          //      eventBus.fireEvent(new CollectionRowDeletedEvent(selectedIds));
                selectedIds.addAll(temporarySelectedIds);
                fetchParsedRows();
                dialogBox.hide();
            }
        });
        eventBus.addHandler(CollectionRowSelectedEvent.TYPE, new CollectionRowSelectedEventHandler() {
            @Override
            public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
              temporarySelectedIds.clear();
              temporarySelectedIds.add(event.getId());

            }
        });

    }
    private void initSizes() {
        String widthString = displayConfig.getWidth();
        String heightString = displayConfig.getHeight();
        width = widthString == null ? 500 :  Integer.parseInt(widthString.replaceAll("\\D+", ""));
        height = heightString == null ? 300 :  Integer.parseInt(heightString.replaceAll("\\D+", ""));

    }

}