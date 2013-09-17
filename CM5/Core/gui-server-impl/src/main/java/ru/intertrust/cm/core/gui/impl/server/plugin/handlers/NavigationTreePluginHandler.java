package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

import javax.ejb.EJB;

@ComponentName("navigation.tree")
public class NavigationTreePluginHandler extends PluginHandler {
    @EJB(mappedName = "java:app/GuiService")
    GuiService guiService;

    @Override
    public PluginData initialize(Dto param) {
        NavigationConfig navigationConfig = guiService.getNavigationConfiguration();
        NavigationTreePluginData navigationTreePluginData = new NavigationTreePluginData();
        navigationTreePluginData.setNavigationConfig(navigationConfig);
        return navigationTreePluginData;
    }
}
