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
import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.SimpleActionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.tableviewer.TableViewerConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.CustomDelete;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEvent;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.gui.model.form.widget.TableViewerData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.List;

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
    private ToggleButton deleteButton;
    private Button addButton;
    // меню для списка activiti
    private MenuBar actionsMenu;
    private MenuBar fooMenu;

    // меню для обычных действий из toolbar
    private MenuBar toolbarActionsMenu;
    private MenuBar toolbarFooMenu;

    private Id selectedId;
    private EventBus eventBus;
    private TableViewerData actionsData;
    private Integer multiContextToExecute = 0;
    private List<Id> selectedIds;
    private TableViewerConfig config;

    public TableViewerToobar(EventBus localEventBus, final List<Id> selectedIds) {
        this.eventBus = localEventBus;
        this.selectedIds = selectedIds;
        toolbarPanel = new HorizontalPanel();
        editButton = new ToggleButton();
        deleteButton = new ToggleButton();
        addButton = new Button();

        editButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().editButton());
        //editButton.addStyleName("edit-btn-table-viewer");
        editButton.addStyleName("edit-btn-table-viewer-disable");
        editButton.setTitle("Открыть");


        deleteButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().editButton());
        deleteButton.addStyleName("delete-btn-table-viewer-disable");
        deleteButton.setTitle("Удалить");


        addButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().addDoBtn());
        addButton.addStyleName("add-btn-table-viewer");
        addButton.setTitle("Создать");

        editButton.addClickHandler(new ClickHandler() {
                                       @Override
                                       public void onClick(ClickEvent event) {
                                           if (selectedId != null && selectedIds.size() == 0) {
                                               eventBus.fireEvent(new OpenDomainObjectFormEvent(selectedId));
                                           }
                                       }
                                   }
        );

        deleteButton.addClickHandler(new ClickHandler() {
                                         @Override
                                         public void onClick(ClickEvent event) {
                                             CustomDelete deleteComponent;
                                             if (selectedId != null && selectedIds.size() == 0) {
                                                 if (config != null && config.getDeleteComponent() != null) {
                                                     deleteComponent = ComponentRegistry.instance.get(config.getDeleteComponent());
                                                 } else {
                                                     deleteComponent = ComponentRegistry.instance.get("default.custom.delete.component");
                                                 }

                                                 if (deleteComponent != null) {
                                                     deleteComponent.delete(selectedId, eventBus);
                                                 } else {
                                                     Window.alert("Невозможно получить компонент для удаления:" + config.getDeleteComponent());
                                                 }
                                             }

                                         }
                                     }
        );

        toolbarPanel.add(editButton);
        toolbarPanel.add(deleteButton);
        toolbarPanel.add(addButton);


        actionsMenu = new MenuBar();
        fooMenu = new MenuBar(true);
        actionsMenu.setStyleName("button-table-viewer");
        fooMenu.setStyleName("wrapApproval");
        actionsMenu.setTitle("Процессы");
        actionsMenu.addItem("", fooMenu);

        toolbarActionsMenu = new MenuBar();
        toolbarFooMenu = new MenuBar(true);
        toolbarActionsMenu.setStyleName("button-actions-viewer");
        toolbarFooMenu.setStyleName("wrapActions");
        toolbarActionsMenu.setTitle("Действия");
        toolbarActionsMenu.addItem("", toolbarFooMenu);

        initMenu();
        initActionsMenu();

        toolbarPanel.add(actionsMenu);
        toolbarPanel.add(toolbarActionsMenu);
        actionsMenu.setVisible(true);
        toolbarActionsMenu.setVisible(true);
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

    public ToggleButton getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(ToggleButton deleteButton) {
        this.deleteButton = deleteButton;
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
//        activateSingleRowAction();
        if (selectedId == null) {
            initMenu();
            activateSingleRowAction(false, false);
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

    private MenuItem buildToolbarActionButton(final ActionContext context, Boolean isSimpleAction) {
        String actionText = (isSimpleAction) ?
                ((SimpleActionConfig) ((SimpleActionContext) context).getActionConfig()).getText() :
                ((ActionConfig) context.getActionConfig()).getText();
        return new MenuItem(actionText, getCommandForToolbarAction(context));
    }

    private Scheduler.ScheduledCommand getCommandForContext(final ActionContext context) {
        Scheduler.ScheduledCommand menuItemCommand = new Scheduler.ScheduledCommand() {
            public void execute() {
                SimpleActionConfig actionConfig = context.getActionConfig();
                String actionHandler = actionConfig.getActionHandler();
                if (actionHandler == null) {
                    actionHandler = "generic.workflow.action";
                }
                Command command = new Command("executeAction", actionHandler, context);
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

    private Scheduler.ScheduledCommand getCommandForToolbarAction(final ActionContext context) {

        Scheduler.ScheduledCommand menuItemCommand = new Scheduler.ScheduledCommand() {
            public void execute() {
                String actionHandler = null;
                String componentName = null;
                if (selectedId == null && (selectedIds == null || selectedIds.size() == 0)) {
                    Window.alert("Не выбрана строка");
                    return;
                }
                if (context instanceof SimpleActionContext) {
                    SimpleActionConfig actionConfig = context.getActionConfig();
                    actionHandler = actionConfig.getActionHandler();
                } else {
                    ActionConfig actionConfig = context.getActionConfig();
                    componentName = actionConfig.getComponentName();
                }

                if (selectedId != null && selectedIds.size() == 0) {
                    context.setRootObjectId(selectedId);
                } else if (selectedIds.size() > 0) {
                    context.setObjectsIds(selectedIds);
                }

                Command command = new Command("executeAction", (actionHandler == null) ? componentName : actionHandler, context);
                BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("something was going wrong while obtaining details for stage ");
                        Window.alert("Ошибка выполнения. " + caught.getMessage());
                        caught.printStackTrace();
                    }

                    @Override
                    public void onSuccess(Dto result) {
                        if (((ActionData) result).getOnSuccessMessage() != null) {
                            Window.alert(((ActionData) result).getOnSuccessMessage());
                        }
                        if (context.getRootObjectId() != null && context.getObjectsIds().size() == 0) {
                            eventBus.fireEvent(new UpdateCollectionEvent(context.getRootObjectId()));
                            setSelectedId(context.getRootObjectId());
                        } else if (context.getObjectsIds().size() > 0) {
                            for (Id id : context.getObjectsIds()) {
                                eventBus.fireEvent(new UpdateCollectionEvent(id));
                            }
                        }
                    }
                });
            }
        };
        return menuItemCommand;
    }

    private Scheduler.ScheduledCommand getCommandForMultipleContext(final ActionContext context) {
        Scheduler.ScheduledCommand menuItemCommand = new Scheduler.ScheduledCommand() {
            public void execute() {
                SimpleActionConfig actionConfig = context.getActionConfig();
                String actionHandler = actionConfig.getActionHandler();
                if (actionHandler == null) {
                    actionHandler = "generic.workflow.action";
                }
                Command command = new Command("executeAction", actionHandler, context);
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
                        if (multiContextToExecute <= 0) {
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

                activateSingleRowAction(true, data.getHasDeleteAccess());

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

    private void initActionsMenu() {
        toolbarFooMenu.clearItems();

        SafeHtml noActionsMenu = SafeHtmlUtils.fromString("Нет доступных действий");
        if (config == null || config.getCollectionViewerConfig() == null ||
                config.getCollectionViewerConfig().getToolBarConfig() == null ||
                config.getCollectionViewerConfig().getToolBarConfig().getActions() == null ||
                config.getCollectionViewerConfig().getToolBarConfig().getActions().size() == 0) {
            toolbarFooMenu.addItem(new MenuItem(noActionsMenu)).addStyleName("item-disable");
        } else {
            for (AbstractActionConfig aConfig : config.getCollectionViewerConfig().getToolBarConfig().getActions()) {
                if (aConfig instanceof SimpleActionConfig) {
                    SimpleActionContext aContext = new SimpleActionContext();
                    aContext.setActionConfig(aConfig);
                    toolbarFooMenu.addItem(buildToolbarActionButton(aContext, true));
                } else {
                    ActionContext aContext = new ActionContext();
                    aContext.setActionConfig(aConfig);
                    toolbarFooMenu.addItem(buildToolbarActionButton(aContext, false));
                }
            }
        }

    }

    private MenuItem addMultiItem(final String itemName) {
        Scheduler.ScheduledCommand menuItemCommand = new Scheduler.ScheduledCommand() {
            public void execute() {
                multiContextToExecute = actionsData.getIdsActions().size();
                for (ActionContext aContext : actionsData.getIdsActions().get(itemName)) {
                    Scheduler.ScheduledCommand cmd = getCommandForMultipleContext(aContext);
                    cmd.execute();
                }
            }
        };
        MenuItem menuItem = new MenuItem(itemName, menuItemCommand);
        return menuItem;
    }

    public void activateSingleRowAction(boolean hasEdit, boolean hasDelete) {
        if(hasEdit){
            editButton.addStyleName("edit-btn-table-viewer");
            editButton.removeStyleName("edit-btn-table-viewer-disable");
        }else{
            editButton.addStyleName("edit-btn-table-viewer-disable");
            editButton.removeStyleName("edit-btn-table-viewer");
        }

        if(hasDelete){
            deleteButton.addStyleName("delete-btn-table-viewer");
            deleteButton.removeStyleName("delete-btn-table-viewer-disable");
        }else{
            deleteButton.addStyleName("delete-btn-table-viewer-disable");
            deleteButton.removeStyleName("delete-btn-table-viewer");
        }
    }

    public void setConfig(TableViewerConfig config) {
        this.config = config;
        initActionsMenu();
    }

    public void setWorkflowMenuVisible(boolean isVisible){
        actionsMenu.setVisible(isVisible);
    }
}
