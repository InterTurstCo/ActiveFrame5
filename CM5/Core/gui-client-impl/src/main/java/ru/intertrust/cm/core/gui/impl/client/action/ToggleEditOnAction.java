package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Sergey.Okolot
 */
@ComponentName("toggle.edit.on.action")
public class ToggleEditOnAction extends ToggleAction {

    @Override
    public void execute() {
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        final Id id = editor.getFormState().getObjects().getRootNode().getDomainObject().getId();
        final FormPluginConfig config;
        if (id == null) {
            config = new FormPluginConfig(editor.getRootDomainObject().getTypeName());
        } else {
            config = new FormPluginConfig(id);
        }
        config.setPluginState(editor.getFormPluginState());
        config.getPluginState().setEditable(true);
        updateForm(config);
//        editor.replaceForm(config);
    }

    @Override
    public Component createNew() {
        return new ToggleEditOnAction();
    }

    private void updateForm(final FormPluginConfig config) {
        final DomainObjectSurferPlugin dosPlugin = (DomainObjectSurferPlugin) getPlugin();
        final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
        formPlugin.setConfig(config);
        formPlugin.setDisplayActionToolBar(true);
        Object view = formPlugin.getView();
        dosPlugin.getOwner().openChild(formPlugin);
    }
}
