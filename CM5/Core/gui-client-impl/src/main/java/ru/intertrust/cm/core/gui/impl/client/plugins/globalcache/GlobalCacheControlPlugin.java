package ru.intertrust.cm.core.gui.impl.client.plugins.globalcache;

import com.google.gwt.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.GlobalCacheControlConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 21.10.2015
 */
@ComponentName("GlobalCacheControl.plugin")
public class GlobalCacheControlPlugin extends Plugin {

    private EventBus eventBus;

    public void setLocalEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public PluginView createView() {
        GlobalCacheControlConfig config = (GlobalCacheControlConfig)getConfig();
        return new GlobalCacheControlView(this,config.getStatisticsOnly());
    }

    @Override
    public Component createNew() {
        return new GlobalCacheControlPlugin();
    }
}
