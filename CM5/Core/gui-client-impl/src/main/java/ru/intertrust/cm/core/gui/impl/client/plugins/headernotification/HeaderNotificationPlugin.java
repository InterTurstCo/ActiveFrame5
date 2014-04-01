package ru.intertrust.cm.core.gui.impl.client.plugins.headernotification;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.util.logging.Logger;

/**
 * Created by lvov on 25.03.14.
 */
@ComponentName("header.notifications.plugin")
public class HeaderNotificationPlugin extends Plugin {

    static Logger log = Logger.getLogger("header.notifications.plugin");
    @Override
    public PluginView createView() {
        return new HeaderNotificationPluginView(this);
    }

    @Override
    public Component createNew() {
        return new HeaderNotificationPlugin();
    }
}
