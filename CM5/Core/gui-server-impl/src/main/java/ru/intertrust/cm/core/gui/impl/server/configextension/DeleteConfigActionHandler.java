package ru.intertrust.cm.core.gui.impl.server.configextension;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.SummaryConfigurationException;
import ru.intertrust.cm.core.gui.impl.server.action.DeleteActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.DeleteActionData;

import java.util.Arrays;
import java.util.List;

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
        try {
            configurationControlService.deleteNewExtensions(Arrays.asList(context.getRootObjectId()));
            result.setId(context.getRootObjectId());
        } catch(ConfigurationException e){
            throw new GuiException(e.getMessage());
        }
        return result;
    }
}
