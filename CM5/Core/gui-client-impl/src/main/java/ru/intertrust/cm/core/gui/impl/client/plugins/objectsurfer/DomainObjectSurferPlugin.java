package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEventHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;

import java.util.logging.Logger;

@ComponentName("domain.object.surfer.plugin")
public class DomainObjectSurferPlugin extends Plugin implements IsActive, NavigationTreeItemSelectedEventHandler {
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
        Plugin domainObjectSurfer = ComponentRegistry.instance.get("domain.object.surfer.plugin");
        getOwner().open(domainObjectSurfer);
    }
}
