package ru.intertrust.cm.core.gui.impl.client;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginData;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 15:28
 */
@ComponentName("some.active.plugin")
public class SomeActivePlugin extends Plugin implements IsActive {
    @Override
    public PluginView createView() {
        SomeActivePluginData initialData = getInitialData();
        return new SomeActivePluginView(this, initialData.getForm());
    }

    @Override
    public Component createNew() {
        return new SomeActivePlugin();
    }
}
