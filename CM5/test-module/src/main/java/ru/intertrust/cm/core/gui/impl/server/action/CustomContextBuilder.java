package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.stereotype.Component;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.ActionExecutorContextBuilder;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.OpenLinkActionContext;

/**
 * Created by Ravil Abdulkhairov
 * on 18.02.2016.
 * CMFIVE-5049
 */
@Component("sample.action.context.builder")
public class CustomContextBuilder implements ActionExecutorContextBuilder {

    @Override
    public ActionContext getActionContext(Id rootDomainObject, WidgetContext widgetContext, ActionConfig actionConfig) {
        OpenLinkActionContext context = new OpenLinkActionContext(actionConfig);
        context.setBaseUrl("http://www.intertrust.ru");
        return context;
    }
}
