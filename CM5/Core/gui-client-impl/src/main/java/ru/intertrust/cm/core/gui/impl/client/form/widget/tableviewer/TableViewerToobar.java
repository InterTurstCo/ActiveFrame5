package ru.intertrust.cm.core.gui.impl.client.form.widget.tableviewer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.SimpleActionConfig;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEvent;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 18.04.2016
 * Time: 11:03
 * To change this template use File | Settings | File and Code Templates.
 */
public class TableViewerToobar  {
    private HorizontalPanel toolbarPanel;
    private ToggleButton editButton;
    private Button addButton;
    private MenuBar actionsMenu;
    private Id selectedId;
    private EventBus eventBus;

    public TableViewerToobar(EventBus localEventBus){
        this.eventBus = localEventBus;
        toolbarPanel = new HorizontalPanel();
        editButton = new ToggleButton();
        addButton = new Button();
        editButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().editButton());
        editButton.addStyleName("edit-btn-table-viewer");
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
        MenuBar fooMenu = new MenuBar(true);
        actionsMenu.setStyleName("button-table-viewer");
        fooMenu.setStyleName("wrapApproval");

        //for (ActionContext actionContext : model.getAvailableActions()) {
        //    fooMenu.addItem(buildActionButton(actionContext));
        //}

        //if (model.getAvailableActions() != null && model.getAvailableActions().size() > 0) {
        actionsMenu.addItem("", fooMenu);
        toolbarPanel.add(actionsMenu);
        actionsMenu.setVisible(false);
        //}
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

    public void setSelectedId(Id selectedId) {
        this.selectedId = selectedId;
    }

    private MenuItem buildActionButton(final ActionContext context) {
        SimpleActionContext simpleActionContext = (SimpleActionContext) context;
        SimpleActionConfig simpleActionConfig = simpleActionContext.getActionConfig();

        Scheduler.ScheduledCommand menuItemCommand = new Scheduler.ScheduledCommand() {
            public void execute() {
                Command command = new Command("executeAction", "approval.generic.workflow.action", context);
                BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("something was going wrong while obtaining details for stage ");
                        Window.alert("Ошибка выполнения. " + caught.getMessage());
                        caught.printStackTrace();
                    }

                    @Override
                    public void onSuccess(Dto result) {
                        /**
                         * После выполнения снять выделение
                         */
                    }
                });
            }
        };
        MenuItem menuItem = new MenuItem(simpleActionConfig.getText(), menuItemCommand);

        return menuItem;
    }
}
