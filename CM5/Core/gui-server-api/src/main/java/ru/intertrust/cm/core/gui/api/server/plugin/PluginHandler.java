package ru.intertrust.cm.core.gui.api.server.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

/**
 * Обработчик команд плагинов
 *
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 16:21
 */
public abstract class PluginHandler {
    public abstract PluginData createPluginData();

    public PluginData initialize(Dto param) {
        return null;
    }
}
