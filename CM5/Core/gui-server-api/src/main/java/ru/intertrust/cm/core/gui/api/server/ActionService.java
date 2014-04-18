package ru.intertrust.cm.core.gui.api.server;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * Данный сервис отдает доступные действия для текущего контекста (типа
 * доменного объекта и его статуса).
 */
public interface ActionService {
    public interface Remote extends ActionService {
    }

    /**
     * Возвращает действия для переданного идентификатора доменного объекта.
     * @return действия для переданного доменного объекта.
     */
    List<ActionContext> getActions(Id domainObjectId);

    /**
     * Возвращает доступные действия для нового доменного объекта
     * @param domainObjectType
     * @return
     */
    List<ActionContext> getActions(String domainObjectType);

    /**
     * Returns default toolbar for plugin.
     * @param pluginName componentName of plugin.
     * @return default toolbar of plugin. Can be NULL if toolbar not defined.
     */
    ToolBarConfig getDefaultToolbarConfig(String pluginName);

}
