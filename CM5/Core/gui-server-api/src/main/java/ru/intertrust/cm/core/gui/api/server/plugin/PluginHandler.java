package ru.intertrust.cm.core.gui.api.server.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

/**
 * Обработчик команд плагинов. По существующему контракту, обработчик команды плагина - это метод, принимающий
 * единственный параметр типа {@link Dto} и возвращающий результат типа {@link PluginData}. Например, метод
 * {@link #initialize(Dto)} является обработчиком команды плагина.
 *
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 16:21
 */
public abstract class PluginHandler implements ComponentHandler {

    public PluginData initialize(Dto param) {
        return null;
    }
}
