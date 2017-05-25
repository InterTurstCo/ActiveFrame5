package ru.intertrust.cm.core.gui.impl.server.configextension;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Ravil on 25.05.2017.
 */
@ComponentName("deactivate.extensions.action.handler")
public class DeactivateActionHandler extends ConfigExtensionsGuiActionBase {
    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        SimpleActionData aData = new SimpleActionData();
        if(context.getRootObjectId()!=null ||
                (context.getObjectsIds()!=null && context.getObjectsIds().size()>0)){
            aData.setSavedMainObjectId(context.getRootObjectId());
            List<Id> extIds = (context.getObjectsIds().size()>0)?context.getObjectsIds():
                    Arrays.asList(context.getRootObjectId());
            if(extIds!=null) {
                try {
                    configurationControlService.deactivateExtensionsById(extIds);
                    aData.setOnSuccessMessage("Объект успешно деактивирован.");
                } catch(ConfigurationException e){
                    throw new GuiException(e.getMessage());
                }

            }
        }
        return aData;
    }
}
