package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 21.07.2016
 * Time: 10:35
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("ordinary.action")
public class TestOrdinaryActionHandler extends ActionHandler {

    @Override
    public ActionData executeAction(ActionContext context) {
        ActionData aData = new ActionData();
        if (context.getRootObjectId() != null && context.getObjectsIds().size()==0) {
            aData.setOnSuccessMessage("Hello from handler for object " + context.getRootObjectId());
        } else if(context.getObjectsIds().size()>0){
            StringBuilder sBuilder = new StringBuilder();
            for(Id id : context.getObjectsIds()){
                sBuilder.append(id).append(' ');
            }
            aData.setOnSuccessMessage("Hello from handler for objects " + sBuilder.toString());
        }
        return aData;
    }

    @Override
    public ActionContext getActionContext(final ActionConfig actionConfig) {
        return new ActionContext();
    }
}
