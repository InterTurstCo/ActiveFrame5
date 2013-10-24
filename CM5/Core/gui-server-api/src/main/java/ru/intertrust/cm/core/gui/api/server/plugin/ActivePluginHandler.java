package ru.intertrust.cm.core.gui.api.server.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;

/**
 * Обработчик плагина, способного выполнять действия, внешнее представление которых отображается в "Панели действий".
 *
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 16:25
 */
public abstract class ActivePluginHandler extends PluginHandler {
    @Autowired
    protected ActionService actionService;

    public abstract ActivePluginData initialize(Dto param);
}
