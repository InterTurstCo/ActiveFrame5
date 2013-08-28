package ru.intertrust.cm.core.gui.impl.server.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:22
 */
@ComponentName("some.plugin")
public class SomePluginHandler extends PluginHandler {
    @Override
    public PluginData initialize(Dto param) {
        System.out.println("SomePluginHandler initialized");
        return null;
    }

    public PluginData doSomethingGood(Dto dto) {
        System.out.println("SomePluginHandler executed doSomethingGood()");
        return null;
    }

}
