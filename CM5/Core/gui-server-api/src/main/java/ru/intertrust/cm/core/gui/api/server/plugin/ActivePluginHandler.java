package ru.intertrust.cm.core.gui.api.server.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.ActionConfig;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 16:25
 */
public abstract class ActivePluginHandler extends PluginHandler {
    @Override
    public abstract ActivePluginData createPluginData();

    public ActivePluginData initialize(Dto param) {
        ActivePluginData result = createPluginData();
        result.setActionConfigs(getActions());
        return result;
    }

    public List<ActionConfig> getActions() {
        ArrayList<ActionConfig> actions = new ArrayList<>();
        actions.add(new ActionConfig("Сохранить"));
        return actions;
    }
}
