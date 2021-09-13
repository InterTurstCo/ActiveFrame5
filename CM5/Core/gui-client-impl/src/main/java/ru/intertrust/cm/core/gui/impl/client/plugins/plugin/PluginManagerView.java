package ru.intertrust.cm.core.gui.impl.client.plugins.plugin;

import com.google.gwt.cell.client.*;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.plugin.PluginInfo;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.event.LeftPanelAttachedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.LeftPanelAttachedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.AttachmentBoxWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.messagedialog.InfoMessageDialog;
import ru.intertrust.cm.core.gui.impl.client.form.widget.messagedialog.MessageDialog;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.LabledCheckboxCell;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.DownloadAttachmentActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.plugin.ExecutePluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginInfoData;
import ru.intertrust.cm.core.gui.model.plugin.TerminatePluginData;
import ru.intertrust.cm.core.gui.model.plugin.UploadData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.*;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.EXECUTION_ACTION_ERROR_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.EXECUTION_ACTION_ERROR;

public class PluginManagerView extends PluginView implements LeftPanelAttachedEventHandler {

    public static final String DATE_TIME_PATTERN = "dd-MM-yyyy HH:mm:ss";
    private Panel mainPanel = new AbsolutePanel();
    private AttachmentBoxWidget attachmentBox;
    private CellTable<PluginInfo> cellTable;
    private Panel toolbarPanel = new HorizontalPanel();
    private SimplePager pager;
    private Button uploadButton;
    private Button executeButton;
    private Button terminateButton;
    private Button updateButton;
    private TextBox filterValue;
    private ListDataProvider<PluginInfo> dataProvider = new ListDataProvider<>();
    ColumnSortEvent.ListHandler<PluginInfo> columnSortHandler;
    private       HandlerRegistration handlerRegistration;

    private PluginManagerParamDialogBox dialogBox;

    public PluginManagerView(Plugin plugin) {
        super(plugin);
        init();
    }

