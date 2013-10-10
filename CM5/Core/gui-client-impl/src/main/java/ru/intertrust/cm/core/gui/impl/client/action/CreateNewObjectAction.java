package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 23.09.13
 *         Time: 20:03
 */
@ComponentName("create.new.object.action")
public class CreateNewObjectAction extends Action {
    @Override
    public void execute() {
        DomainObjectSurferPlugin currentPlugin = (DomainObjectSurferPlugin) getPlugin();
        FormPlugin formPlugin = (FormPlugin) currentPlugin.getFormPlugin();
        String domainObjectTypeToCreate = formPlugin.getRootDomainObject().getTypeName();

        FormPluginConfig config = new FormPluginConfig(domainObjectTypeToCreate);
        config.setDomainObjectTypeToCreate(domainObjectTypeToCreate);
        FormPlugin newPlugin = ComponentRegistry.instance.get(formPlugin.getName());
        newPlugin.setConfig(config);
        currentPlugin.setFormPlugin(newPlugin);
        formPlugin.getOwner().open(newPlugin);
    }

    @Override
    public CreateNewObjectAction createNew() {
        return new CreateNewObjectAction();
    }
}
