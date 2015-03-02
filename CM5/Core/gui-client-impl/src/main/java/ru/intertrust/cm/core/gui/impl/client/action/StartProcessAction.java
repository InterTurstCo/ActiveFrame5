package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
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
    protected StartProcessActionContext appendCurrentContext(ActionContext initialContext) {
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        FormState formState = editor.getFormState();
        StartProcessActionContext context = (StartProcessActionContext) initialContext;
        context.setRootObjectId(formState.getObjects().getRootNode().getDomainObject().getId());
        context.setFormState(formState);
        context.setPluginState(editor.getFormPluginState());
        context.setFormViewerConfig(editor.getFormViewerConfig());
        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        FormPluginData formPluginData = ((StartProcessActionData) result).getFormPluginData();
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
        return LocalizeUtil.get(BusinessUniverseConstants.PROCESS_IS_STARTED_MESSAGE);
    }

}
