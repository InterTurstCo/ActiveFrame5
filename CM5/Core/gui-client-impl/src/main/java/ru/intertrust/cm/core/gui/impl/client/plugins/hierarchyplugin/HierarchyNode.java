package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyCollectionConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyGroupConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.*;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable.DialogBoxAction;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable.LinkedFormDialogBoxBuilder;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 29.07.2016
 * Time: 12:20
 * To change this template use File | Settings | File and Code Templates.
 */
public abstract class HierarchyNode extends Composite implements ExpandHierarchyEventHandler,HierarchyPluginConstants,
        AutoOpenEventHandler
{
    protected HierarchyCollectionConfig collectionConfig;
    protected HierarchyGroupConfig groupConfig;
    protected EventBus localBus;
    protected EventBus commonBus;
    protected AbsolutePanel rootPanel;
    protected HorizontalPanel headerPanel;
    protected VerticalPanel childPanel;
    protected HierarchyGuiElementsFactory guiElementsFactory;
    protected HierarchyGuiFactory guiFactory;
    protected Boolean expanded = false;
    protected Id parentId;
    protected String viewID;
    protected String parentViewID;
    protected Widget expandButton;
    protected Boolean sortAscending = true;
    protected Boolean isGroup = false;



    protected abstract void addRepresentationCells(Panel container);

    protected HierarchyNode(Boolean thisIsGroup){
        localBus = new SimpleEventBus();
        guiElementsFactory = new HierarchyGuiElementsFactory();
        guiFactory = new HierarchyGuiFactory();
        rootPanel = new AbsolutePanel();
        headerPanel = new HorizontalPanel();
        childPanel = new VerticalPanel();
        isGroup = thisIsGroup;
    }

    @Override
    public void onExpandHierarchyEvent(ExpandHierarchyEvent event) {

            expanded = event.isExpand();
            childPanel.setVisible(expanded);
            if (expanded) {
                for (HierarchyGroupConfig group : (groupConfig != null) ? groupConfig.getHierarchyGroupConfigs() : collectionConfig.getHierarchyGroupConfigs()) {
                    childPanel.add(guiFactory.buildGroup(group, event.getParentId(), commonBus, getViewID(), event.isAutoClick()));
                }
                for (HierarchyCollectionConfig collection : (groupConfig != null) ? groupConfig.getHierarchyCollectionConfigs() : collectionConfig.getHierarchyCollectionConfigs()) {
                    childPanel.add(guiFactory.buildCollection(collection, event.getParentId(), commonBus, getViewID(), event.isAutoClick(),sortAscending));
                }

            } else {
                childPanel.clear();
            }

            if (event.isAutoClick()) {
                final Timer timer = new Timer() {
                    @Override
                    public void run() {
                        commonBus.fireEvent(new AutoOpenedEvent(getViewID()));

                    }
                };
                timer.schedule(500);

                final Timer t2 = new Timer() {
                    @Override
                    public void run() {
                        commonBus.fireEvent(new AutoOpenedEvent(getViewID()));

                    }
                };
                t2.schedule(1000);

                final Timer t3 = new Timer() {
                    @Override
                    public void run() {
                        commonBus.fireEvent(new AutoOpenedEvent(getViewID()));

                    }
                };
                t3.schedule(2000);
            }

            if (expanded) {
                if (event.isAutoClick() && (collectionConfig == null || collectionConfig.getHierarchyCollectionConfigs().size() == 0)) {
                    commonBus.fireEvent(new AutoOpenedEvent(getViewID()));


                }
            }


    }



    public String getViewID() {
        return viewID;
    }

    public void setViewID(String viewID) {
        this.viewID = viewID;
    }

    public String getParentViewID() {
        return parentViewID;
    }

    public void setParentViewID(String parentViewID) {
        this.parentViewID = parentViewID;
    }

    protected static native void clickElement(Element elem) /*-{
        elem.click();
    }-*/;

    @Override
    public void onAutoOpenEvent(AutoOpenEvent autoOpenEvent) {
        if (autoOpenEvent.getViewId().equals(getViewID())) {
            clickElement(expandButton.getElement());
        }
    }

    protected void showNewForm(final String domainObjectType, final Id aParentId) {
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
                .withHeight(GuiUtil.getModalHeight(domainObjectType, (isGroup)?groupConfig.getLinkedFormMappingConfig():collectionConfig.getLinkedFormMappingConfig(), null))
                .withWidth(GuiUtil.getModalWidth(domainObjectType, (isGroup)?groupConfig.getLinkedFormMappingConfig():collectionConfig.getLinkedFormMappingConfig(), null))
                .withObjectType(domainObjectType)
                .withLinkedFormMapping((isGroup)?groupConfig.getLinkedFormMappingConfig():collectionConfig.getLinkedFormMappingConfig())
                .withPopupTitlesHolder(null)
                .withParentWidgetIds(null)
                .withWidgetsContainer(null)
                .withTypeTitleMap(null)
                .withFormResizable(false)
                .withExternalParentId(aParentId)
                .buildDialogBox();

        lfb.display();

    }

    protected SaveAction getSaveAction(final FormPlugin formPlugin, final Id rootObjectId) {
        SaveActionContext saveActionContext = new SaveActionContext();
        saveActionContext.setRootObjectId(rootObjectId);
        formPlugin.setLocalEventBus((isGroup)?localBus:commonBus);
        final ActionConfig actionConfig = new ActionConfig("save.action");
        actionConfig.setDirtySensitivity(false);
        saveActionContext.setActionConfig(actionConfig);
        final SaveAction action = ComponentRegistry.instance.get(actionConfig.getComponentName());
        action.setInitialContext(saveActionContext);
        action.setPlugin(formPlugin);
        return action;
    }
}
