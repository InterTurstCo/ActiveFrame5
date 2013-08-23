package ru.intertrust.cm.core.gui.impl.server.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginData;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:22
 */
@ComponentName("some.active.plugin")
public class SomeActivePluginHandler extends ActivePluginHandler {
    @Override
    public ActivePluginData initialize(Dto param) {
        System.out.println("SomeActivePluginHandler initialized!");
        return super.initialize(param);
    }

    @Override
    public ActivePluginData createPluginData() {
        return new SomeActivePluginData();
    }

    public ActivePluginData doSomethingVeryGood(Dto dto) {
        System.out.println("SomeActivePluginHandler executed doSomethingVeryGood()");
        return null;
    }
}
