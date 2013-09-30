package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.SomeActivePlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginConfig;

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
        SomeActivePlugin formPlugin = (SomeActivePlugin) currentPlugin.getFormPlugin();
        String domainObjectTypeToCreate = formPlugin.getRootDomainObject().getTypeName();

        SomeActivePluginConfig config = new SomeActivePluginConfig(domainObjectTypeToCreate);
        config.setDomainObjectTypeToCreate(domainObjectTypeToCreate);
        SomeActivePlugin newPlugin = ComponentRegistry.instance.get(formPlugin.getName());
        newPlugin.setConfig(config);
        currentPlugin.setFormPlugin(newPlugin);
        formPlugin.getOwner().open(newPlugin);
    }

    @Override
    public CreateNewObjectAction createNew() {
        return new CreateNewObjectAction();
    }
}
