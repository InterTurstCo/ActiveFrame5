package ru.intertrust.cm.core.gui.impl.client.form.widget.editabletablebrowser;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.EditableTableBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.EditableTableBrowserParams;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.DefaultConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.SelectConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser.CollectionDialogBox;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser.ViewHolder;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPluginView;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.filters.WidgetIdComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.*;

import static ru.intertrust.cm.core.gui.model.util.WidgetUtil.getLimit;


/**
 * Created by Ravil on 26.09.2017.
 */
@ComponentName("editable-table-browser")
public class EditableTableBrowserWidget extends BaseWidget implements HierarchicalCollectionEventHandler {

    private HandlerRegistration checkBoxRegistration;
    private HandlerRegistration rowSelectedRegistration;
    private CollectionPlugin collectionPlugin;
    private String collectionName;
    private FlowPanel rootFlowPanel;
    private StretchyTextArea textArea;
    private ConfiguredButton addButton;
    private ConfiguredButton addDefaultButton;
    private LazyLoadState lazyLoadState;
    private HandlerRegistration expandHierarchyRegistration;
    private EventBus localEventBus = new SimpleEventBus();
    private ViewHolder viewHolder;
    private List<BreadCrumbItem> breadCrumbItems = new ArrayList<>();
    private List<String> items;
    protected CollectionDialogBox dialogBox;
    protected CollectionViewerConfig initialCollectionViewerConfig;
    protected EditableTableBrowserState currentState;

    private ScrollPanel scrollPanel;

    private CellList<String> cellList;


    public EditableTableBrowserWidget() {
        scrollPanel = new ScrollPanel();
        rootFlowPanel = new FlowPanel();
        textArea = new StretchyTextArea();
        rootFlowPanel.add(textArea);
        rootFlowPanel.addStyleName("root-editable-tablebrowser-widget");
        textArea.addStyleName("textarea-editable-tablebrowser-widget");
        scrollPanel.addStyleName("scroll-panel-tablebrowser-widget");


        TextCell textCell = new TextCell();
        cellList = new CellList<>(textCell);
        cellList.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);

