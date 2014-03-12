package ru.intertrust.cm.core.gui.impl.server.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.AttributeConfig;
import ru.intertrust.cm.core.config.gui.navigation.CustomPluginConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.SomePluginData;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:22
 */
@ComponentName("some.plugin")
public class SomePluginHandler extends PluginHandler {
    @Override
    public PluginData initialize(Dto param) {
        CustomPluginConfig customPluginConfig = (CustomPluginConfig)param;
        System.out.println("SomePluginHandler initialized");
        SomePluginData somePluginData = new SomePluginData();
        List<AttributeConfig> attributeConfigList = customPluginConfig.getAttributeConfigList();
        if (!attributeConfigList.isEmpty()) {
        somePluginData.setText(attributeConfigList.get(0).getValue());
        } else{
            somePluginData.setText("default");
        }
        return somePluginData;
    }

    public PluginData doSomethingGood(Dto dto) {
        System.out.println("SomePluginHandler executed doSomethingGood()");
        return null;
    }

}
