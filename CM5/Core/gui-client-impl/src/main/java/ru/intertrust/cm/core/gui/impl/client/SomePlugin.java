package ru.intertrust.cm.core.gui.impl.client;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Denis Mitavskiy
 *         Date: 13.08.13
 *         Time: 18:43
 */
@ComponentName("some.plugin")
public class SomePlugin extends Plugin {

    @Override
    public PluginView createView() {
        return new SomePluginView(this);
    }

    @Override
    public Component createNew() {
        return new SomePlugin();
    }

    @Override
    protected boolean isInitializable() {
        return true;
    }
}
