package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

import java.util.Map;

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

    public LinkedFormDialogBoxBuilder withWidth(String width) {
        if(width != null) {
        this.width = width;
        }
        return this;
    }
    public LinkedFormDialogBoxBuilder withHeight(String height) {
        if(height != null) {
        this.height = height;
        }
        return this;
    }

    public DialogBox buildDialogBox() {
        FormPluginConfig linkedFormPluginConfig;
        if (id != null) {
            linkedFormPluginConfig = createLinkedFormPluginConfig(id);
        } else if (this.objectTypeName != null) {
            linkedFormPluginConfig = createLinkedFormPluginConfig(this.objectTypeName);
        } else {
            throw new IllegalArgumentException("Id or objectTypeName should be set");
        }
        this.formPlugin = buildLinkedFormPlugin(linkedFormPluginConfig);
        final PluginPanel formPluginPanel = new PluginPanel();
        this.formPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                if (formState != null) {
                    refreshEditableState();
                    formPlugin.setFormState(formState);
                }
            }
        });

        formPluginPanel.open(this.formPlugin);
        return buildLinkedFormDialogBox(formPluginPanel);
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
        db.setHeight(height);
        db.setWidth(width);
        db.addStyleName("popup-body popup-z-index");
        db.setModal(true);

        // create buttons
        Button saveButton = new Button("Сохранить");
        saveButton.setStyleName("lnfm-save-button dark-button");
        decorateButton(saveButton);
        if (saveAction != null) {
            saveButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    saveAction.execute(formPlugin);
                    db.clear();
                    db.hide();
                }
            });
        }
        Button cancelButton = new Button("Отменить");
        cancelButton.setStyleName("lnfm-cancel-button light-button");
        decorateButton(cancelButton);
        if (cancelAction != null) {
            cancelButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    cancelAction.execute(formPlugin);
                    db.clear();
                    db.hide();
                }
            });
        }
        FlowPanel buttons = new FlowPanel();
        buttons.addStyleName("linked-form-buttons-panel");
        buttons.add(saveButton);
        buttons.add(cancelButton);
        ScrollPanel scrollPanel = new ScrollPanel();
        FlowPanel container = new FlowPanel();
        container.add(linkedFormPluginPanel);
        container.add(buttons);
        scrollPanel.add(container);
        db.setWidget(scrollPanel);

        return db;
    }

    private void decorateButton(Button saveButton) {
        saveButton.removeStyleName("gwt-Button");

    }


    private FormPluginConfig createLinkedFormPluginConfig(Id domainObjectId) {
        FormPluginConfig config;
        config = new FormPluginConfig(domainObjectId);
        addPluginStateToConfig(config);
        return config;
    }

    private FormPluginConfig createLinkedFormPluginConfig(String objectTypeName) {
        FormPluginConfig config;
        config = new FormPluginConfig(objectTypeName);
        addPluginStateToConfig(config);
        return config;
    }

    private void addPluginStateToConfig(FormPluginConfig config) {
        FormPluginState formPluginState = new FormPluginState();
        formPluginState.setEditable(true);
        config.setPluginState(formPluginState);
    }
}
