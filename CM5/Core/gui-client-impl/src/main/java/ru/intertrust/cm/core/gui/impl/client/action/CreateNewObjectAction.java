package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.FormMappingConfig;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 23.09.13
 *         Time: 20:03
 */
@ComponentName("create.new.object.action")
public class CreateNewObjectAction extends Action {
    private static final String OBJECT_TYPE_PROP = "create.object.type";
    private static final String OBJECT_FORM_PROP = "create.object.form";

    @Override
    protected void execute() {
        final ActionConfig actionConfig = getInitialContext().getActionConfig();
        IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        final String domainObjectTypeToCreate;
        if (actionConfig.getProperty(OBJECT_TYPE_PROP) == null) {
            domainObjectTypeToCreate = editor.getRootDomainObject().getTypeName();
        } else {
            domainObjectTypeToCreate = actionConfig.getProperty(OBJECT_TYPE_PROP);
        }
        final FormPluginConfig formPluginConfig = new FormPluginConfig(domainObjectTypeToCreate);
        formPluginConfig.setDomainObjectTypeToCreate(domainObjectTypeToCreate);
        final FormPluginState state = editor.getFormPluginState();
        formPluginConfig.setPluginState(state);
        if (actionConfig.getProperty(OBJECT_TYPE_PROP) == null) {
            final FormViewerConfig viewerConfig = new FormViewerConfig();
            final FormMappingConfig formMappingConfig = new FormMappingConfig();
            formMappingConfig.setDomainObjectType(domainObjectTypeToCreate);
            formMappingConfig.setForm(actionConfig.getProperty(OBJECT_FORM_PROP));
            final List<FormMappingConfig> formMappingConfigList = new ArrayList<>();
            viewerConfig.setFormMappingConfigList(formMappingConfigList);
            formPluginConfig.setFormViewerConfig(viewerConfig);
        } else {
            formPluginConfig.setFormViewerConfig(editor.getFormViewerConfig());
        }
        if (state.isToggleEdit()) {
            state.setEditable(true);
            final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
            formPlugin.setConfig(formPluginConfig);
            formPlugin.setDisplayActionToolBar(true);
            formPlugin.setLocalEventBus(plugin.getLocalEventBus());
            state.setInCentralPanel(true); //CMFIVE-2252
            getPlugin().getOwner().openChild(formPlugin);
        } else {
            editor.replaceForm(formPluginConfig);
        }
    }

    @Override
    public CreateNewObjectAction createNew() {
        return new CreateNewObjectAction();
    }
}
