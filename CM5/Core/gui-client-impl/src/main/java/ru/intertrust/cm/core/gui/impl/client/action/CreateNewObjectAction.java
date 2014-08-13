package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Denis Mitavskiy
 *         Date: 23.09.13
 *         Time: 20:03
 */
@ComponentName("create.new.object.action")
public class CreateNewObjectAction extends Action {

    @Override
    protected void execute() {
        IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        String domainObjectTypeToCreate = editor.getRootDomainObject().getTypeName();
        FormPluginConfig config = new FormPluginConfig(domainObjectTypeToCreate);
        config.setDomainObjectTypeToCreate(domainObjectTypeToCreate);
        final FormPluginState state = editor.getFormPluginState();
        config.setPluginState(state);
        config.setFormViewerConfig(editor.getFormViewerConfig());
        if (state.isToggleEdit()) {
            state.setEditable(true);
            final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
            formPlugin.setConfig(config);
            formPlugin.setDisplayActionToolBar(true);
            formPlugin.setLocalEventBus(plugin.getLocalEventBus());
            getPlugin().getOwner().openChild(formPlugin);
        } else {
            editor.replaceForm(config);
        }
    }

    @Override
    public CreateNewObjectAction createNew() {
        return new CreateNewObjectAction();
    }
}
