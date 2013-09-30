package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.SomeActivePlugin;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEventHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginConfig;

import java.util.logging.Logger;

@ComponentName("domain.object.surfer.plugin")
public class DomainObjectSurferPlugin extends Plugin implements IsActive, NavigationTreeItemSelectedEventHandler, CollectionRowSelectedEventHandler {

    private Plugin formPlugin;

    static Logger log = Logger.getLogger("domain.object.surfer.plugin");


    @Override
    public PluginView createView() {
        return new DomainObjectSurferPluginView(this);
    }

    @Override
    public Component createNew() {
        return new DomainObjectSurferPlugin();
    }

    @Override
    public void onNavigationTreeItemSelected(NavigationTreeItemSelectedEvent event) {
        log.info("domain object surfer plugin reloaded");
        getOwner().closeCurrentPlugin();
        DomainObjectSurferPlugin domainObjectSurfer = ComponentRegistry.instance.get("domain.object.surfer.plugin");
        domainObjectSurfer.setConfig(event.getPluginConfig());
        getOwner().open(domainObjectSurfer);
        domainObjectSurfer.addHandler(CollectionRowSelectedEvent.TYPE, domainObjectSurfer);
    }

    public Plugin getFormPlugin() {
        return formPlugin;
    }

    public void setFormPlugin(Plugin formPlugin) {
        this.formPlugin = formPlugin;
    }

    @Override
    public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
        // todo 2 times!!!
        PluginPanel formPluginPanel = formPlugin.getOwner();
        formPluginPanel.closeCurrentPlugin();
        SomeActivePlugin newFormPlugin = ComponentRegistry.instance.get("some.active.plugin");
        newFormPlugin.setConfig(new SomeActivePluginConfig(event.getId()));
        formPluginPanel.open(newFormPlugin);
        this.formPlugin = newFormPlugin;
    }
}
