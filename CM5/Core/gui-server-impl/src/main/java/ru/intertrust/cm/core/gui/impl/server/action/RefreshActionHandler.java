package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.CollectionPluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.RefreshActionContext;
import ru.intertrust.cm.core.gui.model.action.RefreshActionData;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowsResponse;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowsRequest;

/**
 * @author Sergey.Okolot
 *         Created on 09.06.2014 13:42.
 */
@ComponentName("refresh.action")
public class RefreshActionHandler extends ActionHandler<ActionContext, ActionData> {
    @Autowired
    private CollectionPluginHandler collectionPluginHandler;
    @Override
    public ActionData executeAction(ActionContext context) {
        RefreshActionContext refreshActionContext = (RefreshActionContext) context;
        CollectionRowsRequest rowsRequest = refreshActionContext.getRequest();
        Id id = refreshActionContext.getId();
        CollectionRowsResponse response = collectionPluginHandler.refreshCollection(rowsRequest, id);
        RefreshActionData result = new RefreshActionData(response);
        return result;
    }

    @Override
    public ActionContext getActionContext(final ActionConfig actionConfig) {
        return new RefreshActionContext(actionConfig);
    }
}
