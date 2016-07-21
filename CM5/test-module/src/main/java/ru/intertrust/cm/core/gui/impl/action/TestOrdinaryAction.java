package ru.intertrust.cm.core.gui.impl.action;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.action.SimpleServerAction;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 21.07.2016
 * Time: 10:29
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("ordinary.action")
public class TestOrdinaryAction extends SimpleServerAction {

    @Override
    public Component createNew() {
        return new TestOrdinaryAction();
    }

    @Override
    protected ActionContext appendCurrentContext(ActionContext initialContext) {
        ActionContext context = initialContext;
        context.setRootObjectId(context.getRootObjectId());
        return context;
    }

    @Override
    protected void onSuccess(ActionData result){
        Window.alert("Ordinary action result: "+result.getOnSuccessMessage());
    }

    @Override
    protected String getDefaultOnSuccessMessage() {
        return "Default Ordinary Success message!";
    }
}
