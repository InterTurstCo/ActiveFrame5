package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormViewerConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

/**
 * Created by andrey on 20.10.14.
 */
public abstract class LinkedFormOpeningHandler implements ClickHandler {
    protected Id id;
    protected EventBus eventBus;
    protected boolean tooltipContent;
    protected String popupTitle;

    public LinkedFormOpeningHandler(Id id, EventBus eventBus, boolean tooltipContent, String popupTitle) {
        this.id = id;
        this.eventBus = eventBus;
        this.tooltipContent = tooltipContent;
        this.popupTitle = popupTitle;
    }

    protected void createEditableFormDialogBox(HasLinkedFormMappings widget) {
        final FormPluginConfig config = createFormPluginConfig(widget, true);
        final FormDialogBox editableFormDialogBox = new FormDialogBox(popupTitle);
        final FormPlugin formPluginEditable = editableFormDialogBox.createFormPlugin(config, eventBus);
        editableFormDialogBox.initButton("Изменить", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                editableOnChangeClick(formPluginEditable, editableFormDialogBox);
            }
        });
        editableFormDialogBox.initButton("Отмена", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                editableOnCancelClick(formPluginEditable, editableFormDialogBox);
            }
        });
    }

    protected void createNonEditableFormDialogBox(HasLinkedFormMappings widget) {
        final FormDialogBox noneEditableFormDialogBox = new FormDialogBox(popupTitle);
        final FormPluginConfig config = createFormPluginConfig(widget, false);
        final FormPlugin plugin = noneEditableFormDialogBox.createFormPlugin(config, eventBus);
        noneEditableFormDialogBox.initButton("Открыть в полном окне", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openInFullWindow(plugin, noneEditableFormDialogBox);
            }
        });
        noneEditableFormDialogBox.initButton("Изменить", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                noEditableOnChangeButtonClick(plugin, noneEditableFormDialogBox);
            }

        });
        noneEditableFormDialogBox.initButton("Отмена", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                noEditableOnCancelClick(plugin, noneEditableFormDialogBox);
            }
        });
    }

    private FormPluginConfig createFormPluginConfig(HasLinkedFormMappings widget, boolean editable) {
        final FormPluginConfig config = new FormPluginConfig();
        LinkedFormViewerConfig formViewerConfig = new LinkedFormViewerConfig();
        LinkedFormMappingConfig linkedFormMappingConfig = widget.getLinkedFormMappingConfig();
        if (linkedFormMappingConfig != null) {
            formViewerConfig.setLinkedFormConfig(linkedFormMappingConfig.getLinkedFormConfigs());
        }
        config.setFormViewerConfig(formViewerConfig);
        config.setDomainObjectId(id);
        config.getPluginState().setEditable(editable);
        return config;
    }

    protected void openInFullWindow(FormPlugin formPlugin, FormDialogBox dialogBox) {
        formPlugin.setLocalEventBus(eventBus);
        formPlugin.setDisplayActionToolBar(true);
        Application.getInstance().getEventBus()
                .fireEvent(new CentralPluginChildOpeningRequestedEvent(formPlugin));
        dialogBox.hide();
    }

    protected SaveAction getSaveAction(final FormPlugin formPlugin, final Id rootObjectId) {
        SaveActionContext saveActionContext = new SaveActionContext();
        saveActionContext.setRootObjectId(rootObjectId);
        final ActionConfig actionConfig = new ActionConfig("save.action");
        saveActionContext.setActionConfig(actionConfig);

        final SaveAction action = ComponentRegistry.instance.get(actionConfig.getComponentName());
        action.setInitialContext(saveActionContext);
        action.setPlugin(formPlugin);
        return action;
    }

    protected abstract void editableOnCancelClick(FormPlugin formPlugin, FormDialogBox dialogBox);

    protected abstract void editableOnChangeClick(FormPlugin formPlugin, FormDialogBox dialogBox);

    protected abstract void noEditableOnCancelClick(FormPlugin formPlugin, FormDialogBox dialogBox);

    protected abstract void noEditableOnChangeButtonClick(FormPlugin formPlugin, FormDialogBox dialogBox);
}
