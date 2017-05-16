package ru.intertrust.cm.core.gui.impl.server.extension;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

import java.util.List;

/**
 * Created by Ravil on 16.05.2017.
 */
@ComponentName("validate.action.handler")
public class ValidateActionHandler extends GuiExtensionsActionBase {



    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        SimpleActionData aData = new SimpleActionData();
        if(context.getRootObjectId()!=null){
            List<DomainObject> toolingDos = getToolingDos(context.getRootObjectId());
            if(toolingDos.size()>0) {
                try {
                    configurationControlService.validateDrafts(toolingDos);
                    aData.setOnSuccessMessage("Объект успешно проверен.");
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
