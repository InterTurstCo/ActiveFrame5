package ru.intertrust.cm.core.gui.impl.server.configextension;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.SummaryConfigurationException;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Ravil on 24.05.2017.
 */
@ComponentName("validate.extensions.action.handler")
public class ValidateDraftsActionHandler extends ConfigExtensionsGuiActionBase {

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        SimpleActionData aData = new SimpleActionData();
        if(context.getRootObjectId()!=null ||
                (context.getObjectsIds()!=null && context.getObjectsIds().size()>0)){
            List<Id> extIds = (context.getObjectsIds().size()>0)?context.getObjectsIds():
                    Arrays.asList(context.getRootObjectId());
            if(extIds!=null) {
                try {
                    configurationControlService.validateInactiveExtensionsById(extIds);
                    aData.setOnSuccessMessage("Объект успешно проверен.");
                } catch(ConfigurationException e){
                    throw new GuiException(e.getMessage());
                }

            }
        }
        return aData;
    }

    @Override
    public SimpleActionContext getActionContext(ActionConfig actionConfig) {
        return new SimpleActionContext(actionConfig);
    }
}
