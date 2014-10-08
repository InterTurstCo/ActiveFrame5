package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.CompleteTaskActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Denis Mitavskiy
 *         Date: 23.10.13
 *         Time: 15:18
 */
@ComponentName("complete.task.action")
public class CompleteTaskAction extends SimpleServerAction {
    @Override
    public Component createNew() {
        return new CompleteTaskAction();
    }

    @Override
    protected CompleteTaskActionContext appendCurrentContext(ActionContext initialContext) {
        FormState formState = ((IsDomainObjectEditor) getPlugin()).getFormState();
        CompleteTaskActionContext context = (CompleteTaskActionContext) initialContext;
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
        return BusinessUniverseConstants.TASK_COMPLETE_MESSAGE;
    }
}
