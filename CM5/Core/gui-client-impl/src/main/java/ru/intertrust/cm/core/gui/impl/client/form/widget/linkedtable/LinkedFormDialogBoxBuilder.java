package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormViewerConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
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
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.CaptionCloseButton;
import ru.intertrust.cm.core.gui.impl.client.panel.ResizablePanel;
import ru.intertrust.cm.core.gui.impl.client.panel.RightSideResizablePanel;
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
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;




/**
 * Created by andrey on 27.02.14.
 */
public class LinkedFormDialogBoxBuilder  {

    private static final int MINIMAL_HEIGHT = 200;
    private static final int MINIMAL_WIDTH = 300;

    private DialogBoxAction saveAction;
    private DialogBoxAction cancelAction;
    private FormState formState;
    private Id id;
    private String objectTypeName;
    private String linkedFormName;
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
    private boolean resizable;
    private Button cancelButton;
    private Id externalParentId;

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

    public LinkedFormDialogBoxBuilder withLinkedFormName(String linkedFormName) {
        this.linkedFormName = linkedFormName;
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

    public LinkedFormDialogBoxBuilder withExternalParentId(Id id) {
        this.externalParentId = id;
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
            linkedFormPluginConfig = createLinkedFormPluginConfig(this.objectTypeName, this.linkedFormName, linkedFormMappingConfig);
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
        if(this.formPlugin.getConfig()!=null &&
                ((FormPluginConfig)this.formPlugin.getConfig()).getParentId()==null && externalParentId!=null){
            ((FormPluginConfig)this.formPlugin.getConfig()).setParentId(externalParentId);
        }
        formPluginPanel.open(this.formPlugin);
        return this;
    }

    private void setTitle() {
        String domainObjectType = this.objectTypeName == null ? null : Case.toLower(this.objectTypeName);
        PopupTitlesHolder popupTitlesHolder = typeTitleMap == null ? this.popupTitlesHolder : typeTitleMap.get(domainObjectType);
        String title = popupTitlesHolder == null ? (GuiUtil.getConfiguredTitle(formPlugin, id == null))
                : getTitleFromHolder(popupTitlesHolder);
        Panel captionPanel = new AbsolutePanel();
        captionPanel.add(new Label(title));
        HTML caption = (HTML) dialogBox.getCaption();
        caption.getElement().appendChild(captionPanel.getElement());
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
        db.addStyleName("dialogBoxBody");
        db.setModal(true);
        Panel buttonsPanel = new AbsolutePanel();
        buttonsPanel.addStyleName("buttons-panel");
        buttonsPanel.getElement().getStyle().clearPosition();
        // create buttons
        if (editable) {
            Button saveButton = new Button(LocalizeUtil.get(LocalizationKeys.SAVE_BUTTON_KEY, SAVE_BUTTON));
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
            buttonsPanel.add(saveButton);
        }
        cancelButton = new Button(LocalizeUtil.get(CANCEL_BUTTON_KEY, CANCEL_BUTTON));
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
        VerticalPanel panel = new VerticalPanel();
        panel.addStyleName("form-dialog-box-content");

        linkedFormPluginPanel.asWidget().addStyleName("frm-pnl-top");
        panel.add(linkedFormPluginPanel);

        buttonsPanel.add(cancelButton);
        panel.add(buttonsPanel);
        panel.setWidth(width);
        panel.setHeight(height);
        ResizablePanel resizablePanel = new RightSideResizablePanel(MINIMAL_WIDTH, MINIMAL_HEIGHT, true, resizable);
        resizablePanel.wrapWidget(panel);
        db.add(resizablePanel);

        HTML caption = (HTML) db.getCaption();
        CaptionCloseButton captionCloseButton = new CaptionCloseButton();
        captionCloseButton.addClickListener(new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                cancelButton.click();
            }
        });
        caption.getElement().appendChild(captionCloseButton.getElement());

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

    private FormPluginConfig createLinkedFormPluginConfig(String objectTypeName, String linkedFormName, LinkedFormMappingConfig linkedFormMappingConfig) {
        FormPluginConfig config = new FormPluginConfig(objectTypeName, linkedFormName);
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
            Collection<String> widgetIds = parentWidgetIdsForNewFormMap.get(Case.toLower(objectType));
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

    public LinkedFormDialogBoxBuilder withFormResizable(boolean resizable) {
        this.resizable = resizable;
        return this;
    }
}
