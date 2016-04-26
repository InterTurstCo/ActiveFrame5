package ru.intertrust.cm.core.gui.impl.client.form.widget.tableviewer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.SimpleActionConfig;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEvent;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.TableViewerData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 18.04.2016
 * Time: 11:03
 * To change this template use File | Settings | File and Code Templates.
 */
public class TableViewerToobar {
    private HorizontalPanel toolbarPanel;
    private ToggleButton editButton;
    private Button addButton;
    private MenuBar actionsMenu;
    private MenuBar fooMenu;
    private Id selectedId;
    private EventBus eventBus;
    private TableViewerData actionsData;
    private Integer multiContextToExecute = 0;
    private List<Id> selectedIds;

    public TableViewerToobar(EventBus localEventBus, List<Id> selectedIds) {
        this.eventBus = localEventBus;
        this.selectedIds = selectedIds;
        toolbarPanel = new HorizontalPanel();
        editButton = new ToggleButton();
        addButton = new Button();
        editButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().editButton());
        //editButton.addStyleName("edit-btn-table-viewer");
        editButton.addStyleName(".edit-btn-table-viewer-disable");
        editButton.setTitle("Редактировать");
        addButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().addDoBtn());
        addButton.addStyleName("add-btn-table-viewer");
        addButton.setTitle("Создать");

        editButton.addClickHandler(new ClickHandler() {
                                       @Override
                                       public void onClick(ClickEvent event) {
                                           if (selectedId != null) {
                                               eventBus.fireEvent(new OpenDomainObjectFormEvent(selectedId));
                                           }
                                       }
                                   }
        );

        toolbarPanel.add(editButton);
        toolbarPanel.add(addButton);


        actionsMenu = new MenuBar();
        fooMenu = new MenuBar(true);
        actionsMenu.setStyleName("button-table-viewer");
        fooMenu.setStyleName("wrapApproval");
        actionsMenu.addItem("", fooMenu);
        initMenu();
        toolbarPanel.add(actionsMenu);
        actionsMenu.setVisible(true);
    }

    public HorizontalPanel getToolbarPanel() {
        return toolbarPanel;
    }

    public void setToolbarPanel(HorizontalPanel toolbarPanel) {
        this.toolbarPanel = toolbarPanel;
    }

    public ToggleButton getEditButton() {
        return editButton;
    }

    public void setEditButton(ToggleButton editButton) {
        this.editButton = editButton;
    }

    public Button getAddButton() {
        return addButton;
    }

    public void setAddButton(Button addButton) {
        this.addButton = addButton;
    }

    public MenuBar getActionsMenu() {
        return actionsMenu;
    }

    public void setActionsMenu(MenuBar actionsMenu) {
        this.actionsMenu = actionsMenu;
    }

    public Id getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Id id) {
        this.selectedId = id;
        if (selectedId == null) {
            fooMenu.clearItems();
        } else {
            getActionsById(selectedId);
        }
    }


    private MenuItem buildActionButton(final ActionContext context) {
        SimpleActionContext simpleActionContext = (SimpleActionContext) context;
        SimpleActionConfig simpleActionConfig = simpleActionContext.getActionConfig();
        MenuItem menuItem = new MenuItem(simpleActionConfig.getText(), getCommandForContext(context));
        return menuItem;
    }

    private Scheduler.ScheduledCommand getCommandForContext(final ActionContext context){
        Scheduler.ScheduledCommand menuItemCommand = new Scheduler.ScheduledCommand() {
            public void execute() {
                Command command = new Command("executeAction", "generic.workflow.action", context);
                BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("something was going wrong while obtaining details for stage ");
                        Window.alert("Ошибка выполнения. " + caught.getMessage());
                        caught.printStackTrace();
                    }

                    @Override
                    public void onSuccess(Dto result) {
                        eventBus.fireEvent(new UpdateCollectionEvent(context.getRootObjectId()));
                        setSelectedId(context.getRootObjectId());
                    }
                });
            }
        };
        return menuItemCommand;
    }

    private Scheduler.ScheduledCommand getCommandForMultipleContext(final ActionContext context){
        Scheduler.ScheduledCommand menuItemCommand = new Scheduler.ScheduledCommand() {
            public void execute() {
                Command command = new Command("executeAction", "generic.workflow.action", context);
                BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("something was going wrong while obtaining details for stage ");
                        Window.alert("Ошибка выполнения. " + caught.getMessage());
                        caught.printStackTrace();
                    }

                    @Override
                    public void onSuccess(Dto result) {
                        eventBus.fireEvent(new UpdateCollectionEvent(context.getRootObjectId()));
                        multiContextToExecute--;
                        if(multiContextToExecute<=0){
                            multiContextToExecute = 0;
                            setSelectedIds(selectedIds);
                        }
                    }
                });
            }
        };
        return menuItemCommand;
    }


    public void getActionsById(final Id id) {
        Command command = new Command("getActionsById", "table-viewer", id);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while getting actions for id  " + id);
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                TableViewerData data = (TableViewerData) result;
                fooMenu.clearItems();
                if (data.getAvailableActions().size() == 0) {
                    initMenu();
                } else {
                    for (ActionContext actionContext : data.getAvailableActions()) {
                        fooMenu.addItem(buildActionButton(actionContext));
                    }
                }
            }
        });

    }

    public void setSelectedIds(List<Id> selectedIds) {
        initMenu();
        if (selectedIds.size() > 0) {
            getActionsByIds(selectedIds);
        }

    }

    public void getActionsByIds(List<Id> selectedIds) {
        actionsData = new TableViewerData();
        actionsData.setSelectedIds(selectedIds);
        Command command = new Command("getActionsByIds", "table-viewer", actionsData);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while getting multiple actions for Ids");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                actionsData = (TableViewerData) result;
                fooMenu.clearItems();
                if (actionsData.getIdsActions().size() > 0) {
                    for (String menuName : actionsData.getIdsActions().keySet()) {
                        fooMenu.addItem(addMultiItem(menuName));
                    }
                } else {
                    initMenu();
                }
            }
        });
    }

    private void initMenu() {
        fooMenu.clearItems();
        SafeHtml noActionsMenu = SafeHtmlUtils.fromString("Нет доступных действий");
        fooMenu.addItem(new MenuItem(noActionsMenu)).addStyleName("item-disable");
    }

    private MenuItem addMultiItem(final String itemName) {
        Scheduler.ScheduledCommand menuItemCommand = new Scheduler.ScheduledCommand() {
            public void execute() {
                multiContextToExecute = actionsData.getIdsActions().size();
                for(ActionContext aContext : actionsData.getIdsActions().get(itemName)){
                    Scheduler.ScheduledCommand cmd = getCommandForMultipleContext(aContext);
                    cmd.execute();
                }
            }
        };
        MenuItem menuItem = new MenuItem(itemName, menuItemCommand);
        return menuItem;
    }

}
