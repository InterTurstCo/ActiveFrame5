package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.StartProcessActionContext;
import ru.intertrust.cm.core.gui.model.action.StartProcessActionData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Denis Mitavskiy
 *         Date: 23.10.13
 *         Time: 15:10
 */
@ComponentName("start.process.action")
public class StartProcessAction extends SimpleServerAction {
    @Override
    public Component createNew() {
        return new StartProcessAction();
    }

    @Override
    protected StartProcessActionContext getCurrentContext() {
        FormState formState = ((IsDomainObjectEditor) getPlugin()).getFormState();
        StartProcessActionContext context = new StartProcessActionContext();
        context.setRootObjectId(formState.getObjects().getRootNode().getObject().getId());
        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        FormPluginData formPluginData = ((StartProcessActionData) result).getFormPluginData();
        Plugin plugin = getPlugin();
        ((IsDomainObjectEditor) plugin).setFormState(formPluginData.getFormDisplayData().getFormState());
        plugin.setActionContexts(formPluginData.getActionContexts());
        Window.alert("Process Started!!!");
    }
}
