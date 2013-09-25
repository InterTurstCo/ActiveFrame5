package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.form.Form;

/**
 * @author Denis Mitavskiy
 *         Date: 18.09.13
 *         Time: 22:00
 */
@ComponentName("save.action")
public class SaveAction extends SimpleServerAction {
    @Override
    public void execute() {
        super.execute();
    }

    @Override
    public Component createNew() {
        return new SaveAction();
    }

    @Override
    protected SaveActionContext getContext() {
        Form form = (Form) getPlugin().getCurrentState();
        SaveActionContext context = new SaveActionContext();
        context.setRootObjectId(form.getRootObject().getId());
        context.setForm(form);
        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        SaveActionData data = (SaveActionData) result;
        getPlugin().reinit(data.getSomeActivePluginData());
        Window.alert("Saved!!!");
    }
}
