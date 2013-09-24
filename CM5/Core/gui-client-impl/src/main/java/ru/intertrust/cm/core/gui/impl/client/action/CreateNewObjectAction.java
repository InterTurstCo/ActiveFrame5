package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.SomeActivePlugin;
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
        SomeActivePlugin currentPlugin = (SomeActivePlugin) getPlugin();
        String domainObjectTypeToCreate = currentPlugin.getRootDomainObject().getTypeName();

        SomeActivePluginConfig config = new SomeActivePluginConfig(domainObjectTypeToCreate);
        config.setDomainObjectTypeToCreate(domainObjectTypeToCreate);
        SomeActivePlugin newPlugin = ComponentRegistry.instance.get(currentPlugin.getName());
        newPlugin.setConfig(config);

        currentPlugin.getOwner().open(newPlugin);
    }

    @Override
    public CreateNewObjectAction createNew() {
        return new CreateNewObjectAction();
    }
}
