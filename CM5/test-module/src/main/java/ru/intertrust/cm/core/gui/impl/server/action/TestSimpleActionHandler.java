package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 21.07.2016
 * Time: 9:33
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("test.simple.action")
public class TestSimpleActionHandler extends ActionHandler<SimpleActionContext, SimpleActionData> {
    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        SimpleActionData actionData = new SimpleActionData();
        if (context.getRootObjectId() != null) {
            actionData.setOnSuccessMessage("Simple action success for object " + context.getRootObjectId() + " !");
        }
        if(context.getObjectsIds().size()>0){
            StringBuilder sBuilder = new StringBuilder();
            for(Id id : context.getObjectsIds()){
                sBuilder.append(id).append(' ');
            }
            actionData.setOnSuccessMessage("Simple action success for objects " + sBuilder.toString() + " !");
        }
        return actionData;
    }

    @Override
    public SimpleActionContext getActionContext(ActionConfig actionConfig) {
        return new SimpleActionContext(actionConfig);
    }
}
