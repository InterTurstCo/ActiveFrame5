package ru.intertrust.cm.core.gui.impl.client.plugins.plugin;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
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
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.AttachmentBoxWidget;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.LabledCheckboxCell;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.plugin.ExecutePluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginInfoData;
import ru.intertrust.cm.core.gui.model.plugin.UploadData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.EXECUTION_ACTION_ERROR_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.EXECUTION_ACTION_ERROR;

public class PluginManagerView extends PluginView {

    private Panel mainPanel = new VerticalPanel();
    private AttachmentBoxWidget attachmentBox;
    private CellTable<PluginInfo> cellTable;
    private Panel toolbarPanel = new HorizontalPanel();
    private SimplePager pager;
    private Button uploadButton;
    private ListDataProvider<PluginInfo> dataProvider = new ListDataProvider<PluginInfo>();

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
                for(PluginInfo pluginInfo : dataProvider.getList()) {
                    if(pluginInfo.isChecked()) {
                        executePlugin(pluginInfo, dialogBox.getResult());
                    }
                }
            }
        });

        initTableToolbar();
        mainPanel.add(toolbarPanel);

        cellTable = new CellTable<PluginInfo>(50);
        //cellTable.setWidth("100%", true);
        cellTable.addStyleName("cellTable");

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

        Application.getInstance().hideLoadingIndicator();
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
            }

            @Override
            public void onSuccess(Dto result) {
                ApplicationWindow.infoAlert(result.toString());
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
            }

            @Override
            public void onSuccess(Dto result) {
                PluginInfoData data = (PluginInfoData)result;
                dataProvider.getList().clear();
                dataProvider.getList().addAll(data.getPluginInfos());
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

    }


    private void initTableToolbar() {
        Button actionButton = new Button();
        actionButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().addDoBtn());
        actionButton.addStyleName("add-btn-table-viewer");
        actionButton.addStyleName("btn-perform");
        actionButton.setTitle("Выполнить");
        toolbarPanel.add(actionButton);


        actionButton.addClickHandler(new ClickHandler() {
                                         @Override
                                         public void onClick(ClickEvent event) {
                                             dialogBox.showDialogBox();

                                         }
                                   }
        );

    }

}
