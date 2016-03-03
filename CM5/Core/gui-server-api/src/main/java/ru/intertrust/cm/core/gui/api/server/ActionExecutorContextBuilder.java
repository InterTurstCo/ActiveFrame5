package ru.intertrust.cm.core.gui.api.server;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * Created by Ravil Abdulkhairov
 * on 18.02.2016.
 * CMFIVE-5049
 */
public interface ActionExecutorContextBuilder {
    ActionContext getActionContext(Id rootDomainObject, WidgetContext widgetContext, ActionConfig actionConfig);
}
