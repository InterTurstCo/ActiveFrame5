package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

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
    protected SaveActionContext getCurrentContext() {
        FormState formState = ((IsDomainObjectEditor) getPlugin()).getFormState();
        SaveActionContext context = new SaveActionContext();
        context.setRootObjectId(formState.getObjects().getRootObjects().getObject().getId());
        context.setFormState(formState);
        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        FormState formState = ((SaveActionData) result).getFormPluginData().getFormDisplayData().getFormState();
        ((IsDomainObjectEditor) getPlugin()).setFormState(formState);
        Window.alert("Saved!!!");
    }
}
