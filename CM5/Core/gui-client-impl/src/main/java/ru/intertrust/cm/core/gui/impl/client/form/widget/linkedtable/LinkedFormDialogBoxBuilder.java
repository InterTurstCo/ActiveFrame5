package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormViewerConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.FormPluginView;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

import java.util.Collection;
import java.util.Map;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.CANCEL_BUTTON_KEY;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CANCEL_BUTTON;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE;


/**
 * Created by andrey on 27.02.14.
 */
public class LinkedFormDialogBoxBuilder {

    private DialogBoxAction saveAction;
    private DialogBoxAction cancelAction;
    private FormState formState;
    private Id id;
    private String objectTypeName;
    private FormPlugin formPlugin;
    private String height = "300px";
    private String width = "1000px";
    private DialogBox dialogBox;
    private PopupTitlesHolder popupTitlesHolder;
    private Map<String, PopupTitlesHolder> typeTitleMap;
    private LinkedFormMappingConfig linkedFormMappingConfig;
    private WidgetsContainer parentWidgetsContainer;
    private Map<String, Collection<String>> parentWidgetIdsForNewFormMap;
    private boolean editable = true;

    public FormPlugin getFormPlugin() {
        return formPlugin;
    }

    public LinkedFormDialogBoxBuilder setSaveAction(DialogBoxAction saveAction) {
        this.saveAction = saveAction;
        return this;
    }

    public LinkedFormDialogBoxBuilder setCancelAction(DialogBoxAction cancelAction) {
        this.cancelAction = cancelAction;
        return this;
    }

    public LinkedFormDialogBoxBuilder withFormState(FormState formState) {
        this.formState = formState;
        return this;
    }

    public LinkedFormDialogBoxBuilder withId(Id id) {
        this.id = id;
        return this;
    }

    public LinkedFormDialogBoxBuilder withObjectType(String objectTypeName) {
        this.objectTypeName = objectTypeName;
        return this;
    }
    public LinkedFormDialogBoxBuilder withEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public LinkedFormDialogBoxBuilder withTypeTitleMap(Map<String, PopupTitlesHolder> typeTitleMap) {
        this.typeTitleMap = typeTitleMap;
        return this;
    }

    public LinkedFormDialogBoxBuilder withParentWidgetIds(Map<String, Collection<String>> parentWidgetIdsForNewFormMap) {
        this.parentWidgetIdsForNewFormMap = parentWidgetIdsForNewFormMap;
        return this;
    }

    public LinkedFormDialogBoxBuilder withWidgetsContainer(WidgetsContainer widgetsContainer) {
        this.parentWidgetsContainer = widgetsContainer;
        return this;
    }

    public LinkedFormDialogBoxBuilder withWidth(String width) {
        if (width != null) {
            this.width = width;
        }
        return this;
    }

    public LinkedFormDialogBoxBuilder withHeight(String height) {
        if (height != null) {
            this.height = height;
        }
        return this;
    }

    public LinkedFormDialogBoxBuilder withPopupTitlesHolder(PopupTitlesHolder popupTitlesHolder) {
        this.popupTitlesHolder = popupTitlesHolder;
        return this;
    }

