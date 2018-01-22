package ru.intertrust.cm.core.gui.impl.client.plugins.balancer;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.config.gui.balancer.BalancerControlConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by Ravil on 22.01.2018.
 */
@ComponentName("BalancerControl.plugin")
public class BalancerControlPlugin extends Plugin {
    private EventBus eventBus = new SimpleEventBus();

    @Override
    public Component createNew() {
        return new BalancerControlPlugin();
    }

    @Override
    public PluginView createView() {
        BalancerControlConfig bConfig = (BalancerControlConfig)getConfig();
        return new BalancerControlView(this,eventBus);
    }
}
