package ru.intertrust.cm.core.gui.api.server.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.ActionConfig;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;

import java.util.ArrayList;
import java.util.List;

/**
 * Обработчик плагина, способного выполнять действия, внешнее представление которых отображается в "Панели действий".
 *
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 16:25
 */
public abstract class ActivePluginHandler extends PluginHandler {
    /**
     * Создаёт экземпляр объекта, содержащего данные инициализации плагина
     * @return экземпляр объекта, содержащего данные инициализации плагина
     */
    public abstract ActivePluginData createPluginData();

    public ActivePluginData initialize(Dto param) {
        ActivePluginData result = createPluginData();
        result.setActionConfigs(getActions());
        return result;
    }

    /**
     * Возвращает набор действий, отображаемых в "Панели действий"
     * @return набор действий, отображаемых в "Панели действий"
     */
    public List<ActionConfig> getActions() {
        ArrayList<ActionConfig> actions = new ArrayList<>();
        actions.add(new ActionConfig("Сохранить"));
        return actions;
    }
}
