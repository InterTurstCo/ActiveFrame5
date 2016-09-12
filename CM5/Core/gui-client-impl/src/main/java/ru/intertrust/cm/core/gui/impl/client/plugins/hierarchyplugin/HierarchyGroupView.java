package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;


import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyCollectionConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyGroupConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.*;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable.DialogBoxAction;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable.LinkedFormDialogBoxBuilder;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 27.07.2016
 * Time: 10:09
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyGroupView extends HierarchyNode
        implements HierarchyActionEventHandler,UpdateCollectionEventHandler {


    public HierarchyGroupView(HierarchyGroupConfig aGroupConfig, Id aParentId, EventBus aCommonBus, String aParentViewId) {
        super();
        commonBus = aCommonBus;
        parentId = aParentId;
        groupConfig = aGroupConfig;
        rootPanel.addStyleName(STYLE_PARENT_PANEL);
        headerPanel.addStyleName(STYLE_HEADER_PANEL);
        childPanel.addStyleName(STYLE_CHILD_PANEL);
        setViewID(aGroupConfig.getGid() + ((parentId != null) ? "-" + parentId.toStringRepresentation() : ""));
        parentViewID = aParentViewId;
        addRepresentationCells(headerPanel);
        rootPanel.add(headerPanel);
        rootPanel.add(childPanel);
        childPanel.setVisible(expanded);
        initWidget(rootPanel);
        commonBus.fireEvent(new NodeCreatedEvent(getViewID()));
        commonBus.addHandler(AutoOpenEvent.TYPE, this);
        localBus.addHandler(ExpandHierarchyEvent.TYPE, this);
        localBus.addHandler(HierarchyActionEvent.TYPE, this);
        localBus.addHandler(UpdateCollectionEvent.TYPE, this);
    }


    @Override
    protected void addRepresentationCells(Panel container) {
        FlexTable grid = new FlexTable();
        FlexTable.FlexCellFormatter cellFormatter = grid.getFlexCellFormatter();
        grid.addStyleName(STYLE_WRAP_PANEL);

        grid.setWidget(0, 0, guiElementsFactory.buildExpandCell(commonBus, localBus, parentId, getViewID(), getParentViewID()));
        expandButton = grid.getWidget(0, 0);

        InlineHTML groupName = new InlineHTML("<b>" + groupConfig.getName() + "</b>");
        grid.setWidget(0, 1, groupName);


        grid.setWidget(0, 3, guiElementsFactory.buildActionButton(localBus, Actions.GROUPSORT));
        grid.setWidget(0, 4, guiElementsFactory.buildActionButton(localBus, Actions.GROUPADD));
        cellFormatter.setStyleName(0, 1, STYLE_GROUP_NAME);
        container.add(grid);
    }


    @Override
    public void onHierarchyActionEvent(HierarchyActionEvent event) {
        if (event.getAction().equals(Actions.GROUPSORT)) {
            Boolean sortingFieldPresents = false;
            for (HierarchyCollectionConfig cConfig : groupConfig.getHierarchyCollectionConfigs()) {
                if (cConfig.getSortByField() != null) {
                    sortingFieldPresents = true;
                    break;
                }
            }
            if (sortingFieldPresents) {
                sortAscending = !sortAscending;
                clickElement(expandButton.getElement());
                clickElement(expandButton.getElement());
            } else {
                Window.alert("Ни одна коллекция не имеет параметра сортировки");
            }
        }
        else if(event.getAction().equals(Actions.GROUPADD)){
            if(groupConfig.getCreatedObjectsConfig()!=null){
                showNewForm(groupConfig.getCreatedObjectsConfig().getCreateObjectConfigs().get(0).getDomainObjectType());
            } else {
                Window.alert("Создание обьектов не сконфигурировано");
            }
        }
        else {
            Window.alert("Действие: " + event.getAction().toString());
        }
    }

    protected void showNewForm(final String domainObjectType) {
        LinkedFormDialogBoxBuilder linkedFormDialogBoxBuilder = new LinkedFormDialogBoxBuilder();

        DialogBoxAction saveAction = new DialogBoxAction() {
            @Override
            public void execute(FormPlugin formPlugin) {
                SaveAction action = getSaveAction(formPlugin, parentId);
                action.perform();
            }
        };


        DialogBoxAction cancelAction = new DialogBoxAction() {
            @Override
            public void execute(FormPlugin formPlugin) {
                // no op
            }
        };

        LinkedFormDialogBoxBuilder lfb = linkedFormDialogBoxBuilder
                .setSaveAction(saveAction)
                .setCancelAction(cancelAction)
                .withHeight(GuiUtil.getModalHeight(domainObjectType, groupConfig.getLinkedFormMappingConfig(), null))
                .withWidth(GuiUtil.getModalWidth(domainObjectType, groupConfig.getLinkedFormMappingConfig(), null))
                .withObjectType(domainObjectType)
                .withLinkedFormMapping(groupConfig.getLinkedFormMappingConfig())
                .withPopupTitlesHolder(null)
                .withParentWidgetIds(null)
                .withWidgetsContainer(null)
                .withTypeTitleMap(null)
                .withFormResizable(false)
                .withExternalParentId(parentId)
                .buildDialogBox();

        lfb.display();

    }

    protected SaveAction getSaveAction(final FormPlugin formPlugin, final Id rootObjectId) {
        SaveActionContext saveActionContext = new SaveActionContext();
        saveActionContext.setRootObjectId(rootObjectId);
        formPlugin.setLocalEventBus(localBus);
        final ActionConfig actionConfig = new ActionConfig("save.action");
        actionConfig.setDirtySensitivity(false);
        saveActionContext.setActionConfig(actionConfig);
        final SaveAction action = ComponentRegistry.instance.get(actionConfig.getComponentName());
        action.setInitialContext(saveActionContext);
        action.setPlugin(formPlugin);
        return action;
    }

    @Override
    public void updateCollection(UpdateCollectionEvent event) {
        GenericDomainObject eObject = (GenericDomainObject)event.getIdentifiableObject();
        if(groupConfig.getCreatedObjectsConfig()!=null){
            for(CreatedObjectConfig oConfig : groupConfig.getCreatedObjectsConfig().getCreateObjectConfigs()){
                if(oConfig.getDomainObjectType().toLowerCase().equals(eObject.getTypeName().toLowerCase())){
                    clickElement(expandButton.getElement());
                    clickElement(expandButton.getElement());
                }
            }
        }
    }
}
