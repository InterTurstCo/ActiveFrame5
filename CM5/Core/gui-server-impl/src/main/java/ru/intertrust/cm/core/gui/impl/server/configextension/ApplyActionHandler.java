package ru.intertrust.cm.core.gui.impl.server.configextension;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

import java.util.List;

/**
 * Created by Ravil on 16.05.2017.
 */
@ComponentName("apply.action.handler")
public class ApplyActionHandler extends ConfigExtensionsGuiActionBase {

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        SimpleActionData aData = new SimpleActionData();
        if(context.getRootObjectId()!=null){
            aData.setSavedMainObjectId(context.getRootObjectId());
            List<DomainObject> toolingDos = (context.getObjectsIds().size()>0)?getMultipleToolingDos(context.getObjectsIds()):
                    getToolingDos(context.getRootObjectId());
            if(toolingDos.size()>0) {
                try {
                    configurationControlService.activateDrafts(toolingDos);
                    aData.setOnSuccessMessage("Объект успешно активирован.");
                } catch(ConfigurationException e){
                    throw new GuiException(e.getMessage());
                }
            } else {
                aData.setOnFailureMessage("Для данного обьекта нет шаблона");
            }
        } else {
            aData.setOnFailureMessage("Обьект еще не сохранен");
        }
        return aData;
    }

}