        // Add a selection model to handle user selection.
        final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>();
        cellList.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                String selected = selectionModel.getSelectedObject();
                if (selected != null) {
                    textArea.setText(selected);
                    scrollPanel.setVisible(false);
                    textArea.setFocus(true);
                }
            }
        });
        cellList.addDomHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                int key = keyUpEvent.getNativeEvent().getKeyCode();
                if (key == KeyCodes.KEY_ENTER) {
                    keyUpEvent.getNativeEvent().stopPropagation();
                    keyUpEvent.getNativeEvent().preventDefault();
                    String selected = items.get(cellList.getKeyboardSelectedRow());
                    cellList.getSelectionModel().setSelected(selected,true);
                    scrollPanel.setVisible(false);
                }
                if(key == KeyCodes.KEY_ESCAPE){
                    scrollPanel.setVisible(false);
                    textArea.setFocus(true);
                }
            }
        },KeyUpEvent.getType());


        cellList.getKeyboardSelectedRow();
        scrollPanel.add(cellList);
        rootFlowPanel.add(scrollPanel);
        scrollPanel.setVisible(false);


        scrollPanel.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent mouseOutEvent) {
                scrollPanel.setVisible(false);
                textArea.setFocus(true);
            }
        },MouseOutEvent.getType());
    }

    @Override
    public Component createNew() {
        return new EditableTableBrowserWidget();
    }

    @Override
    public void setValue(Object value) {
        //TODO: Implementation required
    }

    @Override
    public void disable(Boolean isDisabled) {
        //TODO: Implementation required
    }

    @Override
    public void reset() {
        //TODO: Implementation required
    }

    @Override
    public void applyFilter(String value) {
        //TODO: Implementation required
    }

    @Override
    public Object getValueTextRepresentation() {
        return getValue();
    }

    @Override
    public void setCurrentState(WidgetState state) {
        this.currentState = (EditableTableBrowserState) state;
        ((TextArea) ((FlowPanel) impl).getWidget(0)).setText(currentState.getText());
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    protected boolean isChanged() {
        if (currentState.getEditableTableBrowserConfig().getFieldPathConfig() == null)
            return false;
        else {
            String initValue = trim(((EditableTableBrowserState) getInitialData()).getText());
            final String currentValue = trim(((TextArea) ((FlowPanel) impl).getWidget(0)).getText());
            return initValue == null ? currentValue != null : !initValue.equals(currentValue);
        }
    }

    @Override
    protected WidgetState createNewState() {
        EditableTableBrowserState nState = new EditableTableBrowserState();
        nState.setText(((TextArea) ((FlowPanel) impl).getWidget(0)).getText());
        return nState;
    }

    protected class OpenCollectionClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            breadCrumbItems.clear();
            currentState.setTemporaryState(true);
            initDialogView();
        }
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        currentState = (EditableTableBrowserState) state;
        viewHolder = new EditableTableBrowserViewsBuilder()
                .withEventBus(localEventBus)
                .withEditable(true)
                .withState(currentState)
                .withOpenCollectionButtonHandler(new EditableTableBrowserWidget.OpenCollectionClickHandler())
                .withWidgetDisplayConfig(getDisplayConfig())
                .withParentWidget(this)
                .buildViewHolder();
        textArea.setEnabled(true);

        addButton = new SelectConfiguredButton(currentState.getEditableTableBrowserConfig().getSelectButtonConfig());
        addDefaultButton = new DefaultConfiguredButton(currentState.getEditableTableBrowserConfig().getDefaultButtonConfig());
        rootFlowPanel.add(addButton);
        rootFlowPanel.add(addDefaultButton);
        addDefaultButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                getDefaultValue();
            }
        });

        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                currentState.setTemporaryState(true);
                initDialogView();
            }
        });

        if (!currentState.getEditableTableBrowserConfig().isEnterKeyAllowed()) {
            textArea.addKeyDownHandler(new KeyDownHandler() {
                @Override
                public void onKeyDown(KeyDownEvent event) {
                    int key = event.getNativeEvent().getKeyCode();
                    if (key == KeyCodes.KEY_ENTER) {
                        event.stopPropagation();
                        event.preventDefault();
                    }
                }
            });
            textArea.addKeyPressHandler(new KeyPressHandler() {
                @Override
                public void onKeyPress(KeyPressEvent event) {
                    int key = event.getNativeEvent().getKeyCode();
                    if (key == KeyCodes.KEY_ENTER) {
                        event.stopPropagation();
                        event.preventDefault();
                    }

                }
            });
            textArea.addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    int key = event.getNativeEvent().getKeyCode();
                    if (key == KeyCodes.KEY_ENTER) {
                        event.stopPropagation();
                        event.preventDefault();
                    }
                    if (key == KeyCodes.KEY_DOWN || key == KeyCodes.KEY_UP) {
                        event.stopPropagation();
                        event.preventDefault();
                        cellList.setFocus(true);
                    }
                    if(key != KeyCodes.KEY_DELETE && key != KeyCodes.KEY_BACKSPACE && !event.isControlKeyDown()
                            && key != KeyCodes.KEY_CTRL &&
                            currentState.getEditableTableBrowserConfig().isAutosuggestAllowed()
                            && textArea.getText().length()>=3 &&
                            key != KeyCodes.KEY_DOWN && key != KeyCodes.KEY_UP && key != KeyCodes.KEY_ENTER) {
                        fetchSuggestions(textArea.getText());
                        event.stopPropagation();
                        event.preventDefault();
                    }
                }
            });
            textArea.addValueChangeHandler(new ValueChangeHandler() {
                @Override
                public void onValueChange(ValueChangeEvent changeEvent) {
                    if (textArea.getText().contains("\n")) {
                        textArea.setText(textArea.getText().replace("\n", " "));
                    }
                }
            });
        }



        return rootFlowPanel;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        currentState = (EditableTableBrowserState) state;
        textArea.setEnabled(false);
        return rootFlowPanel;
    }

    private void getDefaultValue() {
        EditableBrowserRequestData rData = new EditableBrowserRequestData();
        rData.setConfig(((EditableTableBrowserState) currentState).getWidgetConfig());
        if (getContainer().getPlugin() instanceof FormPlugin) {
            rData.setFormState((((FormPlugin) getContainer().getPlugin())).getFormState());
        }

        Command command = new Command("getDefaultValue", "editable-table-browser", rData);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                EditableBrowserRequestData response = (EditableBrowserRequestData) result;
                textArea.clear();
                textArea.setValue(response.getDefaultValue());
            }

            @Override
            public void onFailure(Throwable caught) {

            }
        });
    }

    protected void initDialogView() {
        dialogBox = new EditableTableBrowserViewsBuilder().withState(currentState).buildCollectionDialogBox();
        if (currentState.isSingleChoice()) {
            addClickHandlersForSingleChoice(dialogBox);
        } else {
            addClickHandlersForMultiplyChoice(dialogBox);
        }
        initialCollectionViewerConfig = initCollectionConfig(false, null);
        openCollectionPlugin(initialCollectionViewerConfig, null, dialogBox.getPluginPanel());
        dialogBox.center();
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

    @Override
    public void onExpandHierarchyEvent(HierarchicalCollectionEvent event) {

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

    private void fetchTableBrowserItems() {
        if (!currentState.getSelectedIds().isEmpty()) {
            EditableTableBrowserConfig tableBrowserConfig = currentState.getEditableTableBrowserConfig();
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
                    currentState.getSelectedIds().clear();
                }

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("something was going wrong while obtaining rows");
                }
            });

        }
    }

    private void handleItems(LinkedHashMap<Id, String> listValues) {
        for (Id itm : listValues.keySet()) {
            textArea.setText(textArea.getText().trim().concat(" " + listValues.get(itm)));
        }
    }

    private void putToCorrectContent() {
        int limit = getLimit(currentState.getEditableTableBrowserConfig().getSelectionFiltersConfig());
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

    private void displayItems() {
        viewHolder.setContent(currentState);
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
            PluginPanel pluginPanel = new EditableTableBrowserViewsBuilder().withState(currentState).createDialogCollectionPluginPanel();
            openCollectionPlugin(config, new NavigationConfig(), dialogBox.getPluginPanel());
            //TODO: adding to history makes the rows to be highlighted. can we just check checkbox without highlighting?
            Application.getInstance().getHistoryManager().setSelectedIds(currentState.getTemporarySelectedIds().toArray(
                    new Id[currentState.getTemporarySelectedIds().size()]));
        }
    }

    protected CollectionViewerConfig initCollectionConfig(Boolean displayOnlyChosenIds, Boolean displayCheckBoxes) {
        CollectionViewerConfig collectionViewerConfig = new CollectionViewerConfig();
        CollectionViewRefConfig collectionViewRefConfig = new CollectionViewRefConfig();
        EditableTableBrowserConfig tableBrowserConfig = currentState.getEditableTableBrowserConfig();
        EditableTableBrowserParams tableBrowserParams = createTableBrowserParams(displayOnlyChosenIds, displayCheckBoxes);
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

    protected EditableTableBrowserParams createTableBrowserParams(Boolean displayOnlyChosenIds, Boolean displayCheckBoxes) {
        EditableTableBrowserConfig tableBrowserConfig = currentState.getEditableTableBrowserConfig();
        EditableTableBrowserParams tableBrowserParams = new EditableTableBrowserParams()
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

    private boolean isDisplayChosenValues(Boolean displayOnlyIncludedIds, Boolean displayCheckBoxes) {
        EditableTableBrowserConfig config = currentState.getWidgetConfig();
        boolean displayChosenValuesFromConfig = config.getDisplayChosenValues() != null && config.getDisplayChosenValues().isDisplayChosenValues();
        boolean displayChosenValuesForWidgetPurposes = !displayOnlyIncludedIds && displayCheckBoxes != null && displayCheckBoxes;
        return displayChosenValuesForWidgetPurposes || displayChosenValuesFromConfig;

    }

    private ComplexFiltersParams createFiltersParams() {
        Collection<WidgetIdComponentName> widgetsIdsComponentNames = currentState.getExtraWidgetIdsComponentNames();
        String filterName = null;
        //String filterValue = viewHolder.getChildViewHolder() == null ? null
        //        : ((TableBrowserItemsView) (viewHolder.getChildViewHolder().getWidget())).getFilterValue();
        WidgetsContainer container = getContainer();
        return GuiUtil.createComplexFiltersParams(null, filterName, container, widgetsIdsComponentNames);

    }

    private ComplexFiltersParams createSuggestionFiltersParams(String requestQuery) {
        Collection<WidgetIdComponentName> widgetsIdsComponentNames = currentState.getExtraWidgetIdsComponentNames();
        String filterName = currentState.getEditableTableBrowserConfig().getInputTextFilterConfig().getName();
        WidgetsContainer container = getContainer();
        return GuiUtil.createComplexFiltersParams(requestQuery, filterName, container, widgetsIdsComponentNames);
    }

    private SuggestionRequest createSuggestionRequest(String requestQuery) {
        SuggestionRequest result = new SuggestionRequest();
        String name = currentState.getEditableTableBrowserConfig().getCollectionRefConfig().getName();
        result.setCollectionName(name);
        String dropDownPatternConfig = currentState.getEditableTableBrowserConfig().getDropdownPatternConfig().getValue();
        result.setDropdownPattern(dropDownPatternConfig);
        result.setSelectionPattern(currentState.getEditableTableBrowserConfig().getSelectionPatternConfig().getValue());
        result.setExcludeIds(new LinkedHashSet<Id>(currentState.getSelectedIds()));
        result.setComplexFiltersParams(createSuggestionFiltersParams(requestQuery));
        result.setDefaultSortCriteriaConfig(currentState.getEditableTableBrowserConfig().getDefaultSortCriteriaConfig());
        result.setFormattingConfig(currentState.getEditableTableBrowserConfig().getFormattingConfig());
        result.setCollectionExtraFiltersConfig(currentState.getEditableTableBrowserConfig().getCollectionExtraFiltersConfig());
        if (lazyLoadState == null) {
            lazyLoadState = new LazyLoadState(currentState.getEditableTableBrowserConfig().getPageSize(), 0);
        } else {
            lazyLoadState.setPageSize(currentState.getEditableTableBrowserConfig().getPageSize());
        }
        result.setLazyLoadState(lazyLoadState);
        return result;
    }

    private void fetchSuggestions(final String query){
        SuggestionRequest sRequest = createSuggestionRequest(query);
        Command command = new Command("obtainSuggestions", getName(), sRequest);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                SuggestionList suggestionResponse = (SuggestionList) result;
                if (suggestionResponse.getSuggestions().size()>0){
                    items = new ArrayList<>();
                    for(SuggestionItem sItem : suggestionResponse.getSuggestions()){
                        items.add(sItem.getDisplayText());
                    }
                    cellList.setRowCount(suggestionResponse.getSuggestions().size(), true);
                    cellList.setRowData(0, items);
                    scrollPanel.setVisible(true);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining suggestions for '" + query + "'", caught);
            }
        });
    }
}