    public LinkedFormDialogBoxBuilder buildDialogBox() {
        final FormPluginConfig linkedFormPluginConfig;
        if (id != null) {
            linkedFormPluginConfig = createLinkedFormPluginConfig(id, linkedFormMappingConfig);
        } else if (this.objectTypeName != null) {
            linkedFormPluginConfig = createLinkedFormPluginConfig(this.objectTypeName, linkedFormMappingConfig);
        } else {
            throw new IllegalArgumentException("Id or objectTypeName should be set");
        }
        this.formPlugin = buildLinkedFormPlugin(linkedFormPluginConfig);
        final PluginPanel formPluginPanel = new PluginPanel();

        dialogBox = buildLinkedFormDialogBox(formPluginPanel);
        this.formPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                if (formState != null) {
                    refreshEditableState();
                    formPlugin.setFormState(formState);

                }
                if (parentWidgetsContainer != null) {
                    FormPluginView formPluginView = (FormPluginView) formPlugin.getView();
                    WidgetsContainer widgetContainer = (WidgetsContainer) formPluginView.getViewWidget();
                    widgetContainer.setParentWidgetsContainer(parentWidgetsContainer);
                }
                setTitle();
            }
        });

        formPluginPanel.open(this.formPlugin);
        return this;
    }

    private void setTitle() {
        String domainObjectType = this.objectTypeName == null ? null : this.objectTypeName.toLowerCase();
        PopupTitlesHolder popupTitlesHolder = typeTitleMap == null ? this.popupTitlesHolder : typeTitleMap.get(domainObjectType);
        String title = popupTitlesHolder == null ? (GuiUtil.getConfiguredTitle(formPlugin, id == null))
                : getTitleFromHolder(popupTitlesHolder);
        dialogBox.getCaption().setText(title);
    }

    private String getTitleFromHolder(PopupTitlesHolder popupTitlesHolder) {
        return id == null ? popupTitlesHolder.getTitleNewObject() : popupTitlesHolder.getTitleExistingObject();
    }

    private void refreshEditableState() {
        Map<String, WidgetState> fullWidgetsState = formState.getFullWidgetsState();
        for (Map.Entry<String, WidgetState> stringWidgetStateEntry : fullWidgetsState.entrySet()) {
            stringWidgetStateEntry.getValue().setEditable(true);
        }
    }

    private FormPlugin buildLinkedFormPlugin(FormPluginConfig formPluginConfig) {
        FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
        formPlugin.setConfig(formPluginConfig);
        formPlugin.setLocalEventBus(new SimpleEventBus());
        return formPlugin;
    }

    private DialogBox buildLinkedFormDialogBox(PluginPanel linkedFormPluginPanel) {
        // create dialog box
        final DialogBox db = new DialogBox();
        db.removeStyleName("gwt-DialogBox");
        db.addStyleName("popup-body popup-z-index");
        db.setModal(true);
        Panel buttons = new FlowPanel();
        // create buttons
        if (editable) {
            Button saveButton = new Button("Сохранить");
            saveButton.setStyleName("lnfm-save-button darkButton");
            decorateButton(saveButton);
            if (saveAction != null) {
                saveButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (isValid()) {
                            saveAction.execute(formPlugin);
                            db.clear();
                            db.hide();
                        }
                    }
                });
            }
            buttons.add(saveButton);
        }
        Button cancelButton = new Button(LocalizeUtil.get(CANCEL_BUTTON_KEY, CANCEL_BUTTON));
        cancelButton.setStyleName("lnfm-cancel-button darkButton");
        decorateButton(cancelButton);

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (cancelAction != null) {
                    cancelAction.execute(formPlugin);
                }
                db.clear();
                db.hide();
            }
        });

        buttons.addStyleName("linkedFormButtonsPanel");

        buttons.add(cancelButton);
        ScrollPanel bodyPanel = new ScrollPanel();
        bodyPanel.setStyleName("linkedFormBodyPanel");
        Panel container = new FlowPanel();
        Panel formPluginWrapper = new FlowPanel();
        formPluginWrapper.setWidth(width);
        formPluginWrapper.setHeight(height);
        formPluginWrapper.add(linkedFormPluginPanel);
        container.add(formPluginWrapper);
        container.add(buttons);
        bodyPanel.add(container);
        Panel panel = new AbsolutePanel();
        panel.add(bodyPanel);
        panel.add(buttons);
        db.setWidget(panel);

        return db;
    }

    private void decorateButton(Button saveButton) {
        saveButton.removeStyleName("gwt-Button");
    }

    private FormPluginConfig createLinkedFormPluginConfig(Id domainObjectId, LinkedFormMappingConfig linkedFormMappingConfig) {
        FormPluginConfig config;
        config = new FormPluginConfig(domainObjectId);
        addLinkedFormViewer(linkedFormMappingConfig, config);
        addPluginStateToConfig(config);
        return config;
    }

    private void addLinkedFormViewer(LinkedFormMappingConfig linkedFormMappingConfig, FormPluginConfig config) {
        if (linkedFormMappingConfig != null) {
            LinkedFormViewerConfig linkedFormViewerConfig = new LinkedFormViewerConfig();
            linkedFormViewerConfig.setLinkedFormConfig(linkedFormMappingConfig.getLinkedFormConfigs());
            config.setFormViewerConfig(linkedFormViewerConfig);
        }
    }

    private FormPluginConfig createLinkedFormPluginConfig(String objectTypeName, LinkedFormMappingConfig linkedFormMappingConfig) {
        FormPluginConfig config = new FormPluginConfig(objectTypeName);
        addLinkedFormViewer(linkedFormMappingConfig, config);
        addPluginStateToConfig(config);
        addParentStateToConfig(objectTypeName, config);
        return config;
    }

    private void addPluginStateToConfig(FormPluginConfig config) {
        FormPluginState formPluginState = new FormPluginState();
        formPluginState.setEditable(editable);
        config.setPluginState(formPluginState);
    }

    private void addParentStateToConfig(String objectType, FormPluginConfig config) {
        if (parentWidgetIdsForNewFormMap != null) {
            Collection<String> widgetIds = parentWidgetIdsForNewFormMap.get(objectType.toLowerCase());
            config.setParentFormState(GuiUtil.createParentFormStatesHierarchy(parentWidgetsContainer, widgetIds));
        }
        config.setParentId(GuiUtil.getParentId(parentWidgetsContainer));
    }

    public void display() {
        dialogBox.center();
    }

    private boolean isValid() {
        if (formPlugin != null) {
            FormPanel panel = (FormPanel) formPlugin.getView().getViewWidget();
            ValidationResult validationResult = new ValidationResult();
            for (BaseWidget widget : panel.getWidgets()) {
                validationResult.append(widget.validate());
            }
            if (validationResult.hasErrors()) {
                ApplicationWindow.errorAlert(LocalizeUtil.get(CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE_KEY,
                        CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE));
                return false;
            }
        }
        return true;
    }

    public LinkedFormDialogBoxBuilder withLinkedFormMapping(LinkedFormMappingConfig linkedFormMappingConfig) {
        this.linkedFormMappingConfig = linkedFormMappingConfig;
        return this;
    }
}
