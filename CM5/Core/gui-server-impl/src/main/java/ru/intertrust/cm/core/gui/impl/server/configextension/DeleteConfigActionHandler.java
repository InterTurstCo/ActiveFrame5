package ru.intertrust.cm.core.gui.impl.server.configextension;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.gui.impl.server.action.DeleteActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.DeleteActionData;

/**
 * Created by Ravil on 23.05.2017.
 */
@ComponentName("delete.config.action.handler")
public class DeleteConfigActionHandler extends DeleteActionHandler {
    @Autowired
    ConfigurationControlService configurationControlService;

    @Override
    public DeleteActionData executeAction(ActionContext context) {
        DeleteActionData result = new DeleteActionData();
        result.setId(context.getRootObjectId());
        configurationControlService.deleteNewExtensions(context.getRootObjectId());
        return result;
    }
}
