package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeAndAccessValue;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormViewerConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.*;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * Created by andrey on 20.10.14.
 */
public abstract class LinkedFormOpeningHandler implements ClickHandler {
    protected Id id;
    protected EventBus eventBus;
    protected boolean tooltipContent;
    protected Map<String, PopupTitlesHolder> typeTitleMap;
    protected String popupTitle;
    private String domainObjectType;
    protected boolean modalWindow = true;
    public LinkedFormOpeningHandler(Id id, EventBus eventBus, boolean tooltipContent,
                                    Map<String, PopupTitlesHolder> typeTitleMap) {
        this.id = id;
        this.eventBus = eventBus;
        this.tooltipContent = tooltipContent;
        this.typeTitleMap = typeTitleMap;
    }

    protected void createEditableFormDialogBox(FormDialogBox dialogBox, HasLinkedFormMappings widget) {
        final FormPluginConfig config = createFormPluginConfig(widget, true);
        final FormDialogBox editableFormDialogBox = dialogBox;
        dialogBox.clearButtons();
        final FormPlugin formPluginEditable = editableFormDialogBox.createFormPlugin(config, eventBus);
        editableFormDialogBox.initButton(LocalizeUtil.get(OPEN_IN_FULL_WINDOW_KEY, OPEN_IN_FULL_WINDOW), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openInFullWindow(editableFormDialogBox, true);
            }
        });
        editableFormDialogBox.initButton(LocalizeUtil.get(SAVE_BUTTON_KEY, SAVE_BUTTON), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                editableOnChangeClick(formPluginEditable, editableFormDialogBox);
            }
        });
        editableFormDialogBox.initButton(LocalizeUtil.get(CANCELLATION_BUTTON_KEY, CANCELLATION_BUTTON), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                editableOnCancelClick(formPluginEditable, editableFormDialogBox);
            }
        });
    }

    private String getModalHeight(HasLinkedFormMappings widget) {
        return GuiUtil.getModalHeight(domainObjectType, widget.getLinkedFormMappingConfig(), widget.getLinkedFormConfig());
    }

    private String getModalWidth(HasLinkedFormMappings widget) {
       return GuiUtil.getModalWidth(domainObjectType, widget.getLinkedFormMappingConfig(), widget.getLinkedFormConfig());
    }

    protected void init(final HasLinkedFormMappings widget){

        Command command = new Command("getTypeAndAccess", "domain-object-type-extractor", id);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                DomainObjectTypeAndAccessValue value = (DomainObjectTypeAndAccessValue) result;
               domainObjectType = value.getDomainObjectType();
               PopupTitlesHolder popupTitlesHolder = typeTitleMap == null ? null : typeTitleMap.get(Case.toLower(domainObjectType));
               popupTitle = popupTitlesHolder == null ? null : popupTitlesHolder.getTitleExistingObject();
               createNonEditableFormDialogBox(widget, value.isHasWritePermission());
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining domain object type", caught);
            }
        });
    }

    protected void createNonEditableFormDialogBox(HasLinkedFormMappings widget, boolean hasWritePermission) {
        boolean resizable = GuiUtil.isFormResizable(domainObjectType, widget.getLinkedFormMappingConfig(),
                widget.getLinkedFormConfig());
        final FormDialogBox noneEditableFormDialogBox = new FormDialogBox(popupTitle,
               getModalWidth(widget), getModalHeight(widget), resizable);
        final FormPluginConfig config = createFormPluginConfig(widget, false);
        final FormPlugin plugin = noneEditableFormDialogBox.createFormPlugin(config, eventBus);
        noneEditableFormDialogBox.initButton(LocalizeUtil.get(OPEN_IN_FULL_WINDOW_KEY, OPEN_IN_FULL_WINDOW), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openInFullWindow(noneEditableFormDialogBox, false);
            }
        });
        if(hasWritePermission){
            noneEditableFormDialogBox.initButton(LocalizeUtil.get(EDIT_BUTTON_KEY, EDIT_BUTTON), new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    noEditableOnChangeButtonClick(plugin, noneEditableFormDialogBox);
                }

            });
        }
        noneEditableFormDialogBox.initButton(LocalizeUtil.get(CANCEL_BUTTON_KEY, CANCEL_BUTTON), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                noEditableOnCancelClick(plugin, noneEditableFormDialogBox);
            }
        });
    }

    private FormPluginConfig createFormPluginConfig(HasLinkedFormMappings widget, boolean editable) {
        final FormPluginConfig config = new FormPluginConfig();
        LinkedFormViewerConfig formViewerConfig = new LinkedFormViewerConfig();
        List<LinkedFormConfig> linkedFormConfigs = GuiUtil.getLinkedFormConfigs(widget.getLinkedFormConfig(),
                widget.getLinkedFormMappingConfig());
        formViewerConfig.setLinkedFormConfig(linkedFormConfigs);
        config.setFormViewerConfig(formViewerConfig);
        config.setDomainObjectId(id);
        config.getPluginState().setEditable(editable);
        return config;
    }

    protected void openInFullWindow(FormDialogBox dialogBox, boolean editable) {

    }

    protected SaveAction getSaveAction(final FormPlugin formPlugin, final Id rootObjectId) {
        SaveActionContext saveActionContext = new SaveActionContext();
        saveActionContext.setRootObjectId(rootObjectId);
        final ActionConfig actionConfig = new ActionConfig("save.action");
        actionConfig.setDirtySensitivity(false);
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
