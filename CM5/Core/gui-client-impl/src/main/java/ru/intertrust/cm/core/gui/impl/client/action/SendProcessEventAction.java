package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.action.SendProcessEventActionContext;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Denis Mitavskiy
 *         Date: 23.10.13
 *         Time: 15:19
 */
@ComponentName("send.process.event.action")
public class SendProcessEventAction extends SimpleServerAction {
    @Override
    public Component createNew() {
        return new SendProcessEventAction();
    }

    @Override
    protected SendProcessEventActionContext appendCurrentContext(ActionContext initialContext) {
        FormState formState = ((IsDomainObjectEditor) getPlugin()).getFormState();
        SendProcessEventActionContext context = (SendProcessEventActionContext) initialContext;
        context.setRootObjectId(formState.getObjects().getRootNode().getDomainObject().getId());
        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        FormPluginData formPluginData = ((SaveActionData) result).getFormPluginData();
        Plugin plugin = getPlugin();
        ((IsDomainObjectEditor) plugin).setFormState(formPluginData.getFormDisplayData().getFormState());
        plugin.setToolbarContext(formPluginData.getToolbarContext());
        ApplicationWindow.infoAlert(BusinessUniverseConstants.EVENT_IS_SENT_MESSAGE);
    }
}