    private void init() {

        mainPanel.add(new Label(LocalizeUtil.get(LocalizationKeys.ADD_PLUGIN_FILES_KEY, BusinessUniverseConstants.ADD_PLUGIN_FILES)));
        attachmentBox = createAttachmentBox();
        mainPanel.add(attachmentBox);
        mainPanel.addStyleName("wrapPluginManager");

        uploadButton = new Button("Загрузить");
        uploadButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                deployPlugins();                
            }
            
        });
        mainPanel.add(uploadButton);

        dialogBox = new PluginManagerParamDialogBox();
        dialogBox.addCancelButtonClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });

        dialogBox.addOkButtonClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
                saveParamsForSelectedPlugins(dialogBox.getResult());
                for(PluginInfo pluginInfo : dataProvider.getList()) {
                    if(pluginInfo.isChecked()) {
                        executePlugin(pluginInfo, dialogBox.getResult());
                    }
                }
                refreshPluginsModel();
            }
        });

        initTableToolbar();
        mainPanel.add(toolbarPanel);

        HorizontalPanel filterPanel = new HorizontalPanel();
        Label filterLabel = new Label("Фильтр:");
        filterValue = new TextBox();
        Button applyFilterButton = new Button("Применить");
        applyFilterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshPluginsModel();
            }
        });
        Button clearfilterButton = new Button("Очистить");
        clearfilterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                filterValue.setValue("");
                refreshPluginsModel();
            }
        });

        filterPanel.add(filterLabel);
        filterPanel.add(filterValue);
        filterPanel.add(applyFilterButton);
        filterPanel.add(clearfilterButton);
        mainPanel.add(filterPanel);

        cellTable = new CellTable<PluginInfo>(50);
        final boolean isNavigationTreePanelExpanded = Application.getInstance().getCompactModeState().isNavigationTreePanelExpanded();
        setCellTableStyle(isNavigationTreePanelExpanded);

        // Do not refresh the headers and footers every time the data is updated.
        cellTable.setAutoHeaderRefreshDisabled(true);
        cellTable.setAutoFooterRefreshDisabled(true);
        // Initialize the columns.
        initTableColumns();

        // Create a Pager to control the table.
        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
        pager.setDisplay(cellTable);
        
        dataProvider.addDataDisplay(cellTable);
        refreshPluginsModel();

        mainPanel.add(cellTable);
        mainPanel.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent attachEvent) {
                if (!attachEvent.isAttached()) {
                    clearHandlers();
                }
            }
        });
        EventBus applicationEventBus = Application.getInstance().getEventBus();
        handlerRegistration = applicationEventBus.addHandler(LeftPanelAttachedEvent.TYPE, this);

        Application.getInstance().unlockScreen();
    }

    @Override
    protected void clearHandlers() {
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
            handlerRegistration = null;
        }
    }

    /**
     * Устанавливает стиль таблицы, в зависимости от того развернута ли левая панель навигации и является ли браузер интернет-эксплорером
     * (у него свои особые стили в силу наличия некоторых ограничений)
     *
     * @param isLeftPanelExpanded развернута ли панель навигации слева
     */
    private void setCellTableStyle(boolean isLeftPanelExpanded) {
        final boolean isIE = GuiUtil.isIE();
        if (isIE) {
            if (isLeftPanelExpanded) {
                cellTable.setStyleName("cellTable-IE-left-panel-expanded");
            } else {
                cellTable.setStyleName("cellTable-IE-left-panel-collapsed");
            }
        } else {
            cellTable.setStyleName("cellTable");
        }
    }

    private void deployPlugins() {
        AttachmentBoxState state = (AttachmentBoxState)attachmentBox.getCurrentState();
        UploadData uploadData = new UploadData();
        uploadData.setAttachmentItems(state.getAttachments());        
        
        Command command = new Command("deployPlugins", "plugin.manager.plugin", uploadData);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining statistics");
                caught.printStackTrace();
                ApplicationWindow.errorAlert(LocalizeUtil.get(EXECUTION_ACTION_ERROR_KEY, EXECUTION_ACTION_ERROR) + caught.getMessage());
            }

            @Override
            public void onSuccess(Dto result) {
                clear();
                refreshPluginsModel();
                ApplicationWindow.infoAlert("Плагин установлен");
            }
        });
    }    

    
    private void executePlugin(PluginInfo pluginInfo, String param) {
        ExecutePluginData executePluginData = new ExecutePluginData();
        executePluginData.setParameter(param);
        executePluginData.setPluginId(pluginInfo.getClassName());
        
        Command command = new Command("executePlugin", "plugin.manager.plugin", executePluginData);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining statistics");
                caught.printStackTrace();
                ApplicationWindow.errorAlert(LocalizeUtil.get(EXECUTION_ACTION_ERROR_KEY, EXECUTION_ACTION_ERROR) + caught.getMessage());
                refreshPluginsModel();
            }

            @Override
            public void onSuccess(Dto result) {
                MessageDialog messageDialog = new InfoMessageDialog(result.toString());
                messageDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
                    @Override
                    public void onClose(CloseEvent<PopupPanel> event) {
                        refreshPluginsModel();
                    }
                });
                messageDialog.alert();
                refreshPluginsModel();
            }
        });
    } 

    private void terminatePluginsProcess(List<PluginInfo> pluginInfos) {
        TerminatePluginData terminatePlugins = new TerminatePluginData();
        
        //Из всех плагинов выбираем выделенные и запущенные
        for(PluginInfo pluginInfo: pluginInfos){
            if(pluginInfo.isChecked() && pluginInfo.getStatus().equals(PluginInfo.Status.Running)) {
                terminatePlugins.getPluginIds().add(pluginInfo.getClassName());
            }
        }
        
        Command command = new Command("terminatePlugins", "plugin.manager.plugin", terminatePlugins);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining statistics");
                caught.printStackTrace();
                ApplicationWindow.errorAlert(LocalizeUtil.get(EXECUTION_ACTION_ERROR_KEY, EXECUTION_ACTION_ERROR) + caught.getMessage());
                checkAndUpdatePerformButtonState();
            }

            @Override
            public void onSuccess(Dto result) {
                TerminatePluginData TerminatePluginData = (TerminatePluginData)result;
                String message = "";
                for (String pluginId : TerminatePluginData.getPluginIds()) {
                    if (!message.isEmpty()){
                        message += ", ";
                    }
                    message += pluginId;
                }
                
                if (message.isEmpty()){
                    message = "Не выбрано ни одного запущенного плагина";
                }else{
                    message = "Отправлен сигнал на остановку плагина(ов): " + message;
                }
                
                MessageDialog messageDialog = new InfoMessageDialog(message);
                messageDialog.alert();
            }
        });
        
    }
    
    private void refreshPluginsModel() {
        Command command = new Command("refreshPlugins", "plugin.manager.plugin", null);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining statistics");
                caught.printStackTrace();
                ApplicationWindow.errorAlert(LocalizeUtil.get(EXECUTION_ACTION_ERROR_KEY, EXECUTION_ACTION_ERROR) + caught.getMessage());
                checkAndUpdatePerformButtonState();
            }

            @Override
            public void onSuccess(Dto result) {
                PluginInfoData data = (PluginInfoData)result;
                dataProvider.getList().clear();
                if (filterValue.getValue().isEmpty()) {
                    dataProvider.getList().addAll(data.getPluginInfos());
                }else{
                    for (PluginInfo pluginInfo : data.getPluginInfos()) {
                        if (pluginInfo.getName().toLowerCase().contains(filterValue.getValue().toLowerCase()) ||
                                pluginInfo.getClassName().toLowerCase().contains(filterValue.getValue().toLowerCase()) ||
                                pluginInfo.getDescription().toLowerCase().contains(filterValue.getValue().toLowerCase())){
                            dataProvider.getList().add(pluginInfo);
                        }
                    }
                }
                Column currentSortColumn = cellTable.getColumnSortList().get(0).getColumn();
                if(cellTable.getColumnSortList().get(0).isAscending()) {
                    Collections.sort(dataProvider.getList(), columnSortHandler.getComparator(currentSortColumn));
                }else {
                    Collections.sort(dataProvider.getList(), Collections.reverseOrder(columnSortHandler.getComparator(currentSortColumn)));
                }
                checkAndUpdatePerformButtonState();
            }
        });
    }

    @Override
    public IsWidget getViewWidget() {
        return mainPanel;
    }

    public void clear() {
        ((AttachmentBoxState)attachmentBox.getCurrentState()).getAttachments().clear();
        attachmentBox.setCurrentState(attachmentBox.getCurrentState());
    }

    private AttachmentBoxWidget createAttachmentBox() {
        AttachmentBoxWidget attachmentBox = ComponentRegistry.instance.get("attachment-box");
        WidgetDisplayConfig displayConfig = new WidgetDisplayConfig();
        AttachmentBoxState state = new AttachmentBoxState();
        attachmentBox.setDisplayConfig(displayConfig);
        EventBus eventBus = GWT.create(SimpleEventBus.class);
        attachmentBox.setEventBus(eventBus);
        attachmentBox.setState(state);
        attachmentBox.asWidget().setStyleName("upload-report-template");

        return attachmentBox;
    }
    
    private void initTableColumns() {

        //check boxes
        Column<PluginInfo, Boolean> checkColumn = new Column<PluginInfo, Boolean>(new LabledCheckboxCell()) {

            @Override
            public Boolean getValue(PluginInfo object) {
                return object.isChecked();
            }

            @Override
            public void onBrowserEvent(Cell.Context context, Element elem, PluginInfo object, NativeEvent event) {
                super.onBrowserEvent(context, elem, object, event);
                object.setChecked(((CheckboxCell)this.getCell()).getViewData(object));
                checkAndUpdatePerformButtonState();
            }
        };

        checkColumn.setCellStyleNames("check-box-cell");
        cellTable.addColumn(checkColumn);
        cellTable.setColumnWidth(checkColumn, 10, Unit.PCT);


        // ID.
        Column<PluginInfo, String> idColumn = new Column<PluginInfo, String>(
                new TextCell()) {
            @Override
            public String getValue(PluginInfo object) {
                return object.getClassName();
            }
        };
        idColumn.setSortable(true);
        cellTable.addColumn(idColumn, "id");
        cellTable.setColumnWidth(idColumn, 20, Unit.PCT);


        // Name.
        Column<PluginInfo, String> nameColumn = new Column<PluginInfo, String>(
                new TextCell()) {
            @Override
            public String getValue(PluginInfo object) {
                return object.getName();
            }
        };
        nameColumn.setSortable(true);
        nameColumn.setDefaultSortAscending(true);
        cellTable.addColumn(nameColumn, "name");
        cellTable.setColumnWidth(nameColumn, 100, Unit.PCT);

        // Description.
        Column<PluginInfo, String> descriptionColumn = new Column<PluginInfo, String>(
                new TextCell()) {
            @Override
            public String getValue(PluginInfo object) {
                return object.getDescription();
            }
        };
        cellTable.addColumn(descriptionColumn, "description");
        cellTable.setColumnWidth(descriptionColumn, 100, Unit.PCT);


        Column<PluginInfo, Date> startPluginTimeColumn = new Column<PluginInfo, Date>(new DateCell(DateTimeFormat.getFormat(DATE_TIME_PATTERN))) {
            @Override
            public Date getValue(PluginInfo object) {
                return object.getLastStart();
            }
        };
        startPluginTimeColumn.setSortable(true);
        startPluginTimeColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        cellTable.addColumn(startPluginTimeColumn, "start plugin time");
        cellTable.setColumnWidth(startPluginTimeColumn, 100, Unit.PCT);
        startPluginTimeColumn.setCellStyleNames("start-time");


        Column<PluginInfo, Date> finishPluginTimeColumn = new Column<PluginInfo, Date>(new DateCell(DateTimeFormat.getFormat(DATE_TIME_PATTERN))) {
            @Override
            public Date getValue(PluginInfo object) {
                return object.getLastFinish();
            }
        };
        finishPluginTimeColumn.setSortable(true);
        finishPluginTimeColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        cellTable.addColumn(finishPluginTimeColumn, "finish plugin time");
        cellTable.setColumnWidth(finishPluginTimeColumn, 100, Unit.PCT);
        finishPluginTimeColumn.setCellStyleNames("finish-time");


        Column<PluginInfo, String> pluginStatusColumn = new Column<PluginInfo, String>(new TextCell()) {
            @Override
            public String getValue(PluginInfo object) {
                return object.getStatus().toString();
            }
        };
        pluginStatusColumn.setSortable(true);
        pluginStatusColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        cellTable.addColumn(pluginStatusColumn, "plugin status");
        cellTable.setColumnWidth(pluginStatusColumn, 100, Unit.PCT);


        Column<PluginInfo, String> downloadLogColumn = new Column<PluginInfo, String>(
                new ButtonCell()) {
            @Override
            public String getValue(PluginInfo object) {
                return "";
            }

            @Override
            public void onBrowserEvent(Cell.Context context, Element elem, PluginInfo object, NativeEvent event) {

                DownloadAttachmentActionContext logContext = new DownloadAttachmentActionContext();
                logContext.setId(object.getLastResult());
                final Action action = ComponentRegistry.instance.get("download.attachment.action");
                action.setInitialContext(logContext);
                action.perform();
            }
        };
        downloadLogColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        cellTable.addColumn(downloadLogColumn, "last log");
        cellTable.setColumnWidth(downloadLogColumn, 100, Unit.PCT);
        downloadLogColumn.setCellStyleNames("download-log");

        columnSortHandler = new ColumnSortEvent.ListHandler<>(dataProvider.getList());
        columnSortHandler.setComparator(idColumn, new PluginIdComparator());
        columnSortHandler.setComparator(nameColumn, new PluginNameComparator());
        columnSortHandler.setComparator(startPluginTimeColumn, new LastStartComparator());
        columnSortHandler.setComparator(finishPluginTimeColumn, new LastFinishComparator());
        columnSortHandler.setComparator(pluginStatusColumn, new PluginStatusComparator());
        cellTable.addColumnSortHandler(columnSortHandler);
        cellTable.getColumnSortList().push(nameColumn);
    }

    @Override
    public void onLeftPanelAttachedEvent(LeftPanelAttachedEvent event) {
        final Boolean isLeftPanelAttached = event.getAttached();
        setCellTableStyle(isLeftPanelAttached);
    }

    private class PluginIdComparator implements Comparator<PluginInfo> {
        public int compare(PluginInfo pluginInfo1, PluginInfo pluginInfo2) {
            if(pluginInfo1.getName() != null && pluginInfo2.getName() != null){
                String name1 = pluginInfo1.getClassName().toUpperCase();
                String name2 = pluginInfo2.getClassName().toUpperCase();
                return name1.compareTo(name2);
            }else if(pluginInfo1.getClassName() == null && pluginInfo2.getClassName() == null){
                return 0;
            }else if (pluginInfo1.getClassName() == null ){
                return 1;
            }
            return -1;
        }
    }

    private class PluginNameComparator implements Comparator<PluginInfo> {
        public int compare(PluginInfo pluginInfo1, PluginInfo pluginInfo2) {
            if(pluginInfo1.getName() != null && pluginInfo2.getName() != null){
                String name1 = pluginInfo1.getName().toUpperCase();
                String name2 = pluginInfo2.getName().toUpperCase();
                return name1.compareTo(name2);
            }else if(pluginInfo1.getName() == null && pluginInfo2.getName() == null){
                return 0;
            }else if (pluginInfo1.getName() == null){
                return 1;
            }
            return -1;
        }
    }

    private class LastStartComparator implements Comparator<PluginInfo> {
        public int compare(PluginInfo pluginInfo1, PluginInfo pluginInfo2) {
            if(pluginInfo1.getLastStart() != null && pluginInfo2.getLastStart() != null){
                return pluginInfo1.getLastStart().compareTo(pluginInfo2.getLastStart());
            }else if(pluginInfo1.getLastStart() == null && pluginInfo2.getLastStart() == null){
                return 0;
            }else if (pluginInfo1.getLastStart() == null){
                return 1;
            }
            return -1;
        }
    }

    private class LastFinishComparator implements Comparator<PluginInfo> {
        public int compare(PluginInfo pluginInfo1, PluginInfo pluginInfo2) {
            if(pluginInfo1.getLastFinish() != null && pluginInfo2.getLastFinish() != null){
                return pluginInfo1.getLastFinish().compareTo(pluginInfo2.getLastFinish());
            }else if(pluginInfo1.getLastFinish() == null && pluginInfo2.getLastFinish() == null){
                return 0;
            }else if (pluginInfo1.getLastFinish() == null){
                return 1;
            }
            return -1;
        }
    }

    private class PluginStatusComparator implements Comparator<PluginInfo> {
        public int compare(PluginInfo pluginInfo1, PluginInfo pluginInfo2) {
            if(pluginInfo1.getStatus() != null && pluginInfo2.getStatus() != null){
                return pluginInfo1.getStatus().toString().compareTo(pluginInfo2.getStatus().toString());
            }else if(pluginInfo1.getStatus() == null && pluginInfo2.getStatus() == null){
                return 0;
            }else if (pluginInfo1.getStatus() == null){
                return 1;
            }
            return -1;
        }
    }

    private void initTableToolbar() {
        executeButton = new Button();
        executeButton.setStylePrimaryName("btn-perform");
        executeButton.addStyleName(GlobalThemesManager.getCurrentTheme().commonCss().addDoBtn());
        executeButton.addStyleName("add-btn-table-viewer");
        executeButton.setEnabled(false);
        executeButton.addStyleDependentName("disabled");
        executeButton.setTitle("Выполнить");
        toolbarPanel.add(executeButton);

        executeButton.addClickHandler(new ClickHandler() {
                                          @Override
                                          public void onClick(ClickEvent event) {
                                              dialogBox.checkAndSetParamValue(dataProvider.getList());
                                              dialogBox.showDialogBox();
                                          }
                                      }
        );

        
        terminateButton = new Button();
        terminateButton.setStylePrimaryName("btn-terminate");
        terminateButton.addStyleName(GlobalThemesManager.getCurrentTheme().commonCss().addDoBtn());
        terminateButton.addStyleName("add-btn-table-viewer");
        terminateButton.setEnabled(true);
        terminateButton.setTitle("Остановить");
        toolbarPanel.add(terminateButton);

        terminateButton.addClickHandler(new ClickHandler() {
                                          @Override
                                          public void onClick(ClickEvent event) {
                                              terminatePluginsProcess(dataProvider.getList());
                                          }
                                      }
        );
        
        updateButton = new Button();
        updateButton.setStylePrimaryName("btn-update");
        updateButton.addStyleName(GlobalThemesManager.getCurrentTheme().commonCss().addDoBtn());
        updateButton.addStyleName("add-btn-table-viewer");
        updateButton.setTitle("Обновить");
        toolbarPanel.add(updateButton);


        updateButton.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                  refreshPluginsModel();
              }
          });

    }

    private void saveParamsForSelectedPlugins(String param){
        for(PluginInfo pluginInfo : dataProvider.getList()){
            if(pluginInfo.isChecked()){
                Cookies.setCookie(pluginInfo.getClassName(), param);
            }
        }
    }

    private void checkAndUpdatePerformButtonState() {
        if(isSelectedPluginExist()){
            executeButton.setEnabled(true);
            executeButton.removeStyleDependentName("disabled");
        }else {
            executeButton.setStyleDependentName("disabled", true);
            executeButton.setEnabled(false);

        }
    }

    private boolean isSelectedPluginExist() {
        boolean selected = false;
        for(PluginInfo pluginInfo : dataProvider.getList()) {
            if(pluginInfo.isChecked()){
                selected = true;
                break;
            }
        }
        return selected;

    }
}
