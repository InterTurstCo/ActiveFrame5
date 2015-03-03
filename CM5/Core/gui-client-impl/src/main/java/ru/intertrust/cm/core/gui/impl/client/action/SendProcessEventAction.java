package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEvent;
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
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        if (plugin.getLocalEventBus() != null) {
            editor.setFormState(formPluginData.getFormDisplayData().getFormState());
            editor.setFormToolbarContext(formPluginData.getToolbarContext());
            plugin.getView().updateActionToolBar();
            plugin.getLocalEventBus().fireEvent(new UpdateCollectionEvent(
                    formPluginData.getFormDisplayData().getFormState().getObjects().getRootNode().getDomainObject()));
        }
    }

    @Override
    protected String getDefaultOnSuccessMessage() {
        return LocalizeUtil.get(LocalizationKeys.EVENT_IS_SENT_MESSAGE_KEY,
                BusinessUniverseConstants.EVENT_IS_SENT_MESSAGE);
    }

}
